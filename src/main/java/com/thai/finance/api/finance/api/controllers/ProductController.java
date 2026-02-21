package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.dtos.productDTO.CreateProductDTO;
import com.thai.finance.api.finance.api.dtos.productDTO.ResponseProductDTO;
import com.thai.finance.api.finance.api.dtos.productDTO.UpdateProductDTO;
import com.thai.finance.api.finance.api.services.ProductService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Void> createProduct(@RequestBody CreateProductDTO createProductDTO) {
        productService.createProduct(createProductDTO);
        return ResponseEntity.ok().build();
    };

    @GetMapping
    public ResponseEntity<List<ResponseProductDTO>> getProducts () {
        var allProducts = productService.getAllProducts();
        return ResponseEntity.ok(allProducts);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") UUID productId) {
        productService.deleteProductById(productId);
        return  ResponseEntity.noContent().build();

    }
    @PutMapping("/{id}")
    public ResponseEntity<Void> putProduct(@PathVariable("id") UUID productId, @RequestBody UpdateProductDTO updateProductDTO) {
        productService.updateProductById(productId, updateProductDTO);
        return ResponseEntity.noContent().build();
   };
    @GetMapping("/search")
    public ResponseEntity<List<ResponseProductDTO>> searchProductByName(@RequestParam("name") String name ) {
        return  ResponseEntity.ok().body(productService.findByName(name));

    }

}
