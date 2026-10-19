package com.thai.finance.api.finance.api.domain.dtos.RelatorioDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CurvaABCDTO(
        LocalDate inicio,
        LocalDate fim,
        BigDecimal valor_total_vendido,
        List<CurvaABCClasseDTO> resumo_por_classe,
        List<CurvaABCItemDTO> itens
) {
}
