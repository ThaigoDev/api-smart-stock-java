package com.thai.finance.api.finance.api.domain.entities;

import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
    private Produto produto;

    @Enumerated(EnumType.STRING)
    private TipoMovimentacaoEstoque tipo;

    @Column
    private Integer quantidade;

    @Column
    private String motivo;

    // criado_em continua sendo o timestamp de auditoria (quando o registro foi de fato persistido no banco)
    @CreatedDate
    private LocalDateTime criado_em;

    // data_movimentacao agora é um campo comum, definido explicitamente (data em que a movimentação
    // efetivamente ocorreu). Antes estava com @LastModifiedDate, que sobrescrevia o valor para "agora"
    // toda vez que a entidade era salva/atualizada, impedindo registrar histórico com datas passadas.
    @Column
    private LocalDateTime data_movimentacao;

    public MovimentacaoEstoque() {
    }

    public MovimentacaoEstoque(UUID id, Produto produto, TipoMovimentacaoEstoque tipo, Integer quantidade) {
        this.id = id;
        this.produto = produto;
        this.tipo = tipo;
        this.quantidade = quantidade;
    }

}