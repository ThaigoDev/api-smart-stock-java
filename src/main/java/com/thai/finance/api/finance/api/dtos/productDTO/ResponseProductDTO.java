package com.thai.finance.api.finance.api.dtos.productDTO;

import java.util.UUID;

public record ResponseProductDTO(
        String nameProduct,
        String skuProduct,
        Integer minimum_stock,
        UUID categoryId,
        UUID supplier,
        UUID stock,
        Integer stockQuantity,
        Integer initialStock,
        Boolean active
) {
}
