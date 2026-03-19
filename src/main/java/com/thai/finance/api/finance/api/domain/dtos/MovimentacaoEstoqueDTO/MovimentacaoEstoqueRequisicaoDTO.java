package com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO;

import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record MovimentacaoEstoqueRequisicaoDTO(

        @NotNull(message = "O campo 'estoque_id' não pode ser null")
        UUID estoque_id,

        @NotNull(message = "O campo 'tipo' não pode ser null")
        TipoMovimentacaoEstoque tipo,

        @NotNull(message = "O campo 'quantidade' não pode ser null")
        BigDecimal quantidade,

        @NotBlank(message = "O campo 'motivo' não pode ser vazio")
        String motivo,

        String documentoReferencia
) {
}