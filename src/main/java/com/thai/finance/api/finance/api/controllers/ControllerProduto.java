package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO.ProdutoRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO.ProdutoRespostaDTO;
import com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO.EditarProdutoRequisicaoDTO;
import com.thai.finance.api.finance.api.services.ServiceProduto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/products")
public class ControllerProduto {
    private ServiceProduto serviceProduto;


    public ControllerProduto(ServiceProduto serviceProduto) {
        this.serviceProduto = serviceProduto;
    }

    @PostMapping
    public ResponseEntity<ProdutoRespostaDTO> createProduct(@RequestBody  @Valid ProdutoRequisicaoDTO produtoRequisicaoDTO) {
        ProdutoRespostaDTO productCreated = serviceProduto.createProduct(produtoRequisicaoDTO);

        URI location  = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(productCreated.id())
                .toUri();

        return ResponseEntity.created(location).body(productCreated);
    }

    ;

    @GetMapping
    public ResponseEntity<List<ProdutoRespostaDTO>> getProducts() {
        var allProducts = serviceProduto.getAllProducts();
        return ResponseEntity.ok(allProducts);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") UUID productId) {
        serviceProduto.deleteProductById(productId);
        return ResponseEntity.noContent().build();

    }

    @PutMapping("{id}")
    public ResponseEntity<Void> putProduct(@PathVariable("id") UUID productId, @RequestBody @Valid EditarProdutoRequisicaoDTO editarProdutoRequisicaoDTO) {
        serviceProduto.updateProductById(productId, editarProdutoRequisicaoDTO);
        return ResponseEntity.noContent().build();
    }

    ;

    @GetMapping("search")
    public ResponseEntity<List<ProdutoRespostaDTO>> searchProductByName(
            @RequestParam( value = "name", required = false) String name,
            @RequestParam(value = "skuProduct", required = false) String sku,
            @RequestParam(value ="category", required = false) String category
    ) {
        return ResponseEntity.ok().body(serviceProduto.findByName(name,sku,category));

    }

}
