package com.thai.finance.api.finance.api.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Confiabilidade da API: cada tipo de erro recebe um código HTTP distinto e uma mensagem explicativa. */
class ConfiabilidadeApiIntegracaoTest extends ApiTestBase {

    @Test
    @DisplayName("Requisição sem token é rejeitada com 401")
    void semAutenticacao() throws Exception {
        mvc.perform(get("/produtos")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Produto malformado retorna 400 com uma mensagem por campo inválido")
    void produtoMalformado() throws Exception {
        postar("/produtos", """
                {"nome": "abc", "sku": "", "estoque_minimo": -1, "preco": -5, "estoque_maximo": 0}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(7))))
                .andExpect(jsonPath("$[*].error", hasItems("nome", "sku", "estoque_minimo", "categoria_id", "fornecedor_id", "preco", "estoque_maximo")));
    }

    @Test
    @DisplayName("Estoque máximo menor que o mínimo é rejeitado com 400")
    void faixaDeEstoqueInvalida() throws Exception {
        postar("/produtos", """
                {"nome": "Produto X1", "sku": "SKU-X1", "estoque_minimo": 50, "estoque_maximo": 10,
                 "categoria_id": "%s", "preco": 100, "fornecedor_id": "%s"}""".formatted(UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message", containsString("estoque_maximo")));
    }

    @Test
    @DisplayName("Produto com categoria inexistente retorna 404")
    void categoriaInexistente() throws Exception {
        postar("/produtos", """
                {"nome": "Produto X2", "sku": "SKU-X2", "estoque_minimo": 5, "categoria_id": "%s",
                 "preco": 100, "fornecedor_id": "%s"}""".formatted(UUID.randomUUID(), criarFornecedor()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Categoria não encontrada"));
    }

    @Test
    @DisplayName("Movimentação para produto inexistente retorna 404")
    void movimentacaoProdutoInexistente() throws Exception {
        movimentar(UUID.randomUUID().toString(), "ENTRADA", 5).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Quantidade zero ou negativa (fora do AJUSTE) retorna 400")
    void quantidadeInvalida() throws Exception {
        String produto = criarProdutoComEstoque(10);
        movimentar(produto, "SAIDA", 0).andExpect(status().isBadRequest());
        movimentar(produto, "ENTRADA", -3).andExpect(status().isBadRequest());
        movimentar(produto, "AJUSTE", 0).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Tipo inexistente ou ESTORNO direto retornam 400")
    void tipoInvalido() throws Exception {
        String produto = criarProdutoComEstoque(10);
        movimentar(produto, "VENDA", 1).andExpect(status().isBadRequest());
        movimentar(produto, "ESTORNO", 1)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message", containsString("estorno")));
    }

    @Test
    @DisplayName("JSON sintaticamente inválido retorna 400")
    void jsonInvalido() throws Exception {
        mvc.perform(post("/movimentacoesestoque").with(jwt()).contentType(MediaType.APPLICATION_JSON).content("{\"produto_id\": "))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("UUID malformado na URL retorna 400")
    void uuidMalformado() throws Exception {
        obter("/produtos/nao-e-um-uuid").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Segundo estoque para o mesmo produto retorna 409")
    void estoqueDuplicado() throws Exception {
        String produto = criarProdutoComEstoque(10);
        postar("/estoques", """
                {"produto_id": "%s", "quantidade": 5}""".formatted(produto))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Excluir produto com histórico retorna 409 e preserva os dados")
    void exclusaoComVinculos() throws Exception {
        String produto = criarProdutoComEstoque(10);
        mvc.perform(delete("/produtos/" + produto).with(jwt())).andExpect(status().isConflict());
        obter("/produtos/" + produto).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Pesquisa sem categoria não falha e categoria malformada retorna 400")
    void pesquisa() throws Exception {
        criarProdutoComEstoque(1);
        obter("/produtos/pesquisar?nome=produto").andExpect(status().isOk()).andExpect(jsonPath("$", not(empty())));
        obter("/produtos/pesquisar?categoria=xyz").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Período de relatório invertido retorna 400")
    void periodoInvertido() throws Exception {
        obter("/relatorios/curva-abc?inicio=2026-05-01&fim=2026-04-01").andExpect(status().isBadRequest());
    }
}
