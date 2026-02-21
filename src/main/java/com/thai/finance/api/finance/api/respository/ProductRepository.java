package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface  ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByNameProduct(String name);
}