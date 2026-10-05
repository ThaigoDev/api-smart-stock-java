package com.thai.finance.api.finance.api.config;

import com.thai.finance.api.finance.api.domain.entities.*;
import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import com.thai.finance.api.finance.api.respository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.math.BigInteger;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Configuration
@RequiredArgsConstructor
@Order(2)
public class DataSeederConfig implements CommandLineRunner {

    private final RepositoryCategoria repositoryCategoria;
    private final RepositoryFornecedor repositoryFornecedor;
    private final RepositoryProduto repositoryProduto;
    private final RepositoryEstoque repositoryEstoque;
    private final RepositoryMovimentacaoEstoque repositoryMovimentacaoEstoque;


    private final Random random = new Random(42);

    private static final Map<String, List<String>> PRODUTOS_POR_CATEGORIA = new LinkedHashMap<>();
    static {
        PRODUTOS_POR_CATEGORIA.put("Alimentos", List.of("Arroz", "Feijão", "Macarrão", "Açúcar", "Óleo de Soja", "Sal", "Farinha de Trigo", "Molho de Tomate"));
        PRODUTOS_POR_CATEGORIA.put("Bebidas", List.of("Refrigerante Cola", "Suco de Laranja", "Água Mineral", "Cerveja Pilsen", "Achocolatado", "Café em Pó"));
        PRODUTOS_POR_CATEGORIA.put("Higiene Pessoal", List.of("Sabonete", "Shampoo", "Creme Dental", "Papel Higiênico", "Desodorante", "Absorvente"));
        PRODUTOS_POR_CATEGORIA.put("Limpeza", List.of("Detergente", "Sabão em Pó", "Água Sanitária", "Amaciante", "Esponja de Aço", "Desinfetante"));
        PRODUTOS_POR_CATEGORIA.put("Laticínios", List.of("Leite Integral", "Queijo Mussarela", "Iogurte Natural", "Manteiga", "Requeijão"));
        PRODUTOS_POR_CATEGORIA.put("Padaria", List.of("Pão Francês", "Pão de Forma", "Bolo de Fubá", "Torrada", "Biscoito Recheado"));
        PRODUTOS_POR_CATEGORIA.put("Hortifruti", List.of("Banana", "Maçã", "Tomate", "Batata", "Cebola", "Alface"));
        PRODUTOS_POR_CATEGORIA.put("Papelaria", List.of("Caderno", "Caneta Azul", "Lápis", "Borracha", "Cola Branca", "Grampeador"));
    }

    private static final List<String> MARCAS = List.of("Tradição", "Sabor Real", "Bom Preço", "Master", "Nordeste", "Premium", "Casa Feliz", "Econômico");

    private static final List<String[]> FORNECEDORES = List.of(
            new String[]{"Distribuidora Central Ltda", "contato@distcentral.com.br", "31988887777"},
            new String[]{"Atacadão Minas Suprimentos", "vendas@atacadaominas.com.br", "31977776666"},
            new String[]{"Comercial Zona da Mata", "comercial@zonadamata.com.br", "31966665555"},
            new String[]{"Grupo Bom Abastecimento", "sac@bomabastecimento.com.br", "31955554444"},
            new String[]{"Nova Era Distribuição", "contato@novaeradist.com.br", "31944443333"}
    );

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (repositoryProduto.count() > 0) {
            System.out.println("[DataSeeder] Já existem produtos no banco, seed ignorado (idempotente).");
            return;
        }

        System.out.println("[DataSeeder] Iniciando geração da massa de dados sintética do Smart Stock...");

        List<Categoria> categorias = criarCategorias();
        List<Fornecedor> fornecedores = criarFornecedores();
        List<Produto> produtos = criarProdutos(categorias, fornecedores);
        Map<UUID, Estoque> estoquePorProduto = criarEstoqueInicial(produtos);
        ResumoMovimentacoes resumo = simularMovimentacoes(produtos, estoquePorProduto);

        System.out.println("=========== RESUMO DO SEED (usar na seção de Resultados do TCC) ===========");
        System.out.println("Categorias criadas: " + categorias.size());
        System.out.println("Fornecedores criados: " + fornecedores.size());
        System.out.println("Produtos criados: " + produtos.size());
        System.out.println("Movimentações aplicadas com sucesso: " + resumo.aplicadas);
        System.out.println("  - Entradas: " + resumo.entradas);
        System.out.println("  - Saídas: " + resumo.saidas);
        System.out.println("  - Devoluções: " + resumo.devolucoes);
        System.out.println("  - Ajustes: " + resumo.ajustes);
        System.out.println("Movimentações bloqueadas pela regra de negócio (estoque mínimo): " + resumo.bloqueadas);
        System.out.println("Período simulado: " + resumo.dataInicio + " até " + resumo.dataFim);
        System.out.println("=============================================================================");
    }

    private List<Categoria> criarCategorias() {
        List<Categoria> lista = new ArrayList<>();
        for (String nome : PRODUTOS_POR_CATEGORIA.keySet()) {
            Categoria categoria = new Categoria();
            categoria.setNome(nome);
            categoria.setDescricao("Categoria de " + nome.toLowerCase());
            lista.add(repositoryCategoria.save(categoria));
        }
        return lista;
    }

    private List<Fornecedor> criarFornecedores() {
        List<Fornecedor> lista = new ArrayList<>();
        for (String[] f : FORNECEDORES) {
            Fornecedor fornecedor = new Fornecedor();
            fornecedor.setNome(f[0]);
            fornecedor.setEmail(f[1]);
            fornecedor.setTelefone(f[2]);
            lista.add(repositoryFornecedor.save(fornecedor));
        }
        return lista;
    }

    private List<Produto> criarProdutos(List<Categoria> categorias, List<Fornecedor> fornecedores) {
        List<Produto> produtos = new ArrayList<>();
        int skuSeq = 1000;
        for (Categoria categoria : categorias) {
            List<String> nomesBase = PRODUTOS_POR_CATEGORIA.get(categoria.getNome());
            for (String nomeBase : nomesBase) {
                String marca = MARCAS.get(random.nextInt(MARCAS.size()));
                Produto produto = new Produto();
                produto.setNome(nomeBase + " " + marca);
                produto.setSku(prefixoSku(categoria.getNome()) + "-" + (skuSeq++));
                produto.setEstoque_minimo(5 + random.nextInt(16)); // 5 a 20
                produto.setCategoria(categoria);
                produto.setFornecedor(fornecedores.get(random.nextInt(fornecedores.size())));
                produto.setPreco(BigInteger.valueOf(300 + random.nextInt(4700))); // centavos: R$3,00 a R$50,00
                produto.setAtivo(true);
                produtos.add(repositoryProduto.save(produto));
            }
        }
        return produtos;
    }

    private String prefixoSku(String categoria) {
        String semAcento = Normalizer.normalize(categoria, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        StringBuilder sb = new StringBuilder();
        for (String palavra : semAcento.split(" ")) {
            if (!palavra.isEmpty()) sb.append(Character.toUpperCase(palavra.charAt(0)));
        }
        return sb.toString();
    }

    private Map<UUID, Estoque> criarEstoqueInicial(List<Produto> produtos) {
        Map<UUID, Estoque> mapa = new HashMap<>();
        for (Produto produto : produtos) {
            Estoque estoque = new Estoque();
            estoque.setProduto(produto);
            int quantidadeInicial = produto.getEstoque_minimo() * (3 + random.nextInt(4)); // 3x a 6x o mínimo
            estoque.setQuantidade(quantidadeInicial);
            mapa.put(produto.getId(), repositoryEstoque.save(estoque));
        }
        return mapa;
    }

    private ResumoMovimentacoes simularMovimentacoes(List<Produto> produtos, Map<UUID, Estoque> estoquePorProduto) {
        ResumoMovimentacoes resumo = new ResumoMovimentacoes();
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicio = agora.minusMonths(6);
        resumo.dataInicio = inicio.toLocalDate();
        resumo.dataFim = agora.toLocalDate();

        for (Produto produto : produtos) {
            Estoque estoque = estoquePorProduto.get(produto.getId());
            int quantidadeMovimentacoes = 15 + random.nextInt(26);

            for (int i = 0; i < quantidadeMovimentacoes; i++) {
                TipoMovimentacaoEstoque tipo = sortearTipo();
                int quantidade = sortearQuantidade(tipo, estoque, produto);
                LocalDateTime dataMovimentacao = dataAleatoriaEntre(inicio, agora);

                if (tipo == TipoMovimentacaoEstoque.SAIDA) {

                    boolean autorizado = quantidade < (estoque.getQuantidade() - produto.getEstoque_minimo());
                    if (!autorizado) {
                        resumo.bloqueadas++;
                        continue;
                    }
                    estoque.setQuantidade(estoque.getQuantidade() - quantidade);
                } else {
                    estoque.setQuantidade(estoque.getQuantidade() + quantidade);
                }

                repositoryEstoque.save(estoque);

                MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
                movimentacao.setProduto(produto);
                movimentacao.setTipo(tipo);
                movimentacao.setQuantidade(quantidade);
                movimentacao.setMotivo(motivoPara(tipo));
                movimentacao.setData_movimentacao(dataMovimentacao);
                repositoryMovimentacaoEstoque.save(movimentacao);

                resumo.aplicadas++;
                switch (tipo) {
                    case ENTRADA -> resumo.entradas++;
                    case SAIDA -> resumo.saidas++;
                    case DEVOLUCAO -> resumo.devolucoes++;
                    case AJUSTE -> resumo.ajustes++;
                }
            }
        }
        return resumo;
    }

    private TipoMovimentacaoEstoque sortearTipo() {
        int sorteio = random.nextInt(100);
        if (sorteio < 55) return TipoMovimentacaoEstoque.SAIDA;      // 55% vendas
        if (sorteio < 85) return TipoMovimentacaoEstoque.ENTRADA;    // 30% reposição
        if (sorteio < 93) return TipoMovimentacaoEstoque.AJUSTE;     // 8% ajustes de contagem
        return TipoMovimentacaoEstoque.DEVOLUCAO;                    // 7% devoluções
    }

    private int sortearQuantidade(TipoMovimentacaoEstoque tipo, Estoque estoque, Produto produto) {
        return switch (tipo) {
            case SAIDA -> 1 + random.nextInt(Math.max(1, estoque.getQuantidade() / 3));
            case ENTRADA -> produto.getEstoque_minimo() + random.nextInt(30);
            case DEVOLUCAO -> 1 + random.nextInt(5);
            case AJUSTE -> 1 + random.nextInt(10);
        };
    }

    private String motivoPara(TipoMovimentacaoEstoque tipo) {
        return switch (tipo) {
            case ENTRADA -> "Reposição de estoque junto ao fornecedor";
            case SAIDA -> "Venda ao consumidor final";
            case DEVOLUCAO -> "Devolução de cliente";
            case AJUSTE -> "Correção de contagem de inventário";
        };
    }

    private LocalDateTime dataAleatoriaEntre(LocalDateTime inicio, LocalDateTime fim) {
        long inicioEpoch = inicio.toEpochSecond(ZoneOffset.of("-03:00"));
        long fimEpoch = fim.toEpochSecond(ZoneOffset.of("-03:00"));
        long aleatorio = inicioEpoch + (long) (random.nextDouble() * (fimEpoch - inicioEpoch));
        return LocalDateTime.ofEpochSecond(aleatorio, 0, ZoneOffset.of("-03:00"));
    }

    private static class ResumoMovimentacoes {
        int aplicadas = 0;
        int entradas = 0;
        int saidas = 0;
        int devolucoes = 0;
        int ajustes = 0;
        int bloqueadas = 0;
        java.time.LocalDate dataInicio;
        java.time.LocalDate dataFim;
    }
}