package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRespostaDTO;
import com.thai.finance.api.finance.api.domain.entities.Estoque;
import com.thai.finance.api.finance.api.domain.entities.MovimentacaoEstoque;
import com.thai.finance.api.finance.api.domain.entities.Produto;
import com.thai.finance.api.finance.api.domain.enums.SituacaoEstoque;
import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import com.thai.finance.api.finance.api.exceptions.RegraNegocioException;
import com.thai.finance.api.finance.api.mapper.MapperMovimentacaoEstoque;
import com.thai.finance.api.finance.api.respository.RepositoryEstoque;
import com.thai.finance.api.finance.api.respository.RepositoryMovimentacaoEstoque;
import com.thai.finance.api.finance.api.respository.RepositoryProduto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ServiceMovimentacaoEstoqueTest {

    private RepositoryMovimentacaoEstoque repositoryMovimentacao;
    private RepositoryProduto repositoryProduto;
    private RepositoryEstoque repositoryEstoque;
    private ServiceMovimentacaoEstoque service;

    private Produto produto;
    private Estoque estoque;

    @BeforeEach
    void configurar() {
        repositoryMovimentacao = mock(RepositoryMovimentacaoEstoque.class);
        repositoryProduto = mock(RepositoryProduto.class);
        repositoryEstoque = mock(RepositoryEstoque.class);
        service = new ServiceMovimentacaoEstoque(repositoryMovimentacao, repositoryProduto, repositoryEstoque,
                Mappers.getMapper(MapperMovimentacaoEstoque.class));

        produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setNome("Arroz Tipo 1 5kg");
        produto.setEstoque_minimo(10);
        produto.setEstoque_maximo(100);
        produto.setPrazo_reposicao_dias(5);
        produto.setPreco(BigInteger.valueOf(2690));
        produto.setAtivo(true);

        estoque = new Estoque(UUID.randomUUID(), produto, 50);

        when(repositoryProduto.findById(produto.getId())).thenReturn(Optional.of(produto));
        when(repositoryEstoque.buscarParaAtualizacao(produto.getId())).thenReturn(Optional.of(estoque));
        when(repositoryMovimentacao.save(any(MovimentacaoEstoque.class))).thenAnswer(i -> i.getArgument(0));
        when(repositoryMovimentacao.somarQuantidade(eq(produto.getId()), any(), any(), any())).thenReturn(0L);
    }

    private MovimentacaoEstoqueRequisicaoDTO requisicao(TipoMovimentacaoEstoque tipo, int quantidade) {
        return new MovimentacaoEstoqueRequisicaoDTO(produto.getId(), tipo, quantidade, "teste");
    }

    @Test
    @DisplayName("Saída válida debita o saldo e registra saldo anterior e posterior")
    void saidaValida() {
        MovimentacaoEstoqueRespostaDTO resposta = service.salvar(requisicao(TipoMovimentacaoEstoque.SAIDA, 30));

        assertThat(estoque.getQuantidade()).isEqualTo(20);
        assertThat(resposta.saldo_anterior()).isEqualTo(50);
        assertThat(resposta.saldo_posterior()).isEqualTo(20);
        assertThat(resposta.situacao_estoque()).isEqualTo(SituacaoEstoque.NORMAL);
        verify(repositoryEstoque).save(estoque);
        verify(repositoryMovimentacao).save(any(MovimentacaoEstoque.class));
    }

    @Test
    @DisplayName("Saída que zera o estoque é permitida e sinaliza ruptura")
    void saidaQueZeraOEstoque() {
        MovimentacaoEstoqueRespostaDTO resposta = service.salvar(requisicao(TipoMovimentacaoEstoque.SAIDA, 50));
        assertThat(resposta.saldo_posterior()).isZero();
        assertThat(resposta.situacao_estoque()).isEqualTo(SituacaoEstoque.RUPTURA);
    }

    @Test
    @DisplayName("Saída que deixaria o estoque negativo é rejeitada sem gravar nada")
    void saidaMaiorQueOSaldoEhRejeitada() {
        assertThatThrownBy(() -> service.salvar(requisicao(TipoMovimentacaoEstoque.SAIDA, 51)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Saldo insuficiente")
                .hasMessageContaining("saldo atual 50");

        assertThat(estoque.getQuantidade()).isEqualTo(50);
        verify(repositoryEstoque, never()).save(any());
        verify(repositoryMovimentacao, never()).save(any());
    }

    @Test
    @DisplayName("Entrada e devolução somam ao saldo; entrada acima do máximo sinaliza superestoque")
    void entradaEDevolucao() {
        service.salvar(requisicao(TipoMovimentacaoEstoque.DEVOLUCAO, 2));
        MovimentacaoEstoqueRespostaDTO resposta = service.salvar(requisicao(TipoMovimentacaoEstoque.ENTRADA, 60));
        assertThat(estoque.getQuantidade()).isEqualTo(112);
        assertThat(resposta.situacao_estoque()).isEqualTo(SituacaoEstoque.SUPERESTOQUE);
    }

    @Test
    @DisplayName("Ajuste negativo subtrai do saldo (perda de inventário)")
    void ajusteNegativo() {
        service.salvar(requisicao(TipoMovimentacaoEstoque.AJUSTE, -8));
        assertThat(estoque.getQuantidade()).isEqualTo(42);
    }

    @Test
    @DisplayName("Ajuste negativo maior que o saldo é rejeitado")
    void ajusteNegativoMaiorQueOSaldo() {
        assertThatThrownBy(() -> service.salvar(requisicao(TipoMovimentacaoEstoque.AJUSTE, -51)))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(estoque.getQuantidade()).isEqualTo(50);
    }

    @Test
    @DisplayName("Produto inexistente resulta em 404")
    void produtoInexistente() {
        var dto = new MovimentacaoEstoqueRequisicaoDTO(UUID.randomUUID(), TipoMovimentacaoEstoque.ENTRADA, 1, "teste");
        when(repositoryProduto.findById(dto.produto_id())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.salvar(dto))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Produto inativo não aceita movimentações")
    void produtoInativo() {
        produto.setAtivo(false);
        assertThatThrownBy(() -> service.salvar(requisicao(TipoMovimentacaoEstoque.ENTRADA, 1)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    @DisplayName("ESTORNO não pode ser registrado diretamente")
    void estornoDireto() {
        assertThatThrownBy(() -> service.salvar(requisicao(TipoMovimentacaoEstoque.ESTORNO, 1)))
                .isInstanceOf(RegraNegocioException.class);
        verifyNoInteractions(repositoryEstoque);
    }

    @Test
    @DisplayName("Estorno aplica o efeito inverso e marca a original como estornada")
    void estornoDeSaida() {
        MovimentacaoEstoque original = new MovimentacaoEstoque(UUID.randomUUID(), produto, TipoMovimentacaoEstoque.SAIDA, 30);
        when(repositoryMovimentacao.buscarParaEstorno(original.getId())).thenReturn(Optional.of(original));

        MovimentacaoEstoqueRespostaDTO estorno = service.estornar(original.getId(), "venda cancelada");

        assertThat(estorno.tipo()).isEqualTo(TipoMovimentacaoEstoque.ESTORNO);
        assertThat(estorno.quantidade()).isEqualTo(30);
        assertThat(estorno.movimentacao_origem_id()).isEqualTo(original.getId());
        assertThat(estoque.getQuantidade()).isEqualTo(80);
        assertThat(original.isEstornada()).isTrue();
    }

    @Test
    @DisplayName("Movimentação já estornada e estorno de estorno são rejeitados")
    void estornoInvalido() {
        MovimentacaoEstoque jaEstornada = new MovimentacaoEstoque(UUID.randomUUID(), produto, TipoMovimentacaoEstoque.ENTRADA, 5);
        jaEstornada.setEstornada(true);
        MovimentacaoEstoque estorno = new MovimentacaoEstoque(UUID.randomUUID(), produto, TipoMovimentacaoEstoque.ESTORNO, -5);
        when(repositoryMovimentacao.buscarParaEstorno(jaEstornada.getId())).thenReturn(Optional.of(jaEstornada));
        when(repositoryMovimentacao.buscarParaEstorno(estorno.getId())).thenReturn(Optional.of(estorno));

        assertThatThrownBy(() -> service.estornar(jaEstornada.getId(), "x")).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.estornar(estorno.getId(), "x")).isInstanceOf(RegraNegocioException.class);
        assertThat(estoque.getQuantidade()).isEqualTo(50);
    }

    @Test
    @DisplayName("Estorno de entrada já consumida é rejeitado para não negativar o estoque")
    void estornoDeEntradaConsumida() {
        MovimentacaoEstoque entrada = new MovimentacaoEstoque(UUID.randomUUID(), produto, TipoMovimentacaoEstoque.ENTRADA, 80);
        when(repositoryMovimentacao.buscarParaEstorno(entrada.getId())).thenReturn(Optional.of(entrada));

        assertThatThrownBy(() -> service.estornar(entrada.getId(), "nota cancelada"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Saldo insuficiente");
        assertThat(entrada.isEstornada()).isFalse();
    }

    @Test
    @DisplayName("Demanda média diária considera as saídas dos últimos 30 dias")
    void demandaMediaDiaria() {
        when(repositoryMovimentacao.somarQuantidade(eq(produto.getId()), eq(TipoMovimentacaoEstoque.SAIDA), any(), any())).thenReturn(120L);
        assertThat(service.demandaMediaDiaria(produto.getId(), java.time.LocalDateTime.now())).isEqualTo(4.0);
        // com demanda 4/dia e prazo 5, o ponto de pedido é 30: uma saída que deixa 25 indica reposição
        assertThat(service.salvar(requisicao(TipoMovimentacaoEstoque.SAIDA, 25)).situacao_estoque()).isEqualTo(SituacaoEstoque.REPOR);
    }
}
