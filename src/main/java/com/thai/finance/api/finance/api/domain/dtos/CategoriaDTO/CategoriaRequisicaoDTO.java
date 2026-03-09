package com.thai.finance.api.finance.api.domain.dtos.CategoriaDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequisicaoDTO(
        @NotBlank( message = " Categoria é obrigatória, não pode ser 'null'")
        @Size(min = 3, max = 20, message = "O nome da categoria deve ter entre 3 a 20 caracteres")
        String nome,

        String descricao
) {
}
