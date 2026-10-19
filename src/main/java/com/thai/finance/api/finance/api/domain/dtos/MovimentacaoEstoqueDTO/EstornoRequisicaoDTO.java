package com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO;

import jakarta.validation.constraints.NotBlank;

public record EstornoRequisicaoDTO(
        @NotBlank(message = "O campo 'motivo' do estorno não pode estar em branco")
        String motivo
) {
}
