package com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO;

import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;

import java.time.LocalDateTime;
import java.util.UUID;

public record MovimentacaoEstoqueRespostaDTO(
        UUID id,
        UUID produto_id,
        TipoMovimentacaoEstoque tipo,
        Integer quantidade,
        String motivo,
        LocalDateTime data_movimentacao
) {
}
