package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.RelatorioDTO.*;
import com.thai.finance.api.finance.api.domain.entities.Estoque;
import com.thai.finance.api.finance.api.domain.entities.MovimentacaoEstoque;
import com.thai.finance.api.finance.api.domain.entities.Produto;
import com.thai.finance.api.finance.api.domain.enums.SituacaoEstoque;
import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import com.thai.finance.api.finance.api.domain.indicadores.CalculadoraCurvaABC;
import com.thai.finance.api.finance.api.domain.indicadores.CalculadoraGiro;
import com.thai.finance.api.finance.api.domain.indicadores.PoliticaEstoque;
import com.thai.finance.api.finance.api.respository.RepositoryEstoque;
import com.thai.finance.api.finance.api.respository.RepositoryMovimentacaoEstoque;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Indicadores gerenciais calculados a partir do histórico de movimentações.
 * Somente leitura: nenhum relatório altera o estado do estoque.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceRelatorio {
    public static final int PERIODO_PADRAO_DIAS = 90;

    private final RepositoryEstoque repositoryEstoque;
    private final RepositoryMovimentacaoEstoque repositoryMovimentacaoEstoque;

    // ---------------------------------------------------------------- alertas de ruptura e superestoque

    public List<AlertaEstoqueDTO> alertas(LocalDate dataReferencia, boolean somenteComAlerta) {
        LocalDate referencia = dataReferencia != null ? dataReferencia : LocalDate.now();
        return situacaoDosProdutos(referencia).stream()
                .filter(a -> !somenteComAlerta || a.situacao() != SituacaoEstoque.NORMAL)
                .sorted(Comparator.comparing(AlertaEstoqueDTO::situacao)
                        .thenComparing(a -> a.cobertura_dias() == null ? Double.MAX_VALUE : a.cobertura_dias()))
                .toList();
    }

    private List<AlertaEstoqueDTO> situacaoDosProdutos(LocalDate referencia) {
        LocalDateTime fimJanela = referencia.plusDays(1).atStartOfDay();
        Map<UUID, Long> vendidoNaJanela = somarPorProduto(TipoMovimentacaoEstoque.SAIDA,
                fimJanela.minusDays(ServiceMovimentacaoEstoque.JANELA_DEMANDA_DIAS), fimJanela);

        List<AlertaEstoqueDTO> resultado = new ArrayList<>();
        for (Estoque estoque : repositoryEstoque.buscarTodosComProduto()) {
            Produto produto = estoque.getProduto();
            if (!produto.isAtivo()) {
                continue;
            }
            double demanda = (double) vendidoNaJanela.getOrDefault(produto.getId(), 0L) / ServiceMovimentacaoEstoque.JANELA_DEMANDA_DIAS;
            PoliticaEstoque politica = produto.politicaEstoque();
            int saldo = estoque.getQuantidade();
            int sugestao = politica.sugestaoDeCompra(saldo, demanda);
            resultado.add(new AlertaEstoqueDTO(
                    produto.getId(), produto.getNome(), produto.getSku(), produto.getCategoria().getNome(),
                    saldo, politica.estoqueMinimo(), politica.estoqueMaximo(), politica.pontoDePedido(demanda),
                    arredondar(demanda), arredondar(PoliticaEstoque.coberturaEmDias(saldo, demanda)),
                    politica.classificar(saldo, demanda), sugestao,
                    produto.precoEmReais().multiply(BigDecimal.valueOf(sugestao))));
        }
        return resultado;
    }

    // ---------------------------------------------------------------- curva ABC

    public CurvaABCDTO curvaABC(LocalDate inicio, LocalDate fim) {
        Periodo periodo = Periodo.de(inicio, fim);
        Map<UUID, Long> vendido = somarPorProduto(TipoMovimentacaoEstoque.SAIDA, periodo.inicioInclusivo(), periodo.fimExclusivo());

        List<CalculadoraCurvaABC.ItemConsumo> consumo = repositoryEstoque.buscarTodosComProduto().stream()
                .map(Estoque::getProduto)
                .map(p -> {
                    long quantidade = vendido.getOrDefault(p.getId(), 0L);
                    return new CalculadoraCurvaABC.ItemConsumo(p.getId(), p.getNome(),
                            p.precoEmReais().multiply(BigDecimal.valueOf(quantidade)), quantidade);
                })
                .toList();
        List<CalculadoraCurvaABC.ItemClassificado> classificados = CalculadoraCurvaABC.classificar(consumo);

        BigDecimal total = classificados.stream().map(CalculadoraCurvaABC.ItemClassificado::valorConsumido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<CurvaABCClasseDTO> resumo = new ArrayList<>();
        for (char classe : new char[]{'A', 'B', 'C'}) {
            List<CalculadoraCurvaABC.ItemClassificado> daClasse = classificados.stream().filter(i -> i.classe() == classe).toList();
            BigDecimal valor = daClasse.stream().map(CalculadoraCurvaABC.ItemClassificado::valorConsumido).reduce(BigDecimal.ZERO, BigDecimal::add);
            resumo.add(new CurvaABCClasseDTO(String.valueOf(classe), daClasse.size(),
                    percentual(BigDecimal.valueOf(daClasse.size()), BigDecimal.valueOf(classificados.size())),
                    valor, percentual(valor, total)));
        }
        return new CurvaABCDTO(periodo.inicio(), periodo.fim(), total, resumo,
                classificados.stream().map(ServiceRelatorio::paraItemABC).toList());
    }

    private static CurvaABCItemDTO paraItemABC(CalculadoraCurvaABC.ItemClassificado i) {
        return new CurvaABCItemDTO(i.produtoId(), i.nome(), i.quantidadeConsumida(), i.valorConsumido(),
                i.percentualValor(), i.percentualAcumulado(), String.valueOf(i.classe()));
    }

    // ---------------------------------------------------------------- giro e cobertura

    public GiroDTO giro(LocalDate inicio, LocalDate fim) {
        Periodo periodo = Periodo.de(inicio, fim);
        Map<UUID, List<MovimentacaoEstoque>> porProduto = repositoryMovimentacaoEstoque.buscarAte(periodo.fimExclusivo()).stream()
                .collect(Collectors.groupingBy(m -> m.getProduto().getId()));

        List<GiroProdutoDTO> itens = new ArrayList<>();
        for (Estoque estoque : repositoryEstoque.buscarTodosComProduto()) {
            Produto produto = estoque.getProduto();
            List<MovimentacaoEstoque> movimentacoes = porProduto.getOrDefault(produto.getId(), List.of());

            int saldoInicial = 0;
            long vendido = 0;
            List<CalculadoraGiro.Evento> eventos = new ArrayList<>();
            for (MovimentacaoEstoque m : movimentacoes) {
                LocalDate data = m.getData_movimentacao().toLocalDate();
                if (data.isBefore(periodo.inicio())) {
                    saldoInicial += m.efeitoNoSaldo();
                } else {
                    eventos.add(new CalculadoraGiro.Evento(data, m.efeitoNoSaldo()));
                    if (m.getTipo() == TipoMovimentacaoEstoque.SAIDA && !m.isEstornada()) {
                        vendido += m.getQuantidade();
                    }
                }
            }
            double estoqueMedio = CalculadoraGiro.estoqueMedio(saldoInicial, eventos, periodo.inicio(), periodo.fim());
            Double giro = CalculadoraGiro.giro(vendido, estoqueMedio);
            Double diasEmEstoque = giro == null || giro == 0 ? null : periodo.dias() / giro;
            double demandaDiaria = (double) vendido / periodo.dias();
            itens.add(new GiroProdutoDTO(produto.getId(), produto.getNome(), produto.getCategoria().getNome(), vendido,
                    arredondar(estoqueMedio), arredondar(giro), arredondar(diasEmEstoque), estoque.getQuantidade(),
                    arredondar(PoliticaEstoque.coberturaEmDias(estoque.getQuantidade(), demandaDiaria))));
        }
        itens.sort(Comparator.comparing((GiroProdutoDTO g) -> g.giro() == null ? -1 : g.giro()).reversed());
        Double giroMedio = itens.stream().map(GiroProdutoDTO::giro).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().stream().boxed().findFirst().orElse(null);
        return new GiroDTO(periodo.inicio(), periodo.fim(), periodo.dias(), arredondar(giroMedio), itens);
    }

    // ---------------------------------------------------------------- conciliação (integridade)

    public ConciliacaoDTO conciliacao() {
        Map<UUID, List<MovimentacaoEstoque>> porProduto = repositoryMovimentacaoEstoque.buscarTodasOrdenadas().stream()
                .collect(Collectors.groupingBy(m -> m.getProduto().getId(), LinkedHashMap::new, Collectors.toList()));

        List<DivergenciaConciliacaoDTO> divergencias = new ArrayList<>();
        long movimentacoesVerificadas = 0;
        long quebrasTotais = 0;
        long negativos = 0;
        List<Estoque> estoques = repositoryEstoque.buscarTodosComProduto();
        for (Estoque estoque : estoques) {
            List<MovimentacaoEstoque> movimentacoes = porProduto.getOrDefault(estoque.getProduto().getId(), List.of());
            int saldoRecalculado = 0;
            int quebras = 0;
            Integer saldoEsperado = 0;
            for (MovimentacaoEstoque m : movimentacoes) {
                if (!Objects.equals(m.getSaldo_anterior(), saldoEsperado)
                        || !Objects.equals(m.getSaldo_posterior(), m.getSaldo_anterior() == null ? null : m.getSaldo_anterior() + m.efeitoNoSaldo())) {
                    quebras++;
                }
                saldoRecalculado += m.efeitoNoSaldo();
                saldoEsperado = m.getSaldo_posterior();
            }
            movimentacoesVerificadas += movimentacoes.size();
            quebrasTotais += quebras;
            if (estoque.getQuantidade() < 0) {
                negativos++;
            }
            if (saldoRecalculado != estoque.getQuantidade() || quebras > 0) {
                divergencias.add(new DivergenciaConciliacaoDTO(estoque.getProduto().getId(), estoque.getProduto().getNome(),
                        estoque.getQuantidade(), saldoRecalculado, quebras));
            }
        }
        return new ConciliacaoDTO(estoques.size(), estoques.size() - divergencias.size(), divergencias.size(),
                movimentacoesVerificadas, quebrasTotais, negativos, divergencias.isEmpty() && negativos == 0, divergencias);
    }

    // ---------------------------------------------------------------- série mensal

    public List<ResumoMensalDTO> resumoMensal(LocalDate inicio, LocalDate fim) {
        Periodo periodo = Periodo.de(inicio, fim);
        Map<YearMonth, List<MovimentacaoEstoque>> porMes = repositoryMovimentacaoEstoque.buscarAte(periodo.fimExclusivo()).stream()
                .filter(m -> !m.getData_movimentacao().isBefore(periodo.inicioInclusivo()))
                .collect(Collectors.groupingBy(m -> YearMonth.from(m.getData_movimentacao()), TreeMap::new, Collectors.toList()));

        List<ResumoMensalDTO> resultado = new ArrayList<>();
        porMes.forEach((mes, movimentacoes) -> {
            long entradas = 0, saidas = 0, devolucoes = 0, ajustes = 0, estornos = 0;
            BigDecimal faturamento = BigDecimal.ZERO;
            for (MovimentacaoEstoque m : movimentacoes) {
                // movimentação estornada foi anulada pelo seu estorno: não entra nos totais
                if (m.isEstornada()) {
                    continue;
                }
                switch (m.getTipo()) {
                    case ENTRADA -> entradas += m.getQuantidade();
                    case SAIDA -> {
                        saidas += m.getQuantidade();
                        faturamento = faturamento.add(m.getProduto().precoEmReais().multiply(BigDecimal.valueOf(m.getQuantidade())));
                    }
                    case DEVOLUCAO -> devolucoes += m.getQuantidade();
                    case AJUSTE -> ajustes += m.getQuantidade();
                    case ESTORNO -> estornos++;
                }
            }
            resultado.add(new ResumoMensalDTO(mes.toString(), movimentacoes.size(), entradas, saidas, devolucoes,
                    ajustes, estornos, faturamento));
        });
        return resultado;
    }

    // ---------------------------------------------------------------- painel

    public PainelDTO painel(LocalDate dataReferencia) {
        LocalDate referencia = dataReferencia != null ? dataReferencia : LocalDate.now();
        List<AlertaEstoqueDTO> situacoes = situacaoDosProdutos(referencia);
        Map<SituacaoEstoque, Long> porSituacao = new EnumMap<>(SituacaoEstoque.class);
        for (SituacaoEstoque s : SituacaoEstoque.values()) {
            porSituacao.put(s, situacoes.stream().filter(a -> a.situacao() == s).count());
        }

        List<Estoque> estoques = repositoryEstoque.buscarTodosComProduto().stream().filter(e -> e.getProduto().isAtivo()).toList();
        long unidades = estoques.stream().mapToLong(Estoque::getQuantidade).sum();
        BigDecimal valorTotal = estoques.stream()
                .map(e -> e.getProduto().precoEmReais().multiply(BigDecimal.valueOf(e.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorSuperestoque = estoques.stream()
                .filter(e -> e.getProduto().getEstoque_maximo() != null && e.getQuantidade() > e.getProduto().getEstoque_maximo())
                .map(e -> e.getProduto().precoEmReais().multiply(BigDecimal.valueOf(e.getQuantidade() - e.getProduto().getEstoque_maximo())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate inicio30 = referencia.minusDays(29);
        long movimentacoes30 = repositoryMovimentacaoEstoque.buscarAte(referencia.plusDays(1).atStartOfDay()).stream()
                .filter(m -> !m.getData_movimentacao().isBefore(inicio30.atStartOfDay())).count();
        CurvaABCDTO abc30 = curvaABC(inicio30, referencia);

        return new PainelDTO(referencia, estoques.size(), unidades, valorTotal, porSituacao, valorSuperestoque,
                movimentacoes30, abc30.valor_total_vendido(), abc30.itens().stream().limit(5).toList());
    }

    // ---------------------------------------------------------------- utilitários

    private Map<UUID, Long> somarPorProduto(TipoMovimentacaoEstoque tipo, LocalDateTime desde, LocalDateTime ate) {
        Map<UUID, Long> mapa = new HashMap<>();
        for (Object[] linha : repositoryMovimentacaoEstoque.somarQuantidadePorProduto(tipo, desde, ate)) {
            mapa.put((UUID) linha[0], ((Number) linha[1]).longValue());
        }
        return mapa;
    }

    private static BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return parte.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }

    private static Double arredondar(Double valor) {
        return valor == null ? null : Math.round(valor * 100.0) / 100.0;
    }

    private record Periodo(LocalDate inicio, LocalDate fim) {
        static Periodo de(LocalDate inicio, LocalDate fim) {
            LocalDate f = fim != null ? fim : LocalDate.now();
            LocalDate i = inicio != null ? inicio : f.minusDays(PERIODO_PADRAO_DIAS - 1);
            if (i.isAfter(f)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de início deve ser anterior ou igual à data de fim");
            }
            return new Periodo(i, f);
        }

        LocalDateTime inicioInclusivo() {
            return inicio.atStartOfDay();
        }

        LocalDateTime fimExclusivo() {
            return fim.plusDays(1).atStartOfDay();
        }

        long dias() {
            return ChronoUnit.DAYS.between(inicio, fim) + 1;
        }
    }
}
