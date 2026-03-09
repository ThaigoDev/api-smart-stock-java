package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO.ProdutoRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO.ProdutoRespostaDTO;
import com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO.EditarProdutoRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.entities.Categoria;
import com.thai.finance.api.finance.api.domain.entities.Estoque;
import com.thai.finance.api.finance.api.domain.entities.Produto;
import com.thai.finance.api.finance.api.domain.entities.Fornecedor;
import com.thai.finance.api.finance.api.mapper.MapperProduct;
import com.thai.finance.api.finance.api.respository.RepositoryCategoria;
import com.thai.finance.api.finance.api.respository.RepositoryProduto;
import com.thai.finance.api.finance.api.respository.RepositoryEstoque;
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
    private final RepositoryEstoque repositoryEstoque;
    private final MapperProduct mapper;


    public ProdutoRespostaDTO createProduct(ProdutoRequisicaoDTO produtoRequisicaoDTO) {
        Categoria categoriaFinded = repositoryCategoria.findById(produtoRequisicaoDTO.categoryId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        Fornecedor fornecedorFinded = repositoryFornecedor.findById(produtoRequisicaoDTO.supplierId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));

        var ProductEntity = mapper.dtoToEntity(produtoRequisicaoDTO);
        ProductEntity.setSupplier(fornecedorFinded);
        ProductEntity.setCategoryId(categoriaFinded);
        var productSaved = repositoryProduto.save(ProductEntity);
        Estoque estoque = new Estoque(null, productSaved, ProductEntity.getInitialStock());
        ProductEntity.setStock(estoque);

        return mapper.entityToDTO(productSaved);

    }

    public List<ProdutoRespostaDTO> getAllProducts() {
        return repositoryProduto.findAll().stream().map(mapper::entityToDTO).toList();
    }

    public void updateProductById(UUID productId, EditarProdutoRequisicaoDTO editarProdutoRequisicaoDTO) {
        var productExist = repositoryProduto.findById(productId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Categoria categoria = repositoryCategoria.findById(editarProdutoRequisicaoDTO.categoryId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        Fornecedor fornecedor = repositoryFornecedor.findById(editarProdutoRequisicaoDTO.supplier()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));

        Estoque estoque = repositoryEstoque.findById(editarProdutoRequisicaoDTO.stock()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found"));

        productExist.setNameProduct(editarProdutoRequisicaoDTO.nameProduct());
        productExist.setSkuProduct(editarProdutoRequisicaoDTO.skuProduct());
        productExist.setMinimum_stock(editarProdutoRequisicaoDTO.minimum_stock());
        productExist.setCategoryId(categoria);
        productExist.setSupplier(fornecedor);
        productExist.setStock(estoque);
        productExist.setInitialStock(editarProdutoRequisicaoDTO.initialStock());
        productExist.setActive(editarProdutoRequisicaoDTO.active());
        repositoryProduto.save(productExist);
    }

    public void deleteProductById(UUID productId) {
        Produto produtoExist = repositoryProduto.findById(productId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        repositoryProduto.deleteById(produtoExist.getId());

    }

    public List<ProdutoRespostaDTO> findByName(String name, String sku, String category) {
        var ProductExample = new Produto();
        ProductExample.setNameProduct(name);
        Categoria categoriaFinded = repositoryCategoria.findById(UUID.fromString(category)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        ProductExample.setCategoryId(categoriaFinded);
        ProductExample.setSkuProduct(sku);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase().withIgnoreNullValues().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Produto> productExample = Example.of(ProductExample, matcher);

        return repositoryProduto.findAll(productExample).stream().map(mapper::entityToDTO).toList();
    }
}
