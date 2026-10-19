package com.thai.finance.api.finance.api.domain.dtos.RelatorioDTO;

import java.math.BigDecimal;

public record ResumoMensalDTO(
        String mes,
        long quantidade_movimentacoes,
        long unidades_entrada,
        long unidades_saida,
        long unidades_devolvidas,
        long saldo_ajustes,
        long quantidade_estornos,
        BigDecimal faturamento
) {
}
