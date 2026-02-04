package com.thai.finance.api.finance.api.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "TB_SUPPLIERS")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name= "NAME")
    private String nameSupplier;

}
