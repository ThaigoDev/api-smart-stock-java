package com.thai.finance.api.finance.api.domain.dtos.AuthDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarContaRequisicaoDTO(
        @NotBlank(message = "Nome é obrigatório, não pode ser 'null'")
        @Size(min = 3, max = 20,message = "O nome deve ter entre 3 a 20 caracteres")
        String nome,

        @NotBlank(message = "O Email é obrigatório, não pode ser 'null'")
        @Email
        String email,

        @NotBlank(message = "A Senha é obrigatória, não pode ser 'null'")
        @Size(min = 3, max = 20,message = "A senha deve ter entre 3 a 20 carateres.")
        String senha) {
}
