package com.thai.finance.api.finance.api.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class TipoMovimentacaoEstoqueTest {

    @ParameterizedTest(name = "{0} de {1} unidade(s) altera o saldo em {2}")
    @CsvSource({
            "ENTRADA,   10,  10",
            "SAIDA,     10, -10",
            "DEVOLUCAO,  3,   3",
            "AJUSTE,     4,   4",
            "AJUSTE,    -4,  -4",
            "ESTORNO,  -10, -10",
            "ESTORNO,   10,  10"
    })
    void efeitoNoSaldoDeCadaTipo(TipoMovimentacaoEstoque tipo, int quantidade, int efeitoEsperado) {
        assertThat(tipo.efeitoNoSaldo(quantidade)).isEqualTo(efeitoEsperado);
    }

    @Test
    @DisplayName("Apenas AJUSTE e ESTORNO aceitam quantidade negativa")
    void quantidadeNegativa() {
        assertThat(TipoMovimentacaoEstoque.AJUSTE.aceitaQuantidadeNegativa()).isTrue();
        assertThat(TipoMovimentacaoEstoque.ESTORNO.aceitaQuantidadeNegativa()).isTrue();
        assertThat(TipoMovimentacaoEstoque.ENTRADA.aceitaQuantidadeNegativa()).isFalse();
        assertThat(TipoMovimentacaoEstoque.SAIDA.aceitaQuantidadeNegativa()).isFalse();
        assertThat(TipoMovimentacaoEstoque.DEVOLUCAO.aceitaQuantidadeNegativa()).isFalse();
    }

    @Test
    @DisplayName("ESTORNO é gerado apenas pelo sistema, nunca enviado pela API")
    void estornoNaoPermitidoViaApi() {
        assertThat(TipoMovimentacaoEstoque.ESTORNO.permitidoViaApi()).isFalse();
        assertThat(TipoMovimentacaoEstoque.SAIDA.permitidoViaApi()).isTrue();
    }
}
