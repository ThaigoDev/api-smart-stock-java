package com.thai.finance.api.finance.api.domain.dtos.productDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateProductDTO(
        @NotBlank(message = "the propriety 'name product' cannot be null ")
        @Size(min = 5 ,max = 20, message = "It must be between 5 and 20 characters long.")
        String nameProduct,

        @NotBlank(message = "the propriety 'sku product' cannot be null ")
        @Size(min = 5 ,max = 20, message = "It must be between 5 and 20 characters long.")
        String skuProduct,

        @NotNull(message = "the propriety 'minimum stock' cannot be null ")
        Integer minimum_stock,

        @NotNull(message = "the propriety 'category' cannot be null ")
        UUID categoryId,

        @NotNull(message = "the propriety 'supplier' cannot be null ")
        UUID supplier,

        UUID stock,

        @NotNull(message = "the propriety 'initial stock' cannot be null ")
        Integer initialStock,

        Boolean active
) {
}
