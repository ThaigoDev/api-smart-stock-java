package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.PaginaDTO;
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
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceMovimentacaoEstoque {
    // janela usada para estimar a demanda média diária de cada produto
    public static final int JANELA_DEMANDA_DIAS = 30;
    public static final int TAMANHO_MAXIMO_PAGINA = 500;

    private final RepositoryMovimentacaoEstoque repositoryMovimentacaoEstoque;
    private final RepositoryProduto repositoryProduto;
    private final RepositoryEstoque repositoryEstoque;
    private final MapperMovimentacaoEstoque mapper;

    @Transactional
    public MovimentacaoEstoqueRespostaDTO salvar(MovimentacaoEstoqueRequisicaoDTO movimentacaoEstoqueRequisicaoDTO) {
        return registrar(movimentacaoEstoqueRequisicaoDTO, null);
    }

    /**
     * Registra a movimentação na data informada (null = agora, medido após obter o lock). Atualização do saldo e gravação do histórico ocorrem
     * na mesma transação: ou as duas persistem, ou nenhuma (atomicidade).
     */
    @Transactional
    public MovimentacaoEstoqueRespostaDTO registrar(MovimentacaoEstoqueRequisicaoDTO dto, LocalDateTime dataMovimentacao) {
        if (!dto.tipo().permitidoViaApi()) {
            throw new RegraNegocioException("O tipo " + dto.tipo() + " é gerado apenas pelo sistema.");
        }
        Produto produto = repositoryProduto.findById(dto.produto_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        if (!produto.isAtivo()) {
            throw new RegraNegocioException("O produto '" + produto.getNome() + "' está inativo e não aceita movimentações.");
        }
        Estoque estoque = travarEstoque(produto);
        LocalDateTime data = dataMovimentacao != null ? dataMovimentacao : LocalDateTime.now();

        MovimentacaoEstoque movimentacao = mapper.paraEntidade(dto);
        movimentacao.setProduto(produto);
        movimentacao.setData_movimentacao(data);

        MovimentacaoEstoque salva = aplicar(estoque, movimentacao);
        return mapper.paraDTO(salva).comSituacao(situacaoApos(produto, salva.getSaldo_posterior(), data));
    }

    @Transactional
    public MovimentacaoEstoqueRespostaDTO estornar(UUID movimentacaoId, String motivo) {
        return estornar(movimentacaoId, motivo, null);
    }

    /**
     * Desfaz uma movimentação registrando outra com efeito inverso. A original é mantida e marcada como
     * estornada, preservando a trilha de auditoria.
     */
    @Transactional
    public MovimentacaoEstoqueRespostaDTO estornar(UUID movimentacaoId, String motivo, LocalDateTime dataEstorno) {
        MovimentacaoEstoque original = repositoryMovimentacaoEstoque.buscarParaEstorno(movimentacaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimentação de Estoque não encontrada"));
        if (original.getTipo() == TipoMovimentacaoEstoque.ESTORNO) {
            throw new RegraNegocioException("Um estorno não pode ser estornado.");
        }
        if (original.isEstornada()) {
            throw new RegraNegocioException("Esta movimentação já foi estornada.");
        }
        Produto produto = original.getProduto();
        Estoque estoque = travarEstoque(produto);
        LocalDateTime data = dataEstorno != null ? dataEstorno : LocalDateTime.now();

        MovimentacaoEstoque estorno = new MovimentacaoEstoque();
        estorno.setProduto(produto);
        estorno.setTipo(TipoMovimentacaoEstoque.ESTORNO);
        estorno.setQuantidade(-original.efeitoNoSaldo());
        estorno.setMotivo("Estorno de " + original.getTipo() + ": " + motivo);
        estorno.setData_movimentacao(data);
        estorno.setMovimentacao_origem(original);

        MovimentacaoEstoque salvo = aplicar(estoque, estorno);
        original.setEstornada(true);
        repositoryMovimentacaoEstoque.save(original);
        return mapper.paraDTO(salvo).comSituacao(situacaoApos(produto, salvo.getSaldo_posterior(), data));
    }

    @Transactional(readOnly = true)
    public PaginaDTO<MovimentacaoEstoqueRespostaDTO> obter(UUID produtoId, TipoMovimentacaoEstoque tipo, LocalDate inicio, LocalDate fim,
                                                          int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > TAMANHO_MAXIMO_PAGINA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Use 'pagina' >= 0 e 'tamanho' entre 1 e " + TAMANHO_MAXIMO_PAGINA);
        }
        Specification<MovimentacaoEstoque> filtro = (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (produtoId != null) {
                condicoes.add(cb.equal(root.get("produto").get("id"), produtoId));
            }
            if (tipo != null) {
                condicoes.add(cb.equal(root.get("tipo"), tipo));
            }
            if (inicio != null) {
                condicoes.add(cb.greaterThanOrEqualTo(root.get("data_movimentacao"), inicio.atStartOfDay()));
            }
            if (fim != null) {
                condicoes.add(cb.lessThan(root.get("data_movimentacao"), fim.plusDays(1).atStartOfDay()));
            }
            // mais recentes primeiro; a ordenação é feita aqui porque o Sort do Spring Data não aceita
            // nomes de atributo com "_" (interpreta como navegação). A consulta de contagem não é ordenada.
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.orderBy(cb.desc(root.get("data_movimentacao")), cb.desc(root.get("sequencia")));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };
        return PaginaDTO.de(repositoryMovimentacaoEstoque.findAll(filtro, PageRequest.of(pagina, tamanho)), mapper::paraDTO);
    }

    @Transactional(readOnly = true)
    public MovimentacaoEstoqueRespostaDTO obterPorId(UUID movimentacaoId) {
        return mapper.paraDTO(repositoryMovimentacaoEstoque.findById(movimentacaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimentação de Estoque não encontrada")));
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoEstoqueRespostaDTO> extrato(UUID produtoId) {
        if (!repositoryProduto.existsById(produtoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado");
        }
        return repositoryMovimentacaoEstoque.buscarExtrato(produtoId).stream().map(mapper::paraDTO).toList();
    }

    public double demandaMediaDiaria(UUID produtoId, LocalDateTime referencia) {
        long vendido = repositoryMovimentacaoEstoque.somarQuantidade(produtoId, TipoMovimentacaoEstoque.SAIDA,
                referencia.minusDays(JANELA_DEMANDA_DIAS), referencia);
        return (double) vendido / JANELA_DEMANDA_DIAS;
    }

    private Estoque travarEstoque(Produto produto) {
        return repositoryEstoque.buscarParaAtualizacao(produto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque não encontrado para o produto"));
    }

    private MovimentacaoEstoque aplicar(Estoque estoque, MovimentacaoEstoque movimentacao) {
        int saldoAnterior = estoque.getQuantidade();
        int saldoPosterior = saldoAnterior + movimentacao.efeitoNoSaldo();
        if (saldoPosterior < 0) {
            throw new RegraNegocioException(String.format(
                    "Saldo insuficiente para '%s': saldo atual %d, a movimentação de %d unidade(s) deixaria o estoque negativo.",
                    movimentacao.getProduto().getNome(), saldoAnterior, Math.abs(movimentacao.efeitoNoSaldo())));
        }
        long sequencia = (estoque.getUltima_sequencia() == null ? 0 : estoque.getUltima_sequencia()) + 1;
        estoque.setQuantidade(saldoPosterior);
        estoque.setUltima_sequencia(sequencia);
        repositoryEstoque.save(estoque);

        movimentacao.setSaldo_anterior(saldoAnterior);
        movimentacao.setSaldo_posterior(saldoPosterior);
        movimentacao.setSequencia(sequencia);
        return repositoryMovimentacaoEstoque.save(movimentacao);
    }

    private SituacaoEstoque situacaoApos(Produto produto, int saldo, LocalDateTime referencia) {
        return produto.politicaEstoque().classificar(saldo, demandaMediaDiaria(produto.getId(), referencia));
    }
}
