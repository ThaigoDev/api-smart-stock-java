package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.EstoqueDTO.EstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.PedidoCompraDTO.ItemPedidoCompraRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.PedidoCompraDTO.ItemPedidoCompraRespostaDTO;
import com.thai.finance.api.finance.api.domain.dtos.PedidoCompraDTO.PedidoCompraRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.PedidoCompraDTO.PedidoCompraRespostaDTO;
import com.thai.finance.api.finance.api.domain.dtos.RelatorioDTO.PosicaoProdutoDTO;
import com.thai.finance.api.finance.api.domain.entities.Fornecedor;
import com.thai.finance.api.finance.api.domain.entities.ItemPedidoCompra;
import com.thai.finance.api.finance.api.domain.entities.PedidoCompra;
import com.thai.finance.api.finance.api.domain.entities.Produto;
import com.thai.finance.api.finance.api.domain.enums.StatusPedidoCompra;
import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import com.thai.finance.api.finance.api.exceptions.RegraNegocioException;
import com.thai.finance.api.finance.api.respository.RepositoryEstoque;
import com.thai.finance.api.finance.api.respository.RepositoryFornecedor;
import com.thai.finance.api.finance.api.respository.RepositoryPedidoCompra;
import com.thai.finance.api.finance.api.respository.RepositoryProduto;
import com.thai.finance.api.finance.api.security.UsuarioAtual;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Fecha o ciclo de reposição: alerta de estoque → pedido de compra → recebimento → entrada no estoque.
 */
@Service
@RequiredArgsConstructor
public class ServicePedidoCompra {

    private final RepositoryPedidoCompra repositoryPedidoCompra;
    private final RepositoryFornecedor repositoryFornecedor;
    private final RepositoryProduto repositoryProduto;
    private final RepositoryEstoque repositoryEstoque;
    private final ServiceEstoque serviceEstoque;
    private final ServiceMovimentacaoEstoque serviceMovimentacaoEstoque;
    private final ServiceRelatorio serviceRelatorio;
    private final UsuarioAtual usuarioAtual;

    @Transactional
    public PedidoCompraRespostaDTO criar(PedidoCompraRequisicaoDTO dto) {
        Fornecedor fornecedor = repositoryFornecedor.findById(dto.fornecedor_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado"));
        PedidoCompra pedido = novoPedido(fornecedor, dto.observacao());

        Set<UUID> produtosIncluidos = new HashSet<>();
        for (ItemPedidoCompraRequisicaoDTO item : dto.itens()) {
            Produto produto = repositoryProduto.findById(item.produto_id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
            if (!produtosIncluidos.add(produto.getId())) {
                throw new RegraNegocioException("O produto '" + produto.getNome() + "' aparece mais de uma vez no pedido.");
            }
            if (!produto.getFornecedor().getId().equals(fornecedor.getId())) {
                throw new RegraNegocioException("O produto '" + produto.getNome() + "' é fornecido por '"
                        + produto.getFornecedor().getNome() + "', não por '" + fornecedor.getNome() + "'.");
            }
            if (!produto.isAtivo()) {
                throw new RegraNegocioException("O produto '" + produto.getNome() + "' está inativo.");
            }
            pedido.adicionarItem(produto, item.quantidade());
        }
        return paraDTO(repositoryPedidoCompra.saveAndFlush(pedido));
    }

    /**
     * Gera um pedido por fornecedor com os produtos que atingiram o ponto de pedido, na quantidade sugerida
     * para recompor o estoque máximo. Produtos que já estão em um pedido aberto são ignorados, para não comprar duas vezes.
     */
    @Transactional
    public List<PedidoCompraRespostaDTO> gerarSugestoes(LocalDate dataReferencia) {
        LocalDate referencia = dataReferencia != null ? dataReferencia : LocalDate.now();
        Set<UUID> jaPedidos = repositoryPedidoCompra.produtosEmPedidosAbertos();

        Map<UUID, List<PosicaoProdutoDTO>> porFornecedor = serviceRelatorio.posicao(referencia).stream()
                .filter(p -> p.ativo() && p.possui_estoque() && p.situacao() != null && p.situacao().exigeReposicao())
                .filter(p -> p.sugestao_compra() > 0 && !jaPedidos.contains(p.produto_id()))
                .collect(Collectors.groupingBy(PosicaoProdutoDTO::fornecedor_id, LinkedHashMap::new, Collectors.toList()));

        String observacao = "Gerado automaticamente a partir dos alertas de reposição de "
                + referencia.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        List<PedidoCompraRespostaDTO> criados = new ArrayList<>();
        porFornecedor.forEach((fornecedorId, itens) -> {
            Fornecedor fornecedor = repositoryFornecedor.getReferenceById(fornecedorId);
            PedidoCompra pedido = novoPedido(fornecedor, observacao);
            for (PosicaoProdutoDTO item : itens) {
                pedido.adicionarItem(repositoryProduto.getReferenceById(item.produto_id()), item.sugestao_compra());
            }
            criados.add(paraDTO(repositoryPedidoCompra.saveAndFlush(pedido)));
        });
        return criados;
    }

    /** Registra a chegada da mercadoria: uma ENTRADA por item, na mesma transação do pedido. */
    @Transactional
    public PedidoCompraRespostaDTO receber(UUID pedidoId) {
        PedidoCompra pedido = travar(pedidoId);
        exigirAberto(pedido, "receber");
        String motivo = "Recebimento do pedido de compra " + pedido.codigo() + " - " + pedido.getFornecedor().getNome();
        for (ItemPedidoCompra item : pedido.getItens()) {
            Produto produto = item.getProduto();
            if (repositoryEstoque.findByProduto(produto).isEmpty()) {
                // primeira compra de um produto recém-cadastrado: abre o estoque zerado antes da entrada
                serviceEstoque.salvar(new EstoqueRequisicaoDTO(produto.getId(), 0));
            }
            serviceMovimentacaoEstoque.registrar(new MovimentacaoEstoqueRequisicaoDTO(
                    produto.getId(), TipoMovimentacaoEstoque.ENTRADA, item.getQuantidade(), motivo), null);
        }
        pedido.setStatus(StatusPedidoCompra.RECEBIDO);
        pedido.setRecebido_em(LocalDateTime.now());
        pedido.setRecebido_por(usuarioAtual.identificador());
        return paraDTO(pedido);
    }

    @Transactional
    public PedidoCompraRespostaDTO cancelar(UUID pedidoId) {
        PedidoCompra pedido = travar(pedidoId);
        exigirAberto(pedido, "cancelar");
        pedido.setStatus(StatusPedidoCompra.CANCELADO);
        pedido.setCancelado_em(LocalDateTime.now());
        return paraDTO(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoCompraRespostaDTO> listar(StatusPedidoCompra status) {
        List<PedidoCompra> pedidos = status == null
                ? repositoryPedidoCompra.buscarTodosComItens()
                : repositoryPedidoCompra.buscarPorStatus(status);
        return pedidos.stream().map(this::paraDTO).toList();
    }

    @Transactional(readOnly = true)
    public PedidoCompraRespostaDTO obter(UUID pedidoId) {
        return paraDTO(repositoryPedidoCompra.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido de compra não encontrado")));
    }

    private PedidoCompra novoPedido(Fornecedor fornecedor, String observacao) {
        PedidoCompra pedido = new PedidoCompra();
        pedido.setFornecedor(fornecedor);
        pedido.setObservacao(observacao);
        pedido.setCriado_por(usuarioAtual.identificador());
        return pedido;
    }

    private PedidoCompra travar(UUID pedidoId) {
        return repositoryPedidoCompra.buscarParaAtualizacao(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido de compra não encontrado"));
    }

    private static void exigirAberto(PedidoCompra pedido, String acao) {
        if (pedido.getStatus() != StatusPedidoCompra.ABERTO) {
            throw new RegraNegocioException("Não é possível " + acao + " o pedido " + pedido.codigo()
                    + ": ele está " + pedido.getStatus().name().toLowerCase() + ".");
        }
    }

    private PedidoCompraRespostaDTO paraDTO(PedidoCompra pedido) {
        List<ItemPedidoCompraRespostaDTO> itens = pedido.getItens().stream()
                .map(i -> new ItemPedidoCompraRespostaDTO(i.getProduto().getId(), i.getProduto().getNome(),
                        i.getProduto().getSku(), i.getQuantidade(),
                        i.getProduto().precoEmReais().multiply(BigDecimal.valueOf(i.getQuantidade()))))
                .sorted(Comparator.comparing(ItemPedidoCompraRespostaDTO::produto, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return new PedidoCompraRespostaDTO(pedido.getId(), pedido.codigo(), pedido.getFornecedor().getId(),
                pedido.getFornecedor().getNome(), pedido.getStatus(), pedido.getObservacao(), pedido.getCriado_por(),
                pedido.getCriado_em(), pedido.getRecebido_por(), pedido.getRecebido_em(), pedido.getCancelado_em(),
                itens, itens.stream().mapToInt(ItemPedidoCompraRespostaDTO::quantidade).sum(),
                itens.stream().map(ItemPedidoCompraRespostaDTO::valor_estimado).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
