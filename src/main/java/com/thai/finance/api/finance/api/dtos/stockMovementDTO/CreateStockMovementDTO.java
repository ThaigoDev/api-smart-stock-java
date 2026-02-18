package com.thai.finance.api.finance.api.dtos.stockMovementDTO;

import com.thai.finance.api.finance.api.entities.Product;
import com.thai.finance.api.finance.api.enums.MovementType;

import java.util.UUID;

public record CreateStockMovementDTO( UUID productId, MovementType type, Integer quantity) {
}
