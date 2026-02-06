package com.thai.finance.api.finance.api.dtos;

import com.thai.finance.api.finance.api.entities.Category;
import com.thai.finance.api.finance.api.entities.Stock;
import com.thai.finance.api.finance.api.entities.Supplier;

import java.math.BigInteger;
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
