package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.productDTO.CreateProductDTO;
import com.thai.finance.api.finance.api.domain.dtos.productDTO.ResponseProductDTO;
import com.thai.finance.api.finance.api.domain.dtos.productDTO.UpdateProductDTO;
import com.thai.finance.api.finance.api.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/products")
public class ProductController {
    private ProductService productService;


    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ResponseProductDTO> createProduct(@RequestBody  @Valid  CreateProductDTO createProductDTO) {
        ResponseProductDTO productCreated = productService.createProduct(createProductDTO);

        URI location  = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(productCreated.id())
                .toUri();

        return ResponseEntity.created(location).body(productCreated);
    }

    ;

    @GetMapping
    public ResponseEntity<List<ResponseProductDTO>> getProducts() {
        var allProducts = productService.getAllProducts();
        return ResponseEntity.ok(allProducts);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") UUID productId) {
        productService.deleteProductById(productId);
        return ResponseEntity.noContent().build();

    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> putProduct(@PathVariable("id") UUID productId, @RequestBody @Valid UpdateProductDTO updateProductDTO) {
        productService.updateProductById(productId, updateProductDTO);
        return ResponseEntity.noContent().build();
    }

    ;

    @GetMapping("/search")
    public ResponseEntity<List<ResponseProductDTO>> searchProductByName(@RequestParam("name") String name) {
        return ResponseEntity.ok().body(productService.findByName(name));

    }

}
