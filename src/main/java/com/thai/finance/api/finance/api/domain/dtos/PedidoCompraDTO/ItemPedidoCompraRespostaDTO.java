package com.thai.finance.api.finance.api.domain.dtos.PedidoCompraDTO;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPedidoCompraRespostaDTO(
        UUID produto_id,
        String produto,
        String sku,
        int quantidade,
        // valor de referência: quantidade x preço de venda (o sistema não registra custo de aquisição)
        BigDecimal valor_estimado
) {
}
