package com.thai.finance.api.finance.api.domain.dtos.RelatorioDTO;

import java.math.BigDecimal;

public record CurvaABCClasseDTO(
        String classe,
        int quantidade_produtos,
        BigDecimal percentual_produtos,
        BigDecimal valor_vendido,
        BigDecimal percentual_valor
) {
}
