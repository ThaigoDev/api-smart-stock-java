package com.thai.finance.api.finance.api.domain.dtos.PedidoCompraDTO;

import com.thai.finance.api.finance.api.domain.enums.StatusPedidoCompra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoCompraRespostaDTO(
        UUID id,
        String codigo,
        UUID fornecedor_id,
        String fornecedor,
        StatusPedidoCompra status,
        String observacao,
        String criado_por,
        LocalDateTime criado_em,
        String recebido_por,
        LocalDateTime recebido_em,
        LocalDateTime cancelado_em,
        List<ItemPedidoCompraRespostaDTO> itens,
        int quantidade_total,
        BigDecimal valor_estimado
) {
}
