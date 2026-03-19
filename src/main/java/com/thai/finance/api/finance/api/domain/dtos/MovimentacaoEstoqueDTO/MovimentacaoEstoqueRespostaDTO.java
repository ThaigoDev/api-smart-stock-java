package com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO;

import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MovimentacaoEstoqueRespostaDTO(

        UUID id,
        UUID estoque_id,
        TipoMovimentacaoEstoque tipo,
        BigDecimal quantidade,
        String motivo,
        String documentoReferencia,
        LocalDateTime criado_em,
        LocalDateTime data_movimentacao
) {
}