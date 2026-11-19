package com.thai.finance.api.finance.api.domain.dtos.RelatorioDTO;

import com.thai.finance.api.finance.api.domain.enums.SituacaoEstoque;

import java.math.BigDecimal;
import java.util.UUID;

/** Visão consolidada de um produto: cadastro, saldo e indicadores de reposição na data de referência. */
public record PosicaoProdutoDTO(
        UUID produto_id,
        String nome,
        String sku,
        UUID categoria_id,
        String categoria,
        UUID fornecedor_id,
        String fornecedor,
        BigDecimal preco,
        boolean ativo,
        // false quando o estoque do produto ainda não foi aberto (POST /estoques)
        boolean possui_estoque,
        int saldo,
        int estoque_minimo,
        Integer estoque_maximo,
        int prazo_reposicao_dias,
        int ponto_pedido,
        double demanda_media_diaria,
        Double cobertura_dias,
        // null para produtos sem estoque aberto
        SituacaoEstoque situacao,
        int sugestao_compra,
        BigDecimal valor_em_estoque
) {
}
