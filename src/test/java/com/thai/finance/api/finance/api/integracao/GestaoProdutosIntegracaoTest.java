package com.thai.finance.api.finance.api.integracao;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Posição de estoque, ativação de produtos, edição de categorias e autoria das movimentações. */
class GestaoProdutosIntegracaoTest extends ApiTestBase {

    private Map<String, Object> posicaoDe(String produtoId) throws Exception {
        String json = obter("/relatorios/posicao-estoque").andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> itens = JsonPath.read(json, "$[?(@.produto_id == '" + produtoId + "')]");
        assertThat(itens).hasSize(1);
        return itens.get(0);
    }

    @Test
    @DisplayName("Posição de estoque traz saldo, situação, valor e cadastros do produto")
    void posicaoDeEstoque() throws Exception {
        String produto = criarProdutoComEstoque(30);
        Map<String, Object> posicao = posicaoDe(produto);
        assertThat(posicao.get("saldo")).isEqualTo(30);
        assertThat(posicao.get("possui_estoque")).isEqualTo(true);
        assertThat(posicao.get("situacao")).isEqualTo("NORMAL");
        assertThat(((Number) posicao.get("valor_em_estoque")).doubleValue()).isEqualTo(300.0);
        assertThat(posicao.get("fornecedor")).isEqualTo("Distribuidora Teste");
    }

    @Test
    @DisplayName("Produto inativado deixa de aceitar movimentações e pode ser reativado")
    void inativarEReativar() throws Exception {
        String produto = criarProdutoComEstoque(10);
        mvc.perform(patch("/produtos/" + produto + "/status").with(comoAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
        movimentar(produto, "SAIDA", 1).andExpect(status().isUnprocessableContent());
        assertThat(posicaoDe(produto).get("ativo")).isEqualTo(false);

        mvc.perform(patch("/produtos/" + produto + "/status").with(comoUsuario())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\": true}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/produtos/" + produto + "/status").with(comoAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\": true}"))
                .andExpect(status().isOk());
        movimentar(produto, "SAIDA", 1).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Categoria pode ser renomeada pelo administrador")
    void editarCategoria() throws Exception {
        String categoria = criarCategoria();
        mvc.perform(put("/categorias/" + categoria).with(comoAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Hortifrúti\", \"descricao\": \"Frutas e verduras\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Hortifrúti"))
                .andExpect(jsonPath("$.descricao").value("Frutas e verduras"));
    }

    @Test
    @DisplayName("Cada movimentação registra o e-mail de quem a fez")
    void autoriaDasMovimentacoes() throws Exception {
        String produto = criarProdutoComEstoque(10);
        mvc.perform(post("/movimentacoesestoque")
                        .with(jwt().jwt(t -> t.claim("email", "caixa1@mercado.com")).authorities(new SimpleGrantedAuthority("SCOPE_usuario")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produto_id\": \"%s\", \"tipo\": \"SAIDA\", \"quantidade\": 2, \"motivo\": \"venda\"}".formatted(produto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrado_por").value("caixa1@mercado.com"));
        obter("/produtos/" + produto + "/extrato").andExpect(jsonPath("$[-1].registrado_por", containsString("caixa1")));
    }
}
