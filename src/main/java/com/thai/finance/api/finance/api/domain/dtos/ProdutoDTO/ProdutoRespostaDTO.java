package com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO;

import java.math.BigInteger;
import java.util.UUID;

public record ProdutoRespostaDTO(
        UUID id,
        String nome,
        String sku,
        Integer estoque_minimo,
        UUID categoria_id,
        BigInteger precoCusto,
        BigInteger precoVenda,
        UUID fornecedor_id,
        Boolean ativo
) {
}