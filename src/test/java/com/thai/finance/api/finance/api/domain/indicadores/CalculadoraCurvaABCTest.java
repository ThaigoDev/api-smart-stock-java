package com.thai.finance.api.finance.api.domain.indicadores;

import com.thai.finance.api.finance.api.domain.indicadores.CalculadoraCurvaABC.ItemClassificado;
import com.thai.finance.api.finance.api.domain.indicadores.CalculadoraCurvaABC.ItemConsumo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraCurvaABCTest {

    private static ItemConsumo item(String nome, int valor) {
        return new ItemConsumo(UUID.randomUUID(), nome, BigDecimal.valueOf(valor), valor);
    }

    private static char classeDe(List<ItemClassificado> resultado, String nome) {
        return resultado.stream().filter(i -> i.nome().equals(nome)).findFirst().orElseThrow().classe();
    }

    @Test
    @DisplayName("Distribuição de Pareto: poucos itens concentram 80% do valor e formam a classe A")
    void classificacaoClassica() {
        // total 1000: A = 500 + 300 (80%), B = 100 + 50 (95%), C = o restante
        List<ItemConsumo> itens = List.of(
                item("p1", 500), item("p2", 300), item("p3", 100), item("p4", 50),
                item("p5", 20), item("p6", 15), item("p7", 10), item("p8", 5));

        List<ItemClassificado> resultado = CalculadoraCurvaABC.classificar(itens);

        assertThat(resultado).extracting(ItemClassificado::nome).containsExactly("p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8");
        assertThat(resultado).extracting(ItemClassificado::classe).containsExactly('A', 'A', 'B', 'B', 'C', 'C', 'C', 'C');
        assertThat(resultado.get(resultado.size() - 1).percentualAcumulado()).isEqualByComparingTo("100.00");
        assertThat(resultado.get(0).percentualValor()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("O item que cruza a fronteira de 80% permanece na classe A")
    void itemNaFronteira() {
        List<ItemClassificado> resultado = CalculadoraCurvaABC.classificar(List.of(item("grande", 70), item("medio", 25), item("pequeno", 5)));
        assertThat(classeDe(resultado, "grande")).isEqualTo('A');
        assertThat(classeDe(resultado, "medio")).isEqualTo('A'); // começa em 70% < 80%
        assertThat(classeDe(resultado, "pequeno")).isEqualTo('C'); // começa em 95%
    }

    @Test
    @DisplayName("Produtos sem venda no período são classe C")
    void semConsumo() {
        List<ItemClassificado> resultado = CalculadoraCurvaABC.classificar(List.of(item("vende", 10), item("parado", 0)));
        assertThat(classeDe(resultado, "vende")).isEqualTo('A');
        assertThat(classeDe(resultado, "parado")).isEqualTo('C');
    }

    @Test
    @DisplayName("Período sem nenhuma venda não gera divisão por zero")
    void totalZero() {
        List<ItemClassificado> resultado = CalculadoraCurvaABC.classificar(List.of(item("a", 0), item("b", 0)));
        assertThat(resultado).allMatch(i -> i.classe() == 'C' && i.percentualValor().signum() == 0);
    }

    @Test
    void listaVazia() {
        assertThat(CalculadoraCurvaABC.classificar(List.of())).isEmpty();
    }
}
