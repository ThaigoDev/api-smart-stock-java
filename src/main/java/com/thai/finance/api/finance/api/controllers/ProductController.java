package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.dtos.productDTO.CreateProductDTO;
import com.thai.finance.api.finance.api.dtos.productDTO.ResponseProductDTO;
import com.thai.finance.api.finance.api.services.ProductService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


}
