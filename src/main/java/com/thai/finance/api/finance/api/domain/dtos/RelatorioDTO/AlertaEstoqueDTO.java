package com.thai.finance.api.finance.api.domain.dtos.RelatorioDTO;

import com.thai.finance.api.finance.api.domain.enums.SituacaoEstoque;

import java.math.BigDecimal;
import java.util.UUID;

public record AlertaEstoqueDTO(
        UUID produto_id,
        String nome,
        String sku,
        String categoria,
        int saldo,
        int estoque_minimo,
        Integer estoque_maximo,
        int ponto_pedido,
        double demanda_media_diaria,
        Double cobertura_dias,
        SituacaoEstoque situacao,
        int sugestao_compra,
        BigDecimal valor_sugestao_compra
) {
}
