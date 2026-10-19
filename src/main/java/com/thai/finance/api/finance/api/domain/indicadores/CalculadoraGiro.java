package com.thai.finance.api.finance.api.domain.indicadores;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;

/**
 * Estoque médio e giro de estoque a partir do histórico de movimentações.
 *
 * Estoque médio = média dos saldos de fechamento de cada dia do período.
 * Giro = quantidade vendida no período / estoque médio.
 */
public final class CalculadoraGiro {

    public record Evento(LocalDate data, int efeitoNoSaldo) {
    }

    private CalculadoraGiro() {
    }

    /**
     * @param saldoInicial saldo no início do primeiro dia
     * @param eventos      movimentações do período, ordenadas por data
     * @param inicio       primeiro dia (inclusive)
     * @param fim          último dia (inclusive)
     */
    public static double estoqueMedio(int saldoInicial, List<Evento> eventos, LocalDate inicio, LocalDate fim) {
        long dias = ChronoUnit.DAYS.between(inicio, fim) + 1;
        if (dias <= 0) {
            return saldoInicial;
        }
        Iterator<Evento> it = eventos.iterator();
        Evento proximo = it.hasNext() ? it.next() : null;
        long saldo = saldoInicial;
        long somaSaldos = 0;
        for (LocalDate dia = inicio; !dia.isAfter(fim); dia = dia.plusDays(1)) {
            while (proximo != null && !proximo.data().isAfter(dia)) {
                saldo += proximo.efeitoNoSaldo();
                proximo = it.hasNext() ? it.next() : null;
            }
            somaSaldos += saldo;
        }
        return (double) somaSaldos / dias;
    }

    public static Double giro(long quantidadeVendida, double estoqueMedio) {
        if (estoqueMedio <= 0) {
            return null;
        }
        return quantidadeVendida / estoqueMedio;
    }
}
