package com.thai.finance.api.finance.api.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;
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

    @Column(unique = true)
    private String sku;

    @Column
    private String unidadeMedida;

    @Column
    private BigInteger precoCusto;

    @Column
    private BigInteger precoVenda;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Categoria categoria;

    @OneToOne(mappedBy = "produto")
    @JoinColumn(name = "estoque")
    private Estoque estoque;

    @OneToMany(mappedBy = "produto")
    @JoinColumn(nullable = false)
    private List<ProdutoFornecedor> fornecedores;

   @Column
    private boolean ativo = true;


}
