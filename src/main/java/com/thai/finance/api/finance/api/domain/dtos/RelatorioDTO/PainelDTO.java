package com.thai.finance.api.finance.api.domain.dtos.RelatorioDTO;

import com.thai.finance.api.finance.api.domain.enums.SituacaoEstoque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record PainelDTO(
        LocalDate data_referencia,
        long produtos_ativos,
        long unidades_em_estoque,
        BigDecimal valor_total_em_estoque,
        Map<SituacaoEstoque, Long> produtos_por_situacao,
        BigDecimal valor_em_superestoque,
        long movimentacoes_ultimos_30_dias,
        BigDecimal faturamento_ultimos_30_dias,
        List<CurvaABCItemDTO> mais_vendidos_ultimos_30_dias
) {
}
