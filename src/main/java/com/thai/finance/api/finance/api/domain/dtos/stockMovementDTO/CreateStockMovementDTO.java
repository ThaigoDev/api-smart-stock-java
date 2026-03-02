package com.thai.finance.api.finance.api.domain.dtos.stockMovementDTO;

import com.thai.finance.api.finance.api.domain.enums.MovementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateStockMovementDTO(
        @NotNull(message = "the propriety 'productId' cannot be null ")
        UUID productId,
        @NotNull(message = "the propriety 'type' cannot be null ")
        MovementType type,
        @NotNull(message = "the propriety 'quantity' cannot be null ")
        Integer quantity) {
}
