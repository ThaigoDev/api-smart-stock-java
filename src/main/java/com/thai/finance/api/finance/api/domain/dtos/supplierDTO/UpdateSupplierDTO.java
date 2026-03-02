package com.thai.finance.api.finance.api.domain.dtos.supplierDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSupplierDTO(
        @NotBlank(message = "'name' cannot be null")
        @Size(min = 5, max = 20, message = "It must be between 5 and 20 characters long.")
        String name
) {
}
