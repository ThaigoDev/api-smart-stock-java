package com.thai.finance.api.finance.api.domain.dtos.PedidoCompraDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ItemPedidoCompraRequisicaoDTO(
        @NotNull(message = "O campo 'produto_id' do item não pode ser nulo.")
        UUID produto_id,

        @NotNull(message = "O campo 'quantidade' do item não pode ser nulo.")
        @Positive(message = "A quantidade do item deve ser maior que zero.")
        Integer quantidade
) {
}
