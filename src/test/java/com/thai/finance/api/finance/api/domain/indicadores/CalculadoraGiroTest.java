package com.thai.finance.api.finance.api.domain.indicadores;

import com.thai.finance.api.finance.api.domain.indicadores.CalculadoraGiro.Evento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CalculadoraGiroTest {

    private static final LocalDate DIA_1 = LocalDate.of(2026, 1, 1);

    @Test
    @DisplayName("Sem movimentações o estoque médio é o próprio saldo inicial")
    void semMovimentacoes() {
        assertThat(CalculadoraGiro.estoqueMedio(50, List.of(), DIA_1, DIA_1.plusDays(9))).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Estoque médio é a média dos saldos de fechamento diários")
    void mediaDosSaldosDiarios() {
        // 4 dias: fecha 100, 60, 60, 120 -> média 85
        List<Evento> eventos = List.of(
                new Evento(DIA_1.plusDays(1), -40),
                new Evento(DIA_1.plusDays(3), 60));
        assertThat(CalculadoraGiro.estoqueMedio(100, eventos, DIA_1, DIA_1.plusDays(3))).isCloseTo(85.0, within(1e-9));
    }

    @Test
    @DisplayName("Várias movimentações no mesmo dia contam pelo saldo de fechamento")
    void movimentacoesNoMesmoDia() {
        List<Evento> eventos = List.of(new Evento(DIA_1, -10), new Evento(DIA_1, -5), new Evento(DIA_1, 30));
        assertThat(CalculadoraGiro.estoqueMedio(20, eventos, DIA_1, DIA_1)).isEqualTo(35.0);
    }

    @Test
    void giro() {
        assertThat(CalculadoraGiro.giro(300, 50)).isEqualTo(6.0);
        assertThat(CalculadoraGiro.giro(300, 0)).isNull();
    }
}
