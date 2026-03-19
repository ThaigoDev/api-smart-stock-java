package com.thai.finance.api.finance.api.domain.dtos.FornecedorDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

public record FornecedorRequisicaoDTO(
        @NotBlank( message = " CNPJ é obrigatório, não pode ser 'null'")
        @CNPJ
        String cnpj,

        @NotBlank( message = " Nome é obrigatório, não pode ser 'null'")
        String razaoSocial,

        @NotBlank(message = "O Email é obrigatório, não pode ser 'null'")
        @Email(message = "Email inválido")
        String email,

        @NotBlank
        @Size(min = 11)
        String telefone
) {
}
