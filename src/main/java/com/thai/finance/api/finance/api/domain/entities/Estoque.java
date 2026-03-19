package com.thai.finance.api.finance.api.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Estoque {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private BigDecimal quantidadeAtual;

    @Column
    private BigDecimal quantidadeMinima;

    @Column
    private BigDecimal quantidadeMaxima;

    @Column
    private String localizacao;

    @OneToOne
    @JoinColumn(nullable = true)
    private Produto produto;

    @CreatedDate
    private LocalDateTime criado_em;

    @LastModifiedDate
    private LocalDateTime ultima_atualizacao;

    public Estoque(UUID id, Produto produto, BigDecimal quantidadeAtual, BigDecimal quantidadeMinima, BigDecimal quantidadeMaxima, String localizacao) {
        this.id = id;
        this.quantidadeAtual = quantidadeAtual;
        this.quantidadeMinima = quantidadeMinima;
        this.quantidadeMaxima = quantidadeMaxima;
        this.localizacao = localizacao;
        this.produto = produto;

    }
}
