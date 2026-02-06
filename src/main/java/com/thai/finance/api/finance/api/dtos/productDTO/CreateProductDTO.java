package com.thai.finance.api.finance.api.dtos.productDTO;

import java.util.UUID;

public record CreateProductDTO(
        String nameProduct,
        String skuProduct,
        Integer minimum_stock,
        UUID categoryId,
        UUID supplier,
        UUID stock,
        Integer initialStock,
        Boolean active
) {
}
