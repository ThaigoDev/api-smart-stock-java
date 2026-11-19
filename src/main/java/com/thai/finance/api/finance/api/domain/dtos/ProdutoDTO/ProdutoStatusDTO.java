package com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO;

import jakarta.validation.constraints.NotNull;

public record ProdutoStatusDTO(
        @NotNull(message = "O campo 'ativo' não pode ser nulo.")
        Boolean ativo
) {
}
