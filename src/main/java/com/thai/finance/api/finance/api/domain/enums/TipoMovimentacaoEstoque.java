package com.thai.finance.api.finance.api.domain.enums;

/**
 * Cada tipo de movimentação sabe qual é o seu efeito sobre o saldo.
 * Incluir um novo tipo exige apenas uma nova constante, sem alterar o serviço (OCP).
 */
public enum TipoMovimentacaoEstoque {
    ENTRADA {
        @Override
        public int efeitoNoSaldo(int quantidade) {
            return quantidade;
        }
    },
    SAIDA {
        @Override
        public int efeitoNoSaldo(int quantidade) {
            return -quantidade;
        }
    },
    DEVOLUCAO {
        @Override
        public int efeitoNoSaldo(int quantidade) {
            return quantidade;
        }
    },
    // Ajuste de inventário: quantidade positiva soma (sobra na contagem), negativa subtrai (perda, quebra, vencimento)
    AJUSTE {
        @Override
        public int efeitoNoSaldo(int quantidade) {
            return quantidade;
        }

        @Override
        public boolean aceitaQuantidadeNegativa() {
            return true;
        }
    },
    // Gerado apenas pelo sistema ao estornar uma movimentação; carrega o efeito inverso da original
    ESTORNO {
        @Override
        public int efeitoNoSaldo(int quantidade) {
            return quantidade;
        }

        @Override
        public boolean aceitaQuantidadeNegativa() {
            return true;
        }

        @Override
        public boolean permitidoViaApi() {
            return false;
        }
    };

    public abstract int efeitoNoSaldo(int quantidade);

    public boolean aceitaQuantidadeNegativa() {
        return false;
    }

    public boolean permitidoViaApi() {
        return true;
    }
}
