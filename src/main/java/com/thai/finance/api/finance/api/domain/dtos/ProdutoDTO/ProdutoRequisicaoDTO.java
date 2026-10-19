package com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;
import java.util.UUID;

public record ProdutoRequisicaoDTO(

        @NotBlank(message = "O campo 'nome' não pode estar em branco.")
        @Size(min = 5, max = 20, message = "O campo 'nome' deve conter entre 5 e 20 caracteres.")
        String nome,

        @NotBlank(message = "O campo 'sku' não pode estar em branco.")
        @Size(min = 5, max = 20, message = "O campo 'sku' deve conter entre 5 e 20 caracteres.")
        String sku,

        @NotNull(message = "O campo 'estoque_minimo' não pode ser nulo.")
        @PositiveOrZero(message = "O campo 'estoque_minimo' não pode ser negativo.")
        Integer estoque_minimo,

        @NotNull(message = "O campo 'categoria_id' não pode ser nulo.")
        UUID categoria_id,

        @NotNull(message = "O campo 'preco' não pode ser nulo.")
        @PositiveOrZero(message = "O campo 'preco' não pode ser negativo.")
        BigInteger preco,

        @NotNull(message = "O campo 'fornecedor_id' não pode ser nulo.")
        UUID fornecedor_id,

        Boolean ativo,

        @Positive(message = "O campo 'estoque_maximo' deve ser maior que zero.")
        Integer estoque_maximo,

        @Positive(message = "O campo 'prazo_reposicao_dias' deve ser maior que zero.")
        Integer prazo_reposicao_dias
) {
    @AssertTrue(message = "O campo 'estoque_maximo' deve ser maior que o 'estoque_minimo'.")
    public boolean isFaixaDeEstoqueValida() {
        return estoque_maximo == null || estoque_minimo == null || estoque_maximo > estoque_minimo;
    }
}
