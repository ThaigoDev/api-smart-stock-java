package com.thai.finance.api.finance.api.domain.indicadores;

import com.thai.finance.api.finance.api.domain.enums.SituacaoEstoque;

/**
 * Parâmetros de reposição de um produto e as regras de classificação derivadas deles.
 *
 * Ponto de pedido = demanda média diária x prazo de reposição + estoque mínimo (estoque de segurança).
 */
public record PoliticaEstoque(int estoqueMinimo, Integer estoqueMaximo, int prazoReposicaoDias) {

    public static final int PRAZO_REPOSICAO_PADRAO_DIAS = 7;

    public PoliticaEstoque {
        if (estoqueMinimo < 0) {
            throw new IllegalArgumentException("Estoque mínimo não pode ser negativo");
        }
        if (prazoReposicaoDias <= 0) {
            prazoReposicaoDias = PRAZO_REPOSICAO_PADRAO_DIAS;
        }
    }

    public int pontoDePedido(double demandaMediaDiaria) {
        return (int) Math.ceil(demandaMediaDiaria * prazoReposicaoDias) + estoqueMinimo;
    }

    public SituacaoEstoque classificar(int saldo, double demandaMediaDiaria) {
        if (saldo <= 0) {
            return SituacaoEstoque.RUPTURA;
        }
        if (saldo < estoqueMinimo) {
            return SituacaoEstoque.CRITICO;
        }
        if (saldo <= pontoDePedido(demandaMediaDiaria)) {
            return SituacaoEstoque.REPOR;
        }
        if (estoqueMaximo != null && saldo > estoqueMaximo) {
            return SituacaoEstoque.SUPERESTOQUE;
        }
        return SituacaoEstoque.NORMAL;
    }

    /**
     * Quantidade a comprar para voltar ao estoque máximo. Sem máximo definido, repõe o consumo de dois
     * prazos de reposição acima do ponto de pedido.
     */
    public int sugestaoDeCompra(int saldo, double demandaMediaDiaria) {
        if (!classificar(saldo, demandaMediaDiaria).exigeReposicao()) {
            return 0;
        }
        int alvo = estoqueMaximo != null
                ? estoqueMaximo
                : pontoDePedido(demandaMediaDiaria) + (int) Math.ceil(demandaMediaDiaria * prazoReposicaoDias);
        return Math.max(0, alvo - Math.max(saldo, 0));
    }

    /** Dias que o saldo atual sustenta a demanda média; null quando não há consumo. */
    public static Double coberturaEmDias(int saldo, double demandaMediaDiaria) {
        if (demandaMediaDiaria <= 0) {
            return null;
        }
        return Math.max(saldo, 0) / demandaMediaDiaria;
    }
}
