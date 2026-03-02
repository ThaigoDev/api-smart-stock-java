package com.thai.finance.api.finance.api.domain.dtos.AuthDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @NotBlank( message = "Email is required")
        @Email
        String email,

        @NotBlank (message = "password is required")
        @Size(min = 3, max = 20,message = "The password must be between 5 and 20 characters long.")
        String password) {
}
