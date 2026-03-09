package com.thai.finance.api.finance.api.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String nome;

    @Column
    private String sku;

    private Integer estoque_minimo;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Categoria categoria_id;

    private BigInteger preco;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Fornecedor fornecedor_id;

   @Column
    private boolean ativo = true;


}
