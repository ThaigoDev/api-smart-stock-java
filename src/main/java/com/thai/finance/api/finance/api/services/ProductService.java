package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.productDTO.CreateProductDTO;
import com.thai.finance.api.finance.api.domain.dtos.productDTO.ResponseProductDTO;
import com.thai.finance.api.finance.api.domain.dtos.productDTO.UpdateProductDTO;
import com.thai.finance.api.finance.api.domain.entities.Category;
import com.thai.finance.api.finance.api.domain.entities.Product;
import com.thai.finance.api.finance.api.domain.entities.Stock;
import com.thai.finance.api.finance.api.domain.entities.Supplier;
import com.thai.finance.api.finance.api.mapper.ProductMapper;
import com.thai.finance.api.finance.api.respository.CategoryRepository;
import com.thai.finance.api.finance.api.respository.ProductRepository;
import com.thai.finance.api.finance.api.respository.StockRespository;
import com.thai.finance.api.finance.api.respository.SupplierRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final SupplierRepository supplierRepository;
    private final StockRespository stockRespository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, SupplierRepository supplierRepository, ProductMapper productMapper, StockRespository stockRespository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.productMapper = productMapper;
        this.stockRespository = stockRespository;
    }

    public ResponseProductDTO createProduct(CreateProductDTO createProductDTO) {
        Category categoryFinded = categoryRepository.findById(createProductDTO.categoryId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        Supplier supplierFinded = supplierRepository.findById(createProductDTO.supplier()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));

        var productEntity = new Product(
                null,
                createProductDTO.nameProduct(),
                createProductDTO.skuProduct(),
                createProductDTO.minimum_stock(),
                categoryFinded,
                supplierFinded,
                null,
                createProductDTO.initialStock(),
                createProductDTO.active(),
                Instant.now(),
                null
        );

        var productSaved = productRepository.save(productEntity);
        Stock stock = new Stock(null, productSaved, productEntity.getInitialStock());
        productEntity.setStock(stock);

        return  productMapper.EntityResponseToDTO(productSaved);



    }

    public List<ResponseProductDTO> getAllProducts() {
        var allProductsFinded = productRepository.findAll().stream().map(productMapper::EntityResponseToDTO).toList();
        return allProductsFinded;
    }

    public void updateProductById(UUID productId, UpdateProductDTO updateProductDTO) {
        var productExist = productRepository.findById(productId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Category category = categoryRepository.findById(updateProductDTO.categoryId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        Supplier supplier = supplierRepository.findById(updateProductDTO.supplier()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));

        Stock stock = stockRespository.findById(updateProductDTO.stock()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found"));

        productExist.setNameProduct(updateProductDTO.nameProduct());
        productExist.setSkuProduct(updateProductDTO.skuProduct());
        productExist.setMinimum_stock(updateProductDTO.minimum_stock());
        productExist.setCategoryId(category);
        productExist.setSupplier(supplier);
        productExist.setStock(stock);
        productExist.setInitialStock(updateProductDTO.initialStock());
        productExist.setActive(updateProductDTO.active());
        productRepository.save(productExist);
    }

    public void deleteProductById(UUID productId) {
        Product productExist = productRepository.findById(productId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        productRepository.deleteById(productExist.getId());

    }

    public List<ResponseProductDTO> findByName(String name,String sku, String category) {
        var ProductExample  = new Product();
        ProductExample.setNameProduct(name);
        Category  categoryFinded = categoryRepository.findById(UUID.fromString(category)).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        ProductExample.setCategoryId(categoryFinded);
        ProductExample.setSkuProduct(sku);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase().withIgnoreNullValues().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Product>  productExample = Example.of(ProductExample,matcher);

        return productRepository.findAll(productExample).stream().map(productMapper::EntityResponseToDTO).toList();
    }
}
