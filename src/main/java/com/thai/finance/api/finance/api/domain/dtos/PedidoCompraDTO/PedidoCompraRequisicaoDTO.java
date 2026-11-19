package com.thai.finance.api.finance.api.domain.dtos.PedidoCompraDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record PedidoCompraRequisicaoDTO(
        @NotNull(message = "O campo 'fornecedor_id' não pode ser nulo.")
        UUID fornecedor_id,

        @Size(max = 255, message = "O campo 'observacao' deve ter no máximo 255 caracteres.")
        String observacao,

        @NotEmpty(message = "O pedido deve ter ao menos um item.")
        List<@Valid ItemPedidoCompraRequisicaoDTO> itens
) {
}
