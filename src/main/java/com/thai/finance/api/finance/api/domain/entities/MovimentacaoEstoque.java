package com.thai.finance.api.finance.api.domain.entities;

import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro imutável de uma alteração de saldo (livro-razão / kardex).
 * Correções são feitas por estorno, nunca por edição ou exclusão, preservando a trilha de auditoria.
 */
@Entity
@Table(
        indexes = @Index(name = "idx_movimentacao_produto_data", columnList = "produto_id, data_movimentacao"),
        // segunda barreira contra atualização concorrente: duas movimentações nunca ocupam a mesma posição no kardex
        uniqueConstraints = @UniqueConstraint(name = "uk_movimentacao_produto_sequencia", columnNames = {"produto_id", "sequencia"}))
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

    // positiva para ENTRADA/SAIDA/DEVOLUCAO; com sinal para AJUSTE e ESTORNO
    @Column
    private Integer quantidade;

    @Column
    private String motivo;

    // criado_em continua sendo o timestamp de auditoria (quando o registro foi de fato persistido no banco)
    @CreatedDate
    private LocalDateTime criado_em;

    // data em que a movimentação efetivamente ocorreu
    @Column
    private LocalDateTime data_movimentacao;

    // saldo do produto imediatamente antes e depois desta movimentação
    private Integer saldo_anterior;

    private Integer saldo_posterior;

    // posição da movimentação no histórico do produto (1, 2, 3...), atribuída sob o lock do estoque
    private Long sequencia;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean estornada = false;

    // preenchido apenas em movimentações do tipo ESTORNO
    @ManyToOne
    private MovimentacaoEstoque movimentacao_origem;

    public MovimentacaoEstoque() {
    }

    public MovimentacaoEstoque(UUID id, Produto produto, TipoMovimentacaoEstoque tipo, Integer quantidade) {
        this.id = id;
        this.produto = produto;
        this.tipo = tipo;
        this.quantidade = quantidade;
    }

    public int efeitoNoSaldo() {
        return tipo.efeitoNoSaldo(quantidade);
    }

}
