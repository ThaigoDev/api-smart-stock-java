package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.EstoqueDTO.EstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.EstoqueDTO.EstoqueRespostaDTO;
import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.entities.Estoque;
import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import com.thai.finance.api.finance.api.mapper.MapperEstoque;
import com.thai.finance.api.finance.api.respository.RepositoryEstoque;
import com.thai.finance.api.finance.api.respository.RepositoryProduto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceEstoque {
    public static final String MOTIVO_SALDO_INICIAL = "Saldo inicial de implantação";

    private final RepositoryEstoque repositoryEstoque;
    private final RepositoryProduto repositoryProduto;
    private final ServiceMovimentacaoEstoque serviceMovimentacaoEstoque;
    private final MapperEstoque mapper;

    @Transactional
    public EstoqueRespostaDTO salvar(EstoqueRequisicaoDTO estoqueRequisicaoDTO) {
        return salvar(estoqueRequisicaoDTO, LocalDateTime.now());
    }

    /**
     * Abre o estoque do produto com saldo zero e registra o saldo inicial como uma ENTRADA,
     * de modo que todo o saldo tenha origem rastreável no histórico de movimentações.
     */
    @Transactional
    public EstoqueRespostaDTO salvar(EstoqueRequisicaoDTO estoqueRequisicaoDTO, LocalDateTime dataImplantacao) {
        var produtoEncontrado = repositoryProduto.findById(estoqueRequisicaoDTO.produto_id()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Produto não encontrado"));
        if (repositoryEstoque.findByProduto(produtoEncontrado).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O produto já possui estoque cadastrado; altere o saldo por meio de movimentações");
        }
        Estoque estoqueEtidade = new Estoque();
        estoqueEtidade.setProduto(produtoEncontrado);
        estoqueEtidade.setQuantidade(0);
        estoqueEtidade = repositoryEstoque.save(estoqueEtidade);

        if (estoqueRequisicaoDTO.quantidade() > 0) {
            serviceMovimentacaoEstoque.registrar(new MovimentacaoEstoqueRequisicaoDTO(produtoEncontrado.getId(),
                    TipoMovimentacaoEstoque.ENTRADA, estoqueRequisicaoDTO.quantidade(), MOTIVO_SALDO_INICIAL), dataImplantacao);
        }
        return mapper.paraDTO(estoqueEtidade);
    }

    @Transactional(readOnly = true)
    public List<EstoqueRespostaDTO> obter() {
        return repositoryEstoque.findAll().stream().map(mapper::paraDTO).toList();
    }

    @Transactional(readOnly = true)
    public EstoqueRespostaDTO obterPorId(UUID estoque_id) {
        return mapper.paraDTO(repositoryEstoque.findById(estoque_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque não encontrado")));
    }
}
