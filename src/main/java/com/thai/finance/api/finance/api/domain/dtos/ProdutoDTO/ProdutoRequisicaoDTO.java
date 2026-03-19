package com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO;

import com.thai.finance.api.finance.api.domain.dtos.ProdutoFornecedorDTO.ProdutoFornecedorDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public record ProdutoRequisicaoDTO(

        @NotBlank(message = "O campo 'nome' não pode estar em branco.")
        @Size(min = 5, max = 20, message = "O campo 'nome' deve conter entre 5 e 20 caracteres.")
        String nome,

        @NotBlank(message = "O campo 'sku' não pode estar em branco.")
        @Size(min = 5, max = 20, message = "O campo 'sku' deve conter entre 5 e 20 caracteres.")
        String sku,

        @NotNull(message = "O campo 'estoque_minimo' não pode ser nulo.")
        Integer estoque_minimo,

        @NotNull(message = "O campo 'categoria_id' não pode ser nulo.")
        UUID categoria_id,

        @NotNull(message = "O campo 'precoCusto' não pode ser nulo.")
        BigInteger precoCusto,

        @NotNull(message = "O campo 'precoVenda' não pode ser nulo.")
        BigInteger precoVenda,

        @NotEmpty(message = "O campo 'fornecedor_id' não pode ser nulo.")
        List<ProdutoFornecedorDTO> fornecedores,

        Boolean ativo
) {
}