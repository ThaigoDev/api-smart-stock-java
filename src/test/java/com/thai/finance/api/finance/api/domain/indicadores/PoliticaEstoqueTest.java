package com.thai.finance.api.finance.api.domain.indicadores;

import com.thai.finance.api.finance.api.domain.enums.SituacaoEstoque;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PoliticaEstoqueTest {

    // mínimo 10, máximo 100, prazo de 5 dias; com demanda de 4/dia o ponto de pedido é 4*5 + 10 = 30
    private final PoliticaEstoque politica = new PoliticaEstoque(10, 100, 5);
    private static final double DEMANDA = 4.0;

    @Test
    @DisplayName("Ponto de pedido = demanda diária x prazo de reposição + estoque mínimo")
    void pontoDePedido() {
        assertThat(politica.pontoDePedido(DEMANDA)).isEqualTo(30);
        assertThat(politica.pontoDePedido(2.1)).isEqualTo(21); // ceil(10,5) + 10
        assertThat(politica.pontoDePedido(0)).isEqualTo(10);
    }

    @ParameterizedTest(name = "saldo {0} => {1}")
    @CsvSource({
            "0,   RUPTURA",
            "-1,  RUPTURA",
            "9,   CRITICO",
            "10,  REPOR",
            "30,  REPOR",
            "31,  NORMAL",
            "100, NORMAL",
            "101, SUPERESTOQUE"
    })
    void classificacaoPorFaixaDeSaldo(int saldo, SituacaoEstoque esperada) {
        assertThat(politica.classificar(saldo, DEMANDA)).isEqualTo(esperada);
    }

    @Test
    @DisplayName("Sem estoque máximo definido não há superestoque")
    void semMaximoNaoHaSuperestoque() {
        PoliticaEstoque semMaximo = new PoliticaEstoque(10, null, 5);
        assertThat(semMaximo.classificar(1_000_000, DEMANDA)).isEqualTo(SituacaoEstoque.NORMAL);
    }

    @Test
    @DisplayName("Sugestão de compra repõe até o estoque máximo apenas quando há necessidade")
    void sugestaoDeCompra() {
        assertThat(politica.sugestaoDeCompra(25, DEMANDA)).isEqualTo(75);
        assertThat(politica.sugestaoDeCompra(0, DEMANDA)).isEqualTo(100);
        assertThat(politica.sugestaoDeCompra(50, DEMANDA)).isZero();
        assertThat(politica.sugestaoDeCompra(150, DEMANDA)).isZero();
    }

    @Test
    @DisplayName("Sem máximo, a sugestão leva o saldo ao ponto de pedido mais um prazo de consumo")
    void sugestaoSemMaximo() {
        PoliticaEstoque semMaximo = new PoliticaEstoque(10, null, 5);
        // alvo = 30 (ponto de pedido) + 20 (consumo no prazo) = 50
        assertThat(semMaximo.sugestaoDeCompra(20, DEMANDA)).isEqualTo(30);
    }

    @Test
    void coberturaEmDias() {
        assertThat(PoliticaEstoque.coberturaEmDias(40, DEMANDA)).isEqualTo(10.0);
        assertThat(PoliticaEstoque.coberturaEmDias(40, 0)).isNull();
    }

    @Test
    void prazoInvalidoAssumeOPadrao() {
        assertThat(new PoliticaEstoque(0, null, 0).prazoReposicaoDias()).isEqualTo(PoliticaEstoque.PRAZO_REPOSICAO_PADRAO_DIAS);
    }

    @Test
    void estoqueMinimoNegativoEhRejeitado() {
        assertThatThrownBy(() -> new PoliticaEstoque(-1, null, 5)).isInstanceOf(IllegalArgumentException.class);
    }
}
