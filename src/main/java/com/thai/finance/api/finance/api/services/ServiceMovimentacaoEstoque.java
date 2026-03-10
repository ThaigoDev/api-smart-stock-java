package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRespostaDTO;
import com.thai.finance.api.finance.api.domain.entities.Produto;
import com.thai.finance.api.finance.api.domain.entities.Estoque;
import com.thai.finance.api.finance.api.domain.entities.MovimentacaoEstoque;
import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import com.thai.finance.api.finance.api.mapper.MapperMovimentacaoEstoque;
import com.thai.finance.api.finance.api.respository.RepositoryProduto;
import com.thai.finance.api.finance.api.respository.RepositoryMovimentacaoEstoque;
import com.thai.finance.api.finance.api.respository.RepositoryEstoque;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceMovimentacaoEstoque {
    private final RepositoryMovimentacaoEstoque repositoryMovimentacaoEstoque;
    private final RepositoryProduto repositoryProduto;
    private final RepositoryEstoque repositoryEstoque;
    private final MapperMovimentacaoEstoque mapper;

    public MovimentacaoEstoqueRespostaDTO salvar(MovimentacaoEstoqueRequisicaoDTO movimentacaoEstoqueRequisicaoDTO) {
        if (movimentacaoEstoqueRequisicaoDTO.produto_id() == null) {
            throw new IllegalArgumentException("Produto é obrigatório");
        }

        Produto produtoEncontrado = repositoryProduto.findById(movimentacaoEstoqueRequisicaoDTO.produto_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        System.out.println(produtoEncontrado.getNome());
        MovimentacaoEstoque movimentacaoEstoqueEntidade = mapper.paraEntidade(movimentacaoEstoqueRequisicaoDTO);
        movimentacaoEstoqueEntidade.setProduto(produtoEncontrado);
        System.out.println(movimentacaoEstoqueEntidade.getProduto().getNome());
        Estoque estoqueEncontrado = repositoryEstoque.findByProduto(produtoEncontrado).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque não encontrado"));
        System.out.println(estoqueEncontrado.getProduto().getNome());
        if (movimentacaoEstoqueRequisicaoDTO.tipo() == TipoMovimentacaoEstoque.SAIDA) {
            if (movimentacaoEstoqueRequisicaoDTO.quantidade() > (estoqueEncontrado.getQuantidade() - produtoEncontrado.getEstoque_minimo()) ) {
                throw new IllegalArgumentException("Movimentação não autorizada,  a quantidade é maior que o estoque mínimo do produto" + produtoEncontrado.getNome());
            }

            estoqueEncontrado.setQuantidade(estoqueEncontrado.getQuantidade() - movimentacaoEstoqueRequisicaoDTO.quantidade());
            repositoryEstoque.save(estoqueEncontrado);
        } else {
            estoqueEncontrado.setQuantidade(estoqueEncontrado.getQuantidade() + movimentacaoEstoqueRequisicaoDTO.quantidade());
            repositoryEstoque.save(estoqueEncontrado);
        }

        return mapper.paraDTO(repositoryMovimentacaoEstoque.save(movimentacaoEstoqueEntidade));
    }

    public List<MovimentacaoEstoqueRespostaDTO> obter() {
        return repositoryMovimentacaoEstoque.findAll().stream().map(mapper::paraDTO).toList();
    }

    public void remover(UUID movimentacaoEstoque_id) {
        repositoryMovimentacaoEstoque.deleteById(movimentacaoEstoque_id);
    }

    public MovimentacaoEstoqueRespostaDTO atualizar(UUID movimentacaoEstoque_id, MovimentacaoEstoqueRequisicaoDTO movimentacaoEstoqueRequisicaoDTO) {
        MovimentacaoEstoque movimentacaoEstoqueEncontrada = repositoryMovimentacaoEstoque.findById(movimentacaoEstoque_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimentação de Estoque não encontrada"));
        Produto produtoEncontrado = repositoryProduto.findById(movimentacaoEstoqueRequisicaoDTO.produto_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        Estoque estoqueEncontrado = repositoryEstoque.findByProduto(produtoEncontrado).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque não encontrado"));

        if (movimentacaoEstoqueRequisicaoDTO.tipo() == TipoMovimentacaoEstoque.SAIDA) {
            if (movimentacaoEstoqueRequisicaoDTO.quantidade() >= produtoEncontrado.getEstoque_minimo()) {
                throw new IllegalArgumentException("Movimentação não autorizada,  a quantidade é maior que o estoque mínimo do produto" + produtoEncontrado.getNome());
            }
            estoqueEncontrado.setQuantidade(estoqueEncontrado.getQuantidade() - movimentacaoEstoqueRequisicaoDTO.quantidade());
        } else {
            estoqueEncontrado.setQuantidade(estoqueEncontrado.getQuantidade() + movimentacaoEstoqueRequisicaoDTO.quantidade());
        }


        movimentacaoEstoqueEncontrada.setProduto(produtoEncontrado);
        movimentacaoEstoqueEncontrada.setTipo(movimentacaoEstoqueRequisicaoDTO.tipo());
        movimentacaoEstoqueEncontrada.setQuantidade(movimentacaoEstoqueRequisicaoDTO.quantidade());

        repositoryProduto.save(produtoEncontrado);
        repositoryEstoque.save(estoqueEncontrado);

        return mapper.paraDTO(repositoryMovimentacaoEstoque.save(movimentacaoEstoqueEncontrada));
    }
}
