package com.thai.finance.api.finance.api.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "TB_PRODUCTS")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @OneToOne(mappedBy = "product",fetch = FetchType.LAZY)
    private Stock stock;

    @Column(name = "INITIAL_STOCK")
    private Integer initialStock;

    @Column(name = "STATUS")
    private boolean active = true;

    @CreationTimestamp
    private Instant creationTimestamp;

    @UpdateTimestamp
    private Instant updateTimestamp;


}
