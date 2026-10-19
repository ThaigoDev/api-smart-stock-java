package com.thai.finance.api.finance.api.domain.indicadores;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Classificação ABC (princípio de Pareto) pelo valor consumido no período:
 * classe A até 80% do valor acumulado, B até 95% e C o restante.
 * O item que cruza a fronteira pertence à classe em que começou.
 */
public final class CalculadoraCurvaABC {

    public static final BigDecimal LIMITE_A = new BigDecimal("80");
    public static final BigDecimal LIMITE_B = new BigDecimal("95");

    public record ItemConsumo(UUID produtoId, String nome, BigDecimal valorConsumido, long quantidadeConsumida) {
    }

    public record ItemClassificado(UUID produtoId, String nome, BigDecimal valorConsumido, long quantidadeConsumida,
                                   BigDecimal percentualValor, BigDecimal percentualAcumulado, char classe) {
    }

    private CalculadoraCurvaABC() {
    }

    public static List<ItemClassificado> classificar(List<ItemConsumo> itens) {
        BigDecimal total = itens.stream().map(ItemConsumo::valorConsumido).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<ItemConsumo> ordenados = itens.stream()
                .sorted(Comparator.comparing(ItemConsumo::valorConsumido).reversed().thenComparing(ItemConsumo::nome))
                .toList();

        List<ItemClassificado> resultado = new ArrayList<>();
        BigDecimal acumuladoAntes = BigDecimal.ZERO;
        for (ItemConsumo item : ordenados) {
            BigDecimal percentual = total.signum() == 0
                    ? BigDecimal.ZERO
                    : item.valorConsumido().multiply(BigDecimal.valueOf(100)).divide(total, 4, RoundingMode.HALF_UP);
            char classe;
            if (item.valorConsumido().signum() <= 0) {
                classe = 'C';
            } else if (acumuladoAntes.compareTo(LIMITE_A) < 0) {
                classe = 'A';
            } else if (acumuladoAntes.compareTo(LIMITE_B) < 0) {
                classe = 'B';
            } else {
                classe = 'C';
            }
            BigDecimal acumulado = acumuladoAntes.add(percentual);
            resultado.add(new ItemClassificado(item.produtoId(), item.nome(), item.valorConsumido(),
                    item.quantidadeConsumida(), percentual.setScale(2, RoundingMode.HALF_UP),
                    acumulado.setScale(2, RoundingMode.HALF_UP), classe));
            acumuladoAntes = acumulado;
        }
        return resultado;
    }
}
