package com.thai.finance.api.finance.api.integracao;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Indicadores gerenciais calculados a partir do histórico de movimentações. */
class RelatorioIntegracaoTest extends ApiTestBase {

    private Map<String, Object> alertaDe(String produtoId) throws Exception {
        String json = obter("/relatorios/alertas?todos=true").andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> itens = JsonPath.read(json, "$[?(@.produto_id == '" + produtoId + "')]");
        assertThat(itens).hasSize(1);
        return itens.get(0);
    }

    @Test
    @DisplayName("Alertas identificam ruptura, superestoque e sugerem compra até o estoque máximo")
    void alertas() throws Exception {
        String emRuptura = criarProdutoComEstoque(12);
        movimentar(emRuptura, "SAIDA", 12).andExpect(status().isCreated());
        String emExcesso = criarProdutoComEstoque(150);
        String normal = criarProdutoComEstoque(60);

        Map<String, Object> ruptura = alertaDe(emRuptura);
        assertThat(ruptura.get("situacao")).isEqualTo("RUPTURA");
        assertThat(ruptura.get("sugestao_compra")).isEqualTo(100);
        assertThat(ruptura.get("valor_sugestao_compra")).asString().startsWith("1000");

        assertThat(alertaDe(emExcesso).get("situacao")).isEqualTo("SUPERESTOQUE");
        assertThat(alertaDe(normal).get("situacao")).isEqualTo("NORMAL");

        // sem o parâmetro "todos", apenas produtos que exigem atenção são listados
        obter("/relatorios/alertas")
                .andExpect(jsonPath("$[*].produto_id", hasItems(emRuptura, emExcesso)))
                .andExpect(jsonPath("$[*].produto_id", not(hasItem(normal))));
    }

    @Test
    @DisplayName("Curva ABC classifica pelo valor vendido e resume as três classes")
    void curvaABC() throws Exception {
        String campeao = criarProdutoComEstoque(500);
        movimentar(campeao, "SAIDA", 400).andExpect(status().isCreated());

        obter("/relatorios/curva-abc")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo_por_classe", hasSize(3)))
                .andExpect(jsonPath("$.resumo_por_classe[*].classe", contains("A", "B", "C")))
                .andExpect(jsonPath("$.itens[?(@.produto_id == '" + campeao + "')].classe", contains("A")))
                .andExpect(jsonPath("$.itens[?(@.produto_id == '" + campeao + "')].quantidade_vendida", contains(400)));
    }

    @Test
    @DisplayName("Giro de estoque considera as vendas e o estoque médio do período")
    void giro() throws Exception {
        String produto = criarProdutoComEstoque(100);
        movimentar(produto, "SAIDA", 50).andExpect(status().isCreated());

        String json = obter("/relatorios/giro").andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> itens = JsonPath.read(json, "$.itens[?(@.produto_id == '" + produto + "')]");
        assertThat(itens.get(0).get("quantidade_vendida")).isEqualTo(50);
        // saldo 0 nos 89 dias anteriores e 50 no dia de hoje: estoque médio 50/90
        assertThat(((Number) itens.get(0).get("estoque_medio")).doubleValue()).isCloseTo(0.56, org.assertj.core.data.Offset.offset(0.01));
        assertThat(((Number) itens.get(0).get("giro")).doubleValue()).isGreaterThan(1);
    }

    @Test
    @DisplayName("Conciliação confirma que todo saldo é igual à soma do seu histórico")
    void conciliacao() throws Exception {
        String produto = criarProdutoComEstoque(30);
        movimentar(produto, "SAIDA", 10).andExpect(status().isCreated());
        movimentar(produto, "AJUSTE", -2).andExpect(status().isCreated());
        movimentar(produto, "SAIDA", 99).andExpect(status().isUnprocessableContent());
        String entrada = campo(movimentar(produto, "ENTRADA", 5), "$.id");
        postar("/movimentacoesestoque/" + entrada + "/estorno", "{\"motivo\": \"nota cancelada\"}").andExpect(status().isCreated());

        obter("/relatorios/conciliacao")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consistente").value(true))
                .andExpect(jsonPath("$.produtos_divergentes").value(0))
                .andExpect(jsonPath("$.quebras_de_encadeamento").value(0))
                .andExpect(jsonPath("$.produtos_com_saldo_negativo").value(0));
    }

    @Test
    @DisplayName("Painel consolida valor em estoque, situação dos produtos e vendas recentes")
    void painel() throws Exception {
        criarProdutoComEstoque(10);
        obter("/relatorios/painel")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtos_ativos", greaterThan(0)))
                .andExpect(jsonPath("$.produtos_por_situacao.RUPTURA").exists())
                .andExpect(jsonPath("$.valor_total_em_estoque").isNumber());
    }

    @Test
    @DisplayName("Série mensal agrega movimentações e faturamento por mês")
    void movimentacoesMensais() throws Exception {
        String produto = criarProdutoComEstoque(20);
        movimentar(produto, "SAIDA", 3).andExpect(status().isCreated());
        obter("/relatorios/movimentacoes-mensais")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[-1].unidades_saida", greaterThanOrEqualTo(3)));
    }
}
