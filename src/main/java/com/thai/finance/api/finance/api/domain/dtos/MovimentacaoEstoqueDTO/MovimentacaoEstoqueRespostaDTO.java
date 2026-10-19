package com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO;

import com.thai.finance.api.finance.api.domain.enums.SituacaoEstoque;
import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;

import java.time.LocalDateTime;
import java.util.UUID;

public record MovimentacaoEstoqueRespostaDTO(
        UUID id,
        UUID produto_id,
        TipoMovimentacaoEstoque tipo,
        Integer quantidade,
        String motivo,
        LocalDateTime data_movimentacao,
        Integer saldo_anterior,
        Integer saldo_posterior,
        boolean estornada,
        UUID movimentacao_origem_id,
        // situação do produto após a movimentação (preenchida apenas na resposta do registro)
        SituacaoEstoque situacao_estoque
) {
    public MovimentacaoEstoqueRespostaDTO comSituacao(SituacaoEstoque situacao) {
        return new MovimentacaoEstoqueRespostaDTO(id, produto_id, tipo, quantidade, motivo, data_movimentacao,
                saldo_anterior, saldo_posterior, estornada, movimentacao_origem_id, situacao);
    }
}
