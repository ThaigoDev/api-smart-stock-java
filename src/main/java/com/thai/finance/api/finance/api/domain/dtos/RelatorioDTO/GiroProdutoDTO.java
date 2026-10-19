package com.thai.finance.api.finance.api.domain.dtos.RelatorioDTO;

import java.util.UUID;

public record GiroProdutoDTO(
        UUID produto_id,
        String nome,
        String categoria,
        long quantidade_vendida,
        double estoque_medio,
        Double giro,
        Double dias_medios_em_estoque,
        int saldo_atual,
        Double cobertura_dias
) {
}
