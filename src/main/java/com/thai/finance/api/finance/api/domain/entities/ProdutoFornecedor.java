package com.thai.finance.api.finance.api.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Generated;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table
@Data
public class ProdutoFornecedor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String codigoNoFornecedor;

    @Column
    private BigDecimal precoCusto;

    @ManyToOne
    @JoinColumn(name = "produto")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "fornecedor")
    private  Fornecedor fornecedor;

}
