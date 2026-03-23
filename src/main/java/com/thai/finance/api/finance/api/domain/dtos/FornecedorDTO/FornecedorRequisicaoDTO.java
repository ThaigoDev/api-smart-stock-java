package com.thai.finance.api.finance.api.domain.dtos.FornecedorDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FornecedorRequisicaoDTO(
        @NotBlank( message = " Nome é obrigatório, não pode ser 'null'")
        String nome,

        @NotBlank(message = "O Email é obrigatório, não pode ser 'null'")
        @Email(message = "Email inválido")
        String email,

        @NotBlank
        @Size(min = 11)
        String telefone
) {
}
