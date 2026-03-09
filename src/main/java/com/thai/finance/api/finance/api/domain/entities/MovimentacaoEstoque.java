package com.thai.finance.api.finance.api.domain.entities;

import com.thai.finance.api.finance.api.domain.enums.MovementType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    @ManyToOne
    @JoinColumn(nullable = false)
    private Produto produto_id;

    @Enumerated(EnumType.STRING)
    private MovementType tipo;

    @Column
    private Integer quantidade;

    @CreatedDate
    private LocalDateTime criado_em;

    @LastModifiedDate
    private Instant data_movimentacao;

    @Column
    private String motivo;

    public MovimentacaoEstoque() {
    }

    public MovimentacaoEstoque(UUID id, Produto produto, MovementType tipo, Integer quantidade) {
        this.id = id;
        this.produto_id = produto;
        this.tipo = tipo;
        this.quantidade = quantidade;
    }


}
