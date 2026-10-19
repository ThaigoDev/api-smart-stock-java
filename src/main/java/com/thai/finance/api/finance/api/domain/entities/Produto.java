package com.thai.finance.api.finance.api.domain.entities;

import com.thai.finance.api.finance.api.domain.indicadores.PoliticaEstoque;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
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

    // acima deste saldo o produto é considerado em superestoque (opcional)
    private Integer estoque_maximo;

    // lead time do fornecedor, usado no cálculo do ponto de pedido (opcional, padrão 7 dias)
    private Integer prazo_reposicao_dias;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Categoria categoria;

    // preço em centavos
    private BigInteger preco;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Fornecedor fornecedor;

   @Column
    private boolean ativo = true;

    public PoliticaEstoque politicaEstoque() {
        return new PoliticaEstoque(
                estoque_minimo == null ? 0 : estoque_minimo,
                estoque_maximo,
                prazo_reposicao_dias == null ? PoliticaEstoque.PRAZO_REPOSICAO_PADRAO_DIAS : prazo_reposicao_dias);
    }

    public BigDecimal precoEmReais() {
        return preco == null ? BigDecimal.ZERO : new BigDecimal(preco).movePointLeft(2);
    }
}
