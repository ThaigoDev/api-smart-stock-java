package com.thai.finance.api.finance.api.domain.dtos.EstoqueDTO;

import com.thai.finance.api.finance.api.domain.entities.Produto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record EstoqueRequisicaoDTO(

        @NotNull(message = "O campo 'quantidade atual' não pode ser null")
        BigDecimal quantidadeAtual,

        @NotNull(message = "O campo 'quantidade Minima' não pode ser null")
        BigDecimal quantidadeMinima,

        @NotNull(message = "O campo 'quantidade Máxima' não pode ser null")
        BigDecimal quantidadeMaxima,

        @NotBlank(message = "O campo 'localização' não pode ser null")
        String localizacao,

        @NotNull(message = "O campo 'produto' não pode ser null")
        UUID produto_id
){
}
