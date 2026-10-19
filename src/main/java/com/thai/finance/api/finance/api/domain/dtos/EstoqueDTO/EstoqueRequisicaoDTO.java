package com.thai.finance.api.finance.api.domain.dtos.EstoqueDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record EstoqueRequisicaoDTO(
        @NotNull(message = "O campo 'produto' não pode ser null")
        UUID produto_id,
        @NotNull(message = "O campo 'quantidade' não pode ser null")
        @PositiveOrZero(message = "O campo 'quantidade' não pode ser negativo")
        Integer quantidade) {
}
