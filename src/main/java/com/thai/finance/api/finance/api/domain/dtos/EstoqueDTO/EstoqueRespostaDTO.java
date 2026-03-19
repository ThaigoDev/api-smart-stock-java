package com.thai.finance.api.finance.api.domain.dtos.EstoqueDTO;

import com.thai.finance.api.finance.api.domain.entities.Produto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EstoqueRespostaDTO(
        UUID id ,

        Integer quantidade,

        BigDecimal quantidadeAtual,

        BigDecimal quantidadeMinima,

        BigDecimal quantidadeMaxima,

        String localizacao,

        Produto produto,

        LocalDateTime ultima_atualizacao ) {
}
