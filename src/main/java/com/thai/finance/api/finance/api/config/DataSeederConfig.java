package com.thai.finance.api.finance.api.config;

import com.thai.finance.api.finance.api.domain.dtos.EstoqueDTO.EstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRespostaDTO;
import com.thai.finance.api.finance.api.domain.entities.Categoria;
import com.thai.finance.api.finance.api.domain.entities.Fornecedor;
import com.thai.finance.api.finance.api.domain.entities.Produto;
import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import com.thai.finance.api.finance.api.exceptions.RegraNegocioException;
import com.thai.finance.api.finance.api.respository.RepositoryCategoria;
import com.thai.finance.api.finance.api.respository.RepositoryFornecedor;
import com.thai.finance.api.finance.api.respository.RepositoryProduto;
import com.thai.finance.api.finance.api.services.ServiceEstoque;
import com.thai.finance.api.finance.api.services.ServiceMovimentacaoEstoque;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.math.BigInteger;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Gera a massa de dados sintética do cenário experimental: um minimercado fictício operando por
 * algumas semanas (26 por padrão). Todas as movimentações passam pelo ServiceMovimentacaoEstoque, ou seja, pelas mesmas
 * regras de negócio e transações usadas pela API; as tentativas rejeitadas são contabilizadas no resumo.
 *
 * A simulação é determinística (semente fixa): para o mesmo período, os totais se repetem a cada execução.
 * O período padrão é o do experimento do TCC (ver smartstock.seed.data-fim no application.yaml).
 *
 * Não é @Transactional de propósito: cada movimentação roda em sua própria transação, como na API.
 */
@Configuration
@RequiredArgsConstructor
@Order(2)
@ConditionalOnProperty(name = "smartstock.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeederConfig implements CommandLineRunner {

    private final RepositoryCategoria repositoryCategoria;
    private final RepositoryFornecedor repositoryFornecedor;
    private final RepositoryProduto repositoryProduto;
    private final ServiceEstoque serviceEstoque;
    private final ServiceMovimentacaoEstoque serviceMovimentacaoEstoque;

    @Value("${smartstock.seed.data-fim:}")
    private String dataFimConfigurada;

    @Value("${smartstock.seed.semanas:26}")
    private int semanas;

    private final Random random = new Random(42);

    private record ItemCatalogo(String categoria, String nome, int precoCentavos, double demandaDiaria, boolean perecivel) {
    }

    private static final List<ItemCatalogo> CATALOGO = List.of(
            new ItemCatalogo("Mercearia", "Arroz Tipo 1 5kg", 2690, 9, false),
            new ItemCatalogo("Mercearia", "Feijão Carioca 1kg", 849, 8, false),
            new ItemCatalogo("Mercearia", "Macarrão Espaguete", 459, 6, false),
            new ItemCatalogo("Mercearia", "Açúcar Cristal 2kg", 589, 6, false),
            new ItemCatalogo("Mercearia", "Óleo de Soja 900ml", 749, 7, false),
            new ItemCatalogo("Mercearia", "Sal Refinado 1kg", 299, 2, false),
            new ItemCatalogo("Mercearia", "Farinha de Trigo 1kg", 549, 3, false),
            new ItemCatalogo("Mercearia", "Molho de Tomate", 289, 5, false),
            new ItemCatalogo("Mercearia", "Café Torrado 500g", 1890, 6, false),
            new ItemCatalogo("Mercearia", "Leite Condensado", 699, 3, false),
            new ItemCatalogo("Bebidas", "Refrigerante Cola 2L", 999, 10, false),
            new ItemCatalogo("Bebidas", "Suco de Uva 1L", 1290, 2, false),
            new ItemCatalogo("Bebidas", "Água Mineral 1,5L", 299, 12, false),
            new ItemCatalogo("Bebidas", "Cerveja Lata 350ml", 429, 18, false),
            new ItemCatalogo("Bebidas", "Achocolatado 400g", 899, 2, false),
            new ItemCatalogo("Bebidas", "Energético 473ml", 1099, 1, false),
            new ItemCatalogo("Higiene", "Sabonete 90g", 249, 6, false),
            new ItemCatalogo("Higiene", "Shampoo 350ml", 1690, 1.5, false),
            new ItemCatalogo("Higiene", "Creme Dental 90g", 549, 4, false),
            new ItemCatalogo("Higiene", "Papel Higiênico 12un", 2290, 4, false),
            new ItemCatalogo("Higiene", "Desodorante Aerosol", 1590, 1.5, false),
            new ItemCatalogo("Higiene", "Absorvente 8un", 699, 1, false),
            new ItemCatalogo("Limpeza", "Detergente 500ml", 289, 6, false),
            new ItemCatalogo("Limpeza", "Sabão em Pó 1kg", 1490, 2.5, false),
            new ItemCatalogo("Limpeza", "Água Sanitária 2L", 699, 3, false),
            new ItemCatalogo("Limpeza", "Amaciante 2L", 1790, 1.5, false),
            new ItemCatalogo("Limpeza", "Esponja de Aço 8un", 399, 1.5, false),
            new ItemCatalogo("Limpeza", "Desinfetante 2L", 1190, 1, false),
            new ItemCatalogo("Laticínios", "Leite Integral 1L", 549, 20, true),
            new ItemCatalogo("Laticínios", "Mussarela Fatiada", 1290, 3, true),
            new ItemCatalogo("Laticínios", "Iogurte Natural", 299, 4, true),
            new ItemCatalogo("Laticínios", "Manteiga 200g", 1290, 1.5, true),
            new ItemCatalogo("Laticínios", "Requeijão 200g", 899, 2, true),
            new ItemCatalogo("Padaria", "Pão Francês (un)", 80, 120, true),
            new ItemCatalogo("Padaria", "Pão de Forma 500g", 899, 4, true),
            new ItemCatalogo("Padaria", "Bolo de Fubá", 1490, 1, true),
            new ItemCatalogo("Padaria", "Torrada 160g", 599, 1, false),
            new ItemCatalogo("Padaria", "Biscoito Recheado", 349, 6, false),
            new ItemCatalogo("Hortifruti", "Banana Prata kg", 599, 10, true),
            new ItemCatalogo("Hortifruti", "Maçã Gala kg", 999, 5, true),
            new ItemCatalogo("Hortifruti", "Tomate kg", 899, 7, true),
            new ItemCatalogo("Hortifruti", "Batata kg", 649, 8, true),
            new ItemCatalogo("Hortifruti", "Cebola kg", 549, 6, true),
            new ItemCatalogo("Hortifruti", "Alface (un)", 349, 4, true),
            new ItemCatalogo("Congelados", "Pizza Congelada", 1890, 2, false),
            new ItemCatalogo("Congelados", "Sorvete 2L", 2490, 1, false),
            new ItemCatalogo("Congelados", "Hambúrguer 12un", 2290, 1.5, false),
            new ItemCatalogo("Congelados", "Batata Palito 1kg", 2390, 1, false),
            new ItemCatalogo("Bazar", "Pilha AA 2un", 1290, 0.5, false),
            new ItemCatalogo("Bazar", "Fósforo 10cx", 599, 0.5, false),
            new ItemCatalogo("Bazar", "Vela Branca 8un", 899, 0.2, false),
            new ItemCatalogo("Bazar", "Lâmpada LED 9W", 1290, 0.3, false),
            new ItemCatalogo("Bazar", "Carvão 3kg", 2190, 1, false),
            new ItemCatalogo("Papelaria", "Caderno 96 folhas", 1590, 0.3, false),
            new ItemCatalogo("Papelaria", "Caneta Azul", 199, 0.8, false),
            new ItemCatalogo("Papelaria", "Lápis Grafite", 149, 0.4, false),
            new ItemCatalogo("Papelaria", "Borracha Branca", 199, 0.2, false),
            new ItemCatalogo("Papelaria", "Cola Branca 90g", 399, 0.15, false),
            new ItemCatalogo("Papelaria", "Grampeador", 2490, 0.05, false)
    );

    // nome, e-mail, telefone, prazo de entrega em dias
    private static final List<Object[]> FORNECEDORES = List.of(
            new Object[]{"Distribuidora Central Ltda", "contato@distcentral.com.br", "31988887777", 3},
            new Object[]{"Atacadão Minas Suprimentos", "vendas@atacadaominas.com.br", "31977776666", 5},
            new Object[]{"Comercial Zona da Mata", "comercial@zonadamata.com.br", "31966665555", 7},
            new Object[]{"Grupo Bom Abastecimento", "sac@bomabastecimento.com.br", "31955554444", 4},
            new Object[]{"Nova Era Distribuição", "contato@novaeradist.com.br", "31944443333", 10},
            new Object[]{"Hortifruti Vale Verde", "pedidos@valeverde.com.br", "31933332222", 2}
    );

    // fatores de venda por dia da semana (segunda a domingo)
    private static final double[] FATOR_DIA_SEMANA = {0.90, 0.90, 1.00, 1.00, 1.15, 1.35, 0.70};
    private static final double CRESCIMENTO_MENSAL = 0.03;

    enum PoliticaReposicao {
        // compra ao atingir o ponto de pedido, até o estoque máximo
        ADEQUADA,
        // só compra quando o produto acaba: gera rupturas e vendas perdidas
        TARDIA,
        // compra o triplo do necessário: gera superestoque
        EXCESSIVA
    }

    private static final class ProdutoSimulado {
        Produto produto;
        ItemCatalogo item;
        PoliticaReposicao politica;
        int prazo;
        int saldo;
        Integer pedidoChegaNoDia;
        LocalDateTime ultimoRegistro = LocalDateTime.MIN;
        int quantidadePedida;
        final Map<Integer, Integer> devolucoesAgendadas = new HashMap<>();
    }

    private static final class Resumo {
        int saidas, entradas, devolucoes, ajustes, estornos, vendasParciais, pedidosCompra;
        int saidasBloqueadas, unidadesNaoVendidas, ajustesBloqueados;
        final Map<PoliticaReposicao, Integer> bloqueiosPorPolitica = new EnumMap<>(PoliticaReposicao.class);
    }

    @Override
    public void run(String... args) {
        if (repositoryProduto.count() > 0) {
            System.out.println("[DataSeeder] Já existem produtos no banco, seed ignorado (idempotente).");
            return;
        }
        LocalDate fim = dataFimConfigurada == null || dataFimConfigurada.isBlank()
                ? LocalDate.now().minusDays(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                : LocalDate.parse(dataFimConfigurada);
        LocalDate inicio = fim.minusWeeks(semanas).plusDays(1);
        long inicioExecucao = System.currentTimeMillis();
        System.out.println("[DataSeeder] Gerando cenário do minimercado de " + inicio + " a " + fim + "...");

        Map<String, Categoria> categorias = criarCategorias();
        List<Fornecedor> fornecedores = criarFornecedores();
        List<ProdutoSimulado> produtos = criarProdutos(categorias, fornecedores, inicio);
        Resumo resumo = new Resumo();

        int totalDias = (int) (fim.toEpochDay() - inicio.toEpochDay()) + 1;
        for (int dia = 0; dia < totalDias; dia++) {
            LocalDate data = inicio.plusDays(dia);
            for (ProdutoSimulado p : produtos) {
                simularDia(p, dia, data, resumo);
            }
            if (data.getDayOfMonth() == data.lengthOfMonth() || dia == totalDias - 1) {
                System.out.printf("[DataSeeder] %s concluído (%d saídas até aqui)%n", data.getMonth(), resumo.saidas);
            }
        }

        int saldoFinalTotal = produtos.stream().mapToInt(p -> p.saldo).sum();
        long segundos = (System.currentTimeMillis() - inicioExecucao) / 1000;
        System.out.println("=========== RESUMO DO SEED (usar na seção de Resultados do TCC) ===========");
        System.out.println("Período simulado: " + inicio + " a " + fim + " (" + totalDias + " dias)");
        System.out.println("Categorias: " + categorias.size() + " | Fornecedores: " + fornecedores.size() + " | Produtos: " + produtos.size());
        for (PoliticaReposicao politica : PoliticaReposicao.values()) {
            System.out.println("  Produtos com política " + politica + ": " + produtos.stream().filter(p -> p.politica == politica).count());
        }
        System.out.println("Movimentações registradas: " + (resumo.saidas + resumo.entradas + resumo.devolucoes + resumo.ajustes + resumo.estornos + produtos.size()));
        System.out.println("  - Saldos iniciais (ENTRADA): " + produtos.size());
        System.out.println("  - Entradas (recebimentos): " + resumo.entradas);
        System.out.println("  - Saídas (vendas): " + resumo.saidas + " (das quais " + resumo.vendasParciais + " vendas parciais)");
        System.out.println("  - Devoluções: " + resumo.devolucoes);
        System.out.println("  - Ajustes de inventário: " + resumo.ajustes);
        System.out.println("  - Estornos: " + resumo.estornos);
        System.out.println("Pedidos de compra gerados: " + resumo.pedidosCompra);
        System.out.println("Saídas bloqueadas por saldo insuficiente (HTTP 422 na API): " + resumo.saidasBloqueadas);
        resumo.bloqueiosPorPolitica.forEach((k, v) -> System.out.println("  - política " + k + ": " + v));
        System.out.println("Unidades demandadas e não atendidas (venda perdida): " + resumo.unidadesNaoVendidas);
        System.out.println("Ajustes bloqueados: " + resumo.ajustesBloqueados);
        System.out.println("Saldo final total (unidades): " + saldoFinalTotal);
        System.out.println("Tempo de geração: " + segundos + " s");
        System.out.println("=============================================================================");
    }

    // ------------------------------------------------------------------ cadastros

    private Map<String, Categoria> criarCategorias() {
        Map<String, Categoria> mapa = new LinkedHashMap<>();
        for (ItemCatalogo item : CATALOGO) {
            mapa.computeIfAbsent(item.categoria(), nome -> {
                Categoria categoria = new Categoria();
                categoria.setNome(nome);
                categoria.setDescricao("Produtos da seção " + nome.toLowerCase());
                return repositoryCategoria.save(categoria);
            });
        }
        return mapa;
    }

    private List<Fornecedor> criarFornecedores() {
        List<Fornecedor> lista = new ArrayList<>();
        for (Object[] f : FORNECEDORES) {
            Fornecedor fornecedor = new Fornecedor();
            fornecedor.setNome((String) f[0]);
            fornecedor.setEmail((String) f[1]);
            fornecedor.setTelefone((String) f[2]);
            lista.add(repositoryFornecedor.save(fornecedor));
        }
        return lista;
    }

    private List<ProdutoSimulado> criarProdutos(Map<String, Categoria> categorias, List<Fornecedor> fornecedores, LocalDate inicio) {
        List<ProdutoSimulado> lista = new ArrayList<>();
        int sequencia = 1000;
        for (ItemCatalogo item : CATALOGO) {
            // hortifrúti vem do fornecedor de 2 dias; demais itens de um distribuidor sorteado
            int indiceFornecedor = item.categoria().equals("Hortifruti") ? 5 : random.nextInt(5);
            Fornecedor fornecedor = fornecedores.get(indiceFornecedor);
            int prazo = (int) FORNECEDORES.get(indiceFornecedor)[3];

            int minimo = Math.max(2, (int) Math.ceil(item.demandaDiaria() * 3));
            int maximo = minimo + Math.max(3, (int) Math.ceil(item.demandaDiaria() * (prazo + 14)));

            Produto produto = new Produto();
            produto.setNome(item.nome());
            produto.setSku(prefixoSku(item.categoria()) + "-" + (sequencia++));
            produto.setEstoque_minimo(minimo);
            produto.setEstoque_maximo(maximo);
            produto.setPrazo_reposicao_dias(prazo);
            produto.setCategoria(categorias.get(item.categoria()));
            produto.setFornecedor(fornecedor);
            produto.setPreco(BigInteger.valueOf(item.precoCentavos()));
            produto.setAtivo(true);

            ProdutoSimulado p = new ProdutoSimulado();
            p.produto = repositoryProduto.save(produto);
            p.item = item;
            p.prazo = prazo;
            int sorteio = random.nextInt(100);
            p.politica = sorteio < 70 ? PoliticaReposicao.ADEQUADA : sorteio < 85 ? PoliticaReposicao.TARDIA : PoliticaReposicao.EXCESSIVA;
            p.saldo = (int) Math.round(maximo * (0.5 + random.nextDouble() * 0.5));
            serviceEstoque.salvar(new EstoqueRequisicaoDTO(p.produto.getId(), p.saldo), inicio.atTime(7, 0));
            lista.add(p);
        }
        return lista;
    }

    private String prefixoSku(String categoria) {
        String semAcento = Normalizer.normalize(categoria, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.substring(0, 3).toUpperCase();
    }

    // ------------------------------------------------------------------ simulação diária

    private void simularDia(ProdutoSimulado p, int dia, LocalDate data, Resumo resumo) {
        // 1. recebimento de pedido de compra (início do expediente)
        if (p.pedidoChegaNoDia != null && p.pedidoChegaNoDia == dia) {
            receberPedido(p, data.atTime(7, 30 + random.nextInt(30)), resumo);
        }

        // 2. vendas do dia, em 1 a 3 cupons distribuídos ao longo do expediente
        double fatorSemana = FATOR_DIA_SEMANA[data.getDayOfWeek().getValue() - 1];
        double tendencia = 1 + CRESCIMENTO_MENSAL * dia / 30.0;
        int demanda = poisson(p.item.demandaDiaria() * fatorSemana * tendencia);
        List<Integer> cupons = dividirEmCupons(demanda);
        List<LocalTime> horarios = horariosOrdenados(cupons.size() + 1);
        for (int i = 0; i < cupons.size(); i++) {
            vender(p, cupons.get(i), data.atTime(horarios.get(i)), resumo, dia);
        }

        // 3. devoluções agendadas
        Integer devolvidas = p.devolucoesAgendadas.remove(dia);
        if (devolvidas != null) {
            registrar(p, TipoMovimentacaoEstoque.DEVOLUCAO, devolvidas, "Devolução de cliente (troca/arrependimento)",
                    data.atTime(horarios.get(horarios.size() - 1)), resumo);
        }

        // 4. inventário mensal (último dia do mês)
        if (data.getDayOfMonth() == data.lengthOfMonth()) {
            inventario(p, data.atTime(21, 0), resumo);
        }

        // 5. fim do dia: avalia necessidade de compra conforme a política do produto
        avaliarReposicao(p, dia, resumo);
    }

    private void vender(ProdutoSimulado p, int quantidade, LocalDateTime quando, Resumo resumo, int dia) {
        try {
            registrar(p, TipoMovimentacaoEstoque.SAIDA, quantidade, "Venda ao consumidor final", quando, resumo);
            resumo.saidas++;
            if (random.nextDouble() < 0.015) {
                p.devolucoesAgendadas.merge(dia + 1 + random.nextInt(7), 1, Integer::sum);
            }
        } catch (RegraNegocioException e) {
            // a API recusou a saída (estoque ficaria negativo): o cliente leva apenas o que há na prateleira
            resumo.saidasBloqueadas++;
            resumo.bloqueiosPorPolitica.merge(p.politica, 1, Integer::sum);
            resumo.unidadesNaoVendidas += quantidade - Math.max(p.saldo, 0);
            if (p.saldo > 0) {
                registrar(p, TipoMovimentacaoEstoque.SAIDA, p.saldo, "Venda ao consumidor final (atendimento parcial)", quando.plusMinutes(1), resumo);
                resumo.saidas++;
                resumo.vendasParciais++;
            }
        }
    }

    private void receberPedido(ProdutoSimulado p, LocalDateTime quando, Resumo resumo) {
        String motivo = "Recebimento de NF - " + p.produto.getFornecedor().getNome();
        // em ~4% dos recebimentos o operador lança a quantidade em caixas (x12) e corrige com estorno
        if (random.nextDouble() < 0.04) {
            MovimentacaoEstoqueRespostaDTO errada = registrar(p, TipoMovimentacaoEstoque.ENTRADA, p.quantidadePedida * 12, motivo, quando, resumo);
            resumo.entradas++;
            MovimentacaoEstoqueRespostaDTO estorno = serviceMovimentacaoEstoque.estornar(errada.id(),
                    "Quantidade lançada em caixas em vez de unidades", proximoHorario(p, quando.plusMinutes(5)));
            p.saldo = estorno.saldo_posterior();
            resumo.estornos++;
            quando = quando.plusMinutes(6);
        }
        registrar(p, TipoMovimentacaoEstoque.ENTRADA, p.quantidadePedida, motivo, quando, resumo);
        resumo.entradas++;
        p.pedidoChegaNoDia = null;
    }

    private void inventario(ProdutoSimulado p, LocalDateTime quando, Resumo resumo) {
        int diferenca;
        String motivo;
        if (p.item.perecivel()) {
            diferenca = -(int) Math.round(p.saldo * (0.02 + random.nextDouble() * 0.06));
            motivo = "Inventário: perda por vencimento/avaria";
        } else if (random.nextDouble() < 0.3) {
            diferenca = random.nextBoolean() ? -(1 + random.nextInt(2)) : 1;
            motivo = "Inventário: divergência de contagem";
        } else {
            return;
        }
        if (diferenca == 0) {
            return;
        }
        try {
            registrar(p, TipoMovimentacaoEstoque.AJUSTE, diferenca, motivo, quando, resumo);
            resumo.ajustes++;
        } catch (RegraNegocioException e) {
            resumo.ajustesBloqueados++;
        }
    }

    private void avaliarReposicao(ProdutoSimulado p, int dia, Resumo resumo) {
        if (p.pedidoChegaNoDia != null) {
            return;
        }
        Produto produto = p.produto;
        int pontoDePedido = produto.politicaEstoque().pontoDePedido(p.item.demandaDiaria());
        int maximo = produto.getEstoque_maximo();
        int quantidade = switch (p.politica) {
            case ADEQUADA -> p.saldo <= pontoDePedido ? maximo - p.saldo + (int) Math.ceil(p.item.demandaDiaria() * p.prazo) : 0;
            case TARDIA -> p.saldo <= produto.getEstoque_minimo() / 2 ? maximo - p.saldo : 0;
            case EXCESSIVA -> p.saldo <= pontoDePedido ? maximo * 3 - p.saldo : 0;
        };
        if (quantidade > 0) {
            p.quantidadePedida = quantidade;
            p.pedidoChegaNoDia = dia + p.prazo;
            resumo.pedidosCompra++;
        }
    }

    private MovimentacaoEstoqueRespostaDTO registrar(ProdutoSimulado p, TipoMovimentacaoEstoque tipo, int quantidade,
                                                     String motivo, LocalDateTime quando, Resumo resumo) {
        MovimentacaoEstoqueRespostaDTO resposta = serviceMovimentacaoEstoque.registrar(
                new MovimentacaoEstoqueRequisicaoDTO(p.produto.getId(), tipo, quantidade, motivo), proximoHorario(p, quando));
        p.saldo = resposta.saldo_posterior();
        if (tipo == TipoMovimentacaoEstoque.DEVOLUCAO) {
            resumo.devolucoes++;
        }
        return resposta;
    }

    // garante ordem cronológica estrita das movimentações de cada produto (base da conciliação por encadeamento)
    private LocalDateTime proximoHorario(ProdutoSimulado p, LocalDateTime desejado) {
        LocalDateTime horario = desejado.isAfter(p.ultimoRegistro) ? desejado : p.ultimoRegistro.plusSeconds(1);
        p.ultimoRegistro = horario;
        return horario;
    }

    // ------------------------------------------------------------------ utilitários estatísticos

    private int poisson(double lambda) {
        if (lambda <= 0) {
            return 0;
        }
        if (lambda > 30) {
            // aproximação normal para demandas altas
            return Math.max(0, (int) Math.round(lambda + Math.sqrt(lambda) * random.nextGaussian()));
        }
        double limite = Math.exp(-lambda);
        double produto = random.nextDouble();
        int k = 0;
        while (produto > limite) {
            produto *= random.nextDouble();
            k++;
        }
        return k;
    }

    private List<Integer> dividirEmCupons(int demanda) {
        if (demanda <= 0) {
            return List.of();
        }
        int cupons = Math.min(demanda, 1 + random.nextInt(3));
        List<Integer> partes = new ArrayList<>();
        int restante = demanda;
        for (int i = cupons; i > 1; i--) {
            int parte = 1 + random.nextInt(restante - (i - 1));
            partes.add(parte);
            restante -= parte;
        }
        partes.add(restante);
        return partes;
    }

    private List<LocalTime> horariosOrdenados(int quantidade) {
        List<LocalTime> horarios = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            horarios.add(LocalTime.of(8, 0).plusMinutes(random.nextInt(12 * 60)));
        }
        Collections.sort(horarios);
        return horarios;
    }
}
