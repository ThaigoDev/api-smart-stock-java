package com.thai.finance.api.finance.api.domain.dtos.categoryDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryDTO(
        @NotBlank
        @Size(min = 3, max = 20, message = "It must be between 5 and 20 characters long.")
        String name
) {
}
