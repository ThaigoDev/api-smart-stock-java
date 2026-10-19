package com.thai.finance.api.finance.api.domain.enums;

public enum SituacaoEstoque {
    // saldo zerado: qualquer venda será perdida
    RUPTURA,
    // saldo abaixo do estoque mínimo (estoque de segurança consumido)
    CRITICO,
    // saldo atingiu o ponto de pedido: hora de comprar para não romper durante o prazo de reposição
    REPOR,
    NORMAL,
    // saldo acima do estoque máximo: capital imobilizado
    SUPERESTOQUE;

    public boolean exigeReposicao() {
        return this == RUPTURA || this == CRITICO || this == REPOR;
    }
}
