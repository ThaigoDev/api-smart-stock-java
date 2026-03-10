package com.thai.finance.api.finance.api.domain.dtos.EstoqueDTO;

import com.thai.finance.api.finance.api.domain.entities.Produto;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EstoqueRequisicaoDTO(
        @NotNull(message = "O campo 'produto' não pode ser null")
        UUID produto,
        @NotNull(message = "O campo 'quantidade' não pode ser null")
        Integer quantidade) {
}
