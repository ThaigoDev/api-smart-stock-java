package com.thai.finance.api.finance.api.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name="TB_CATEGORIES")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
}
