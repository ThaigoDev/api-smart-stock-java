package com.thai.finance.api.finance.api.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "TB_PRODUCTS")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "NAME")
    private String nameProduct;

    @Column(name= "SKU")
    private String skuProduct;

    @Column(name="MINIMUM_STOCK")
    private Integer minimum_stock;

    @ManyToOne
    @JoinColumn(name="CATEGORY_ID",nullable = false)
    private Category categoryId;

    @ManyToOne
    @JoinColumn(name = "SUPPLIER_ID",nullable = false)
    private Supplier supplier;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Stock stock;

    @Column(name = "INITIAL_STOCK")
    private Integer initialStock;

    @Column(name = "STATUS")
    private boolean active = true;

    @CreationTimestamp
    private Instant creationTimestamp;

    @UpdateTimestamp
    private Instant updateTimestamp;

    public Product() {
    }

    public Product(UUID id, String nameProduct, String skuProduct, Integer minimum_stock, Category categoryId, Supplier supplier, Stock stock, Integer  initialStock,boolean active, Instant creationTimestamp, Instant updateTimestamp) {
        this.id = id;
        this.nameProduct = nameProduct;
        this.skuProduct = skuProduct;
        this.minimum_stock = minimum_stock;
        this.categoryId = categoryId;
        this.supplier = supplier;
        this.stock = stock;
        this.initialStock = initialStock;
        this.active = active;
        this.creationTimestamp = creationTimestamp;
        this.updateTimestamp = updateTimestamp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNameProduct() {
        return nameProduct;
    }

    public void setNameProduct(String nameProduct) {
        this.nameProduct = nameProduct;
    }

    public String getSkuProduct() {
        return skuProduct;
    }

    public void setSkuProduct(String skuProduct) {
        this.skuProduct = skuProduct;
    }

    public Integer getMinimum_stock() {
        return minimum_stock;
    }

    public void setMinimum_stock(Integer minimum_stock) {
        this.minimum_stock = minimum_stock;
    }

    public Category getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Category categoryId) {
        this.categoryId = categoryId;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Instant creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Instant getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Instant updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public Integer getInitialStock() {
        return initialStock;
    }

    public void setInitialStock(Integer initialStock) {
        this.initialStock = initialStock;
    }
}
