package com.thai.finance.api.finance.api.entities;

import jakarta.persistence.*;

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



}
