package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.dtos.CreateProductDTO;
import com.thai.finance.api.finance.api.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/products")
public class ProductController {
    private ProductService productService;

    public ProductController(ProductService productService) {
            this.productService = productService;
    }

    @PostMapping("/")
    public ResponseEntity<Void> createProduct(@RequestBody CreateProductDTO createProductDTO) {
        productService.createProduct(createProductDTO);
    }


}
