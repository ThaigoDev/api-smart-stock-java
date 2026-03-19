package com.thai.finance.api.finance.api.domain.entities;

import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy  = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TipoMovimentacaoEstoque tipo;

    @Column
    private BigDecimal quantidade;

    @Column
    private String motivo;

    @Column
    private String documentoReferencia;

    @ManyToOne
    @JoinColumn(name = "estoque")
    private  Estoque estoque;

    @CreatedDate
    private LocalDateTime criado_em;

    @LastModifiedDate
    private LocalDateTime data_movimentacao;

    public MovimentacaoEstoque() {
    }

    public MovimentacaoEstoque(UUID id, TipoMovimentacaoEstoque tipo, BigDecimal quantidade, String motivo, Estoque estoque) {
        this.id = id;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.motivo = motivo;
        this.estoque = estoque;
    }


}
