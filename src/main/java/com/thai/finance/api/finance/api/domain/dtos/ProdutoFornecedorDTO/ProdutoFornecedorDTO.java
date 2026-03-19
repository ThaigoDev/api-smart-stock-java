package com.thai.finance.api.finance.api.domain.dtos.ProdutoFornecedorDTO;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoFornecedorDTO(
        @NotNull(message = "O ID do fornecedor é obrigatório.")
        UUID fornecedor_id,

        @NotNull(message = "O preço de custo neste fornecedor é obrigatório.")
        BigDecimal precoCusto,

        String codigoNoFornecedor
) {



}
