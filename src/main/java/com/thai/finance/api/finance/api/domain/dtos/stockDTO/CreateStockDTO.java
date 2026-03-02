package com.thai.finance.api.finance.api.domain.dtos.stockDTO;

import com.thai.finance.api.finance.api.domain.entities.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStockDTO(
        @NotNull(message = "the propriety 'product' cannot be null ")
        Product product,
        @NotNull(message = "the propriety 'quantity' cannot be null ")
        Integer quantityProduct) {
}
