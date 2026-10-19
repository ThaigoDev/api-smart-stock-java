package com.thai.finance.api.finance.api.domain.dtos.RelatorioDTO;

import java.time.LocalDate;
import java.util.List;

public record GiroDTO(
        LocalDate inicio,
        LocalDate fim,
        long dias,
        Double giro_medio,
        List<GiroProdutoDTO> itens
) {
}
