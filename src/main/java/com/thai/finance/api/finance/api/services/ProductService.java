package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.dtos.CreateProductDTO;
import com.thai.finance.api.finance.api.entities.Category;
import com.thai.finance.api.finance.api.entities.Product;
import com.thai.finance.api.finance.api.entities.Stock;
import com.thai.finance.api.finance.api.entities.Supplier;
import com.thai.finance.api.finance.api.respository.CategoryRepository;
import com.thai.finance.api.finance.api.respository.ProductRepository;
import com.thai.finance.api.finance.api.respository.SupplierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final  CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository  = supplierRepository;

    }

    public void createProduct(CreateProductDTO createProductDTO) {
        Category categoryFinded=  categoryRepository.findById(createProductDTO.categoryId()).orElseThrow( ()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Category not found"));
        Supplier supplierFinded  = supplierRepository.findById(createProductDTO.supplier()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Supplier not found"));

        var productEntity  = new Product(
                null,
                createProductDTO.nameProduct(),
                createProductDTO.skuProduct(),
                createProductDTO.minimum_stock(),
                categoryFinded,
                supplierFinded,
                null,
                1000,
                true,
                Instant.now(),
                null
        );
        Stock stock  =  new Stock(null, productEntity, productEntity.getInitialStock());
        productEntity.setStock(stock);
       productRepository.save(productEntity);
    }
}
