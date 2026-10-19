package com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO;

import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MovimentacaoEstoqueRequisicaoDTO(
        @NotNull(message = "O campo 'produto' não pode ser null")
        UUID produto_id,

        @NotNull(message = "O campo 'tipo' não pode ser null")
        TipoMovimentacaoEstoque tipo,

        @NotNull(message = "O campo 'quantidade' não pode ser null")
        Integer quantidade,

        @NotBlank(message = "O campo 'motivo' não pode ser null")
        @Size(max = 255, message = "O campo 'motivo' deve ter no máximo 255 caracteres")
        String motivo
) {
    @AssertTrue(message = "O campo 'quantidade' deve ser maior que zero (apenas AJUSTE aceita valor negativo, e nunca zero)")
    public boolean isQuantidadeValida() {
        if (quantidade == null || tipo == null) {
            return true; // já coberto pelo @NotNull
        }
        return quantidade != 0 && (quantidade > 0 || tipo.aceitaQuantidadeNegativa());
    }

    @AssertTrue(message = "O tipo ESTORNO não pode ser informado diretamente; use POST /movimentacoesestoque/{id}/estorno")
    public boolean isTipoPermitido() {
        return tipo == null || tipo.permitidoViaApi();
    }
}
