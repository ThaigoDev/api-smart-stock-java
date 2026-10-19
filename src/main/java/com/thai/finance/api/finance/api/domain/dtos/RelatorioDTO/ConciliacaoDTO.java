package com.thai.finance.api.finance.api.domain.dtos.RelatorioDTO;

import java.util.List;

/**
 * Resultado da auditoria do estoque: o saldo armazenado de cada produto deve ser igual à soma dos efeitos
 * de todas as suas movimentações, e cada movimentação deve partir do saldo em que a anterior terminou.
 */
public record ConciliacaoDTO(
        int produtos_verificados,
        int produtos_consistentes,
        int produtos_divergentes,
        long movimentacoes_verificadas,
        long quebras_de_encadeamento,
        long produtos_com_saldo_negativo,
        boolean consistente,
        List<DivergenciaConciliacaoDTO> divergencias
) {
}
