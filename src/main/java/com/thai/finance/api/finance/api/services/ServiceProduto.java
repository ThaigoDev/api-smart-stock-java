package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO.ProdutoRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO.ProdutoRespostaDTO;
import com.thai.finance.api.finance.api.domain.entities.Categoria;
import com.thai.finance.api.finance.api.domain.entities.Produto;
import com.thai.finance.api.finance.api.domain.entities.Fornecedor;
import com.thai.finance.api.finance.api.mapper.MapperProduto;
import com.thai.finance.api.finance.api.respository.RepositoryCategoria;
import com.thai.finance.api.finance.api.respository.RepositoryProduto;
import com.thai.finance.api.finance.api.respository.RepositoryFornecedor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceProduto {
    private final RepositoryProduto repositoryProduto;
    private final RepositoryCategoria repositoryCategoria;
    private final RepositoryFornecedor repositoryFornecedor;
    private final MapperProduto mapper;


    public ProdutoRespostaDTO salvar(ProdutoRequisicaoDTO produtoRequisicaoDTO) {

        Categoria categoriaEncontrada = repositoryCategoria.findById(produtoRequisicaoDTO.categoria_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
        Fornecedor fornecedorEncontrado = repositoryFornecedor.findById(produtoRequisicaoDTO.fornecedor_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado"));

        var EntidadeProduto = mapper.paraEntidade(produtoRequisicaoDTO);
        EntidadeProduto.setFornecedor(fornecedorEncontrado);
        EntidadeProduto.setCategoria(categoriaEncontrada);
        EntidadeProduto.setAtivo(produtoRequisicaoDTO.ativo() == null || produtoRequisicaoDTO.ativo());
        var produtoSalvo = repositoryProduto.save(EntidadeProduto);

        return mapper.paraDTO(produtoSalvo);

    }

    public List<ProdutoRespostaDTO> obter() {
        return repositoryProduto.findAll().stream().map(mapper::paraDTO).toList();
    }

    public void atualizar(UUID produto_id, ProdutoRequisicaoDTO produtoRequisicaoDTO) {

        var produtoEcontrado = repositoryProduto.findById(produto_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        Categoria categoriaEncontrada = repositoryCategoria.findById(produtoRequisicaoDTO.categoria_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
        Fornecedor fornecedorEncontrado = repositoryFornecedor.findById(produtoRequisicaoDTO.fornecedor_id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado"));

        produtoEcontrado.setNome(produtoRequisicaoDTO.nome());
        produtoEcontrado.setSku(produtoRequisicaoDTO.sku());
        produtoEcontrado.setEstoque_minimo(produtoRequisicaoDTO.estoque_minimo());
        produtoEcontrado.setEstoque_maximo(produtoRequisicaoDTO.estoque_maximo());
        produtoEcontrado.setPrazo_reposicao_dias(produtoRequisicaoDTO.prazo_reposicao_dias());
        produtoEcontrado.setPreco(produtoRequisicaoDTO.preco());
        produtoEcontrado.setCategoria(categoriaEncontrada);
        produtoEcontrado.setFornecedor(fornecedorEncontrado);
        if (produtoRequisicaoDTO.ativo() != null) {
            produtoEcontrado.setAtivo(produtoRequisicaoDTO.ativo());
        }
        repositoryProduto.save(produtoEcontrado);
    }

    public void remover(UUID productId) {
        Produto produtoExist = repositoryProduto.findById(productId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        // produto com estoque/histórico viola a chave estrangeira ao confirmar: tratado como 409 no manipulador global
        repositoryProduto.deleteById(produtoExist.getId());
    }

    public List<ProdutoRespostaDTO> buscar(String nome, String sku, String categoria) {
        var ExemploProduto = new Produto();
        ExemploProduto.setNome(nome);
        if (categoria != null && !categoria.isBlank()) {
            UUID categoriaId;
            try {
                categoriaId = UUID.fromString(categoria);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O parâmetro 'categoria' deve ser um UUID válido");
            }
            Categoria categoriaEncontrada = repositoryCategoria.findById(categoriaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
            ExemploProduto.setCategoria(categoriaEncontrada);
        }
        ExemploProduto.setSku(sku);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase().withIgnoreNullValues().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Produto> produtoExemplo = Example.of(ExemploProduto, matcher);

        return repositoryProduto.findAll(produtoExemplo).stream().map(mapper::paraDTO).toList();
    }

    public ProdutoRespostaDTO obterPorId(UUID productId) {
       var produtoEncontrado  =   repositoryProduto.findById(productId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
       return mapper.paraDTO(produtoEncontrado);
    }
}
