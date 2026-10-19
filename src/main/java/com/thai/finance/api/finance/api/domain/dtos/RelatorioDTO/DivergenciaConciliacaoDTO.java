package com.thai.finance.api.finance.api.domain.dtos.RelatorioDTO;

import java.util.UUID;

public record DivergenciaConciliacaoDTO(
        UUID produto_id,
        String nome,
        int saldo_registrado,
        int saldo_recalculado,
        int quebras_de_encadeamento
) {
}
