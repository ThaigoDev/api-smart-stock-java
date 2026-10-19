package com.thai.finance.api.finance.api.domain.dtos.RelatorioDTO;

import java.math.BigDecimal;
import java.util.UUID;

public record CurvaABCItemDTO(
        UUID produto_id,
        String nome,
        long quantidade_vendida,
        BigDecimal valor_vendido,
        BigDecimal percentual_valor,
        BigDecimal percentual_acumulado,
        String classe
) {
}
