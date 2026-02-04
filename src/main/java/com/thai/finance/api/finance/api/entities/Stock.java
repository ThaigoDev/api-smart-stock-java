package com.thai.finance.api.finance.api.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "TB_STOCK")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name="PRODUCT_ID", nullable = false, unique = true)
    private Product product;

    @Column(name= "QUANTITY")
    private Integer quantityProduct;
}
