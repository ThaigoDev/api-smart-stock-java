package com.thai.finance.api.finance.api.domain.dtos.AuthDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequestDTO(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 20,message = "Enter a valid number of characters.")
        String username,

        @NotBlank(message = "Email is required")
        @Email
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 3, max = 20,message = "The password must be between 5 and 20 characters long.")
        String password) {
}
