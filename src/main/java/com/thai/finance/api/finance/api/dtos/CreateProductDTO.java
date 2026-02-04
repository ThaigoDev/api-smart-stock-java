package com.thai.finance.api.finance.api.dtos;

import java.util.UUID;

public record CreateProductDTO(UUID id, String nameProduct, String skuProduct, Integer minimum_stock, UUID categoryId, UUID supplier, UUID stock) {
}
