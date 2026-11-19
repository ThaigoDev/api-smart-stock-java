package com.thai.finance.api.finance.api.integracao;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Ciclo de reposição: pedido de compra → recebimento (entradas no estoque) ou cancelamento. */
class PedidoCompraIntegracaoTest extends ApiTestBase {

    private String criarProduto(String fornecedorId) throws Exception {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        return campo(postar("/produtos", """
                {"nome": "Item %1$s", "sku": "PC-%1$s", "estoque_minimo": 10, "estoque_maximo": 100,
                 "prazo_reposicao_dias": 5, "categoria_id": "%2$s", "preco": 500, "fornecedor_id": "%3$s"}"""
                .formatted(sufixo, criarCategoria(), fornecedorId)).andExpect(status().isCreated()), "$.id");
    }

    private void abrirEstoque(String produtoId, int saldo) throws Exception {
        postar("/estoques", """
                {"produto_id": "%s", "quantidade": %d}""".formatted(produtoId, saldo)).andExpect(status().isCreated());
    }

    private String criarPedido(String fornecedorId, String... produtoQuantidade) throws Exception {
        StringBuilder itens = new StringBuilder();
        for (int i = 0; i < produtoQuantidade.length; i += 2) {
            if (i > 0) itens.append(',');
            itens.append("{\"produto_id\": \"%s\", \"quantidade\": %s}".formatted(produtoQuantidade[i], produtoQuantidade[i + 1]));
        }
        return campo(postar("/pedidos-compra", """
                {"fornecedor_id": "%s", "observacao": "teste", "itens": [%s]}""".formatted(fornecedorId, itens))
                .andExpect(status().isCreated()), "$.id");
    }

    @Test
    @DisplayName("Receber o pedido lança uma ENTRADA por item e muda o status para RECEBIDO")
    void recebimentoGeraEntradas() throws Exception {
        String fornecedor = criarFornecedor();
        String arroz = criarProduto(fornecedor);
        String feijao = criarProduto(fornecedor);
        abrirEstoque(arroz, 8);
        String pedido = criarPedido(fornecedor, arroz, "40", feijao, "25");

        obter("/pedidos-compra/" + pedido)
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.codigo", startsWith("PC-")))
                .andExpect(jsonPath("$.quantidade_total").value(65))
                .andExpect(jsonPath("$.itens", hasSize(2)));

        postar("/pedidos-compra/" + pedido + "/recebimento", "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEBIDO"))
                .andExpect(jsonPath("$.recebido_em").isNotEmpty());

        assertThat(saldo(arroz)).isEqualTo(48);
        // o feijão não tinha estoque aberto: o recebimento abre o estoque e lança a entrada
        assertThat(saldo(feijao)).isEqualTo(25);
        obter("/produtos/" + arroz + "/extrato")
                .andExpect(jsonPath("$[-1].tipo").value("ENTRADA"))
                .andExpect(jsonPath("$[-1].motivo", containsString("Recebimento do pedido de compra PC-")));
        obter("/relatorios/conciliacao").andExpect(jsonPath("$.consistente").value(true));
    }

    @Test
    @DisplayName("Pedido recebido ou cancelado não pode ser recebido de novo (422)")
    void pedidoFinalizadoNaoMudaDeEstado() throws Exception {
        String fornecedor = criarFornecedor();
        String produto = criarProduto(fornecedor);
        abrirEstoque(produto, 0);
        String recebido = criarPedido(fornecedor, produto, "10");
        postar("/pedidos-compra/" + recebido + "/recebimento", "").andExpect(status().isOk());
        postar("/pedidos-compra/" + recebido + "/recebimento", "")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message", containsString("recebido")));
        assertThat(saldo(produto)).isEqualTo(10);

        String cancelado = criarPedido(fornecedor, produto, "5");
        postar("/pedidos-compra/" + cancelado + "/cancelamento", "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
        postar("/pedidos-compra/" + cancelado + "/recebimento", "").andExpect(status().isUnprocessableContent());
        assertThat(saldo(produto)).isEqualTo(10);
    }

    @Test
    @DisplayName("Recebimento é atômico: se um item falha, nenhuma entrada é lançada")
    void recebimentoAtomico() throws Exception {
        String fornecedor = criarFornecedor();
        String ativo = criarProduto(fornecedor);
        String inativado = criarProduto(fornecedor);
        abrirEstoque(ativo, 3);
        abrirEstoque(inativado, 3);
        String pedido = criarPedido(fornecedor, ativo, "20", inativado, "20");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/produtos/" + inativado + "/status")
                        .with(comoAdmin()).contentType(MediaType.APPLICATION_JSON).content("{\"ativo\": false}"))
                .andExpect(status().isOk());

        postar("/pedidos-compra/" + pedido + "/recebimento", "").andExpect(status().isUnprocessableContent());
        assertThat(saldo(ativo)).isEqualTo(3);
        obter("/pedidos-compra/" + pedido).andExpect(jsonPath("$.status").value("ABERTO"));
    }

    @Test
    @DisplayName("Itens de outro fornecedor, repetidos ou sem itens são recusados")
    void validacoesDoPedido() throws Exception {
        String fornecedor = criarFornecedor();
        String outroFornecedor = criarFornecedor();
        String produto = criarProduto(fornecedor);
        String deOutro = criarProduto(outroFornecedor);

        postar("/pedidos-compra", """
                {"fornecedor_id": "%s", "itens": [{"produto_id": "%s", "quantidade": 5}]}""".formatted(fornecedor, deOutro))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message", containsString("é fornecido por")));
        postar("/pedidos-compra", """
                {"fornecedor_id": "%1$s", "itens": [{"produto_id": "%2$s", "quantidade": 5}, {"produto_id": "%2$s", "quantidade": 1}]}"""
                .formatted(fornecedor, produto))
                .andExpect(status().isUnprocessableContent());
        postar("/pedidos-compra", """
                {"fornecedor_id": "%s", "itens": []}""".formatted(fornecedor))
                .andExpect(status().isBadRequest());
        postar("/pedidos-compra", """
                {"fornecedor_id": "%s", "itens": [{"produto_id": "%s", "quantidade": 0}]}""".formatted(fornecedor, produto))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Sugestões criam um pedido por fornecedor e não repetem produtos já pedidos")
    void sugestoesAPartirDosAlertas() throws Exception {
        String fornecedor = criarFornecedor();
        String critico = criarProduto(fornecedor);
        String normal = criarProduto(fornecedor);
        abrirEstoque(critico, 5);
        abrirEstoque(normal, 60);

        String json = postar("/pedidos-compra/sugestoes", "").andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> doFornecedor = JsonPath.read(json, "$[?(@.fornecedor_id == '" + fornecedor + "')]");
        assertThat(doFornecedor).hasSize(1);
        List<Map<String, Object>> itens = JsonPath.read(doFornecedor.get(0), "$.itens");
        assertThat(itens).hasSize(1);
        assertThat(itens.get(0).get("produto_id")).isEqualTo(critico);
        // sem vendas recentes, a sugestão recompõe o estoque máximo: 100 - 5
        assertThat(itens.get(0).get("quantidade")).isEqualTo(95);

        String segunda = postar("/pedidos-compra/sugestoes", "").andReturn().getResponse().getContentAsString();
        assertThat((List<?>) JsonPath.read(segunda, "$[?(@.fornecedor_id == '" + fornecedor + "')]")).isEmpty();
    }

    @Test
    @DisplayName("Usuário comum consulta pedidos, mas não cria, recebe nem cancela (403)")
    void autorizacao() throws Exception {
        String fornecedor = criarFornecedor();
        String produto = criarProduto(fornecedor);
        String pedido = criarPedido(fornecedor, produto, "3");

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/pedidos-compra").with(comoUsuario()))
                .andExpect(status().isOk());
        mvc.perform(post("/pedidos-compra/" + pedido + "/recebimento").with(comoUsuario())).andExpect(status().isForbidden());
        mvc.perform(post("/pedidos-compra/sugestoes").with(comoUsuario())).andExpect(status().isForbidden());
    }
}
