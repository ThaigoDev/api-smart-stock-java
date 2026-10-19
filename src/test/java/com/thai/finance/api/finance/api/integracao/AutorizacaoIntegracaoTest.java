package com.thai.finance.api.finance.api.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Autorização por perfil: o usuário comum opera o estoque, mas só o administrador altera cadastros e estorna. */
class AutorizacaoIntegracaoTest extends ApiTestBase {

    private org.springframework.test.web.servlet.ResultActions comoUsuarioPostar(String url, String json) throws Exception {
        return mvc.perform(post(url).with(comoUsuario()).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    @DisplayName("Usuário comum consulta produtos, relatórios e registra movimentações")
    void usuarioComumOperaOEstoque() throws Exception {
        String produto = criarProdutoComEstoque(20);
        mvc.perform(get("/produtos").with(comoUsuario())).andExpect(status().isOk());
        mvc.perform(get("/relatorios/alertas").with(comoUsuario())).andExpect(status().isOk());
        comoUsuarioPostar("/movimentacoesestoque", """
                {"produto_id": "%s", "tipo": "SAIDA", "quantidade": 2, "motivo": "venda no caixa"}""".formatted(produto))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Usuário comum recebe 403 ao cadastrar, alterar, excluir ou estornar")
    void usuarioComumNaoAlteraCadastros() throws Exception {
        String produto = criarProdutoComEstoque(20);
        String saida = campo(movimentar(produto, "SAIDA", 1), "$.id");

        comoUsuarioPostar("/categorias", """
                {"nome": "Proibida", "descricao": "x"}""").andExpect(status().isForbidden());
        comoUsuarioPostar("/estoques", """
                {"produto_id": "%s", "quantidade": 1}""".formatted(produto)).andExpect(status().isForbidden());
        comoUsuarioPostar("/movimentacoesestoque/" + saida + "/estorno", """
                {"motivo": "x"}""").andExpect(status().isForbidden());
        mvc.perform(put("/fornecedores/" + UUID.randomUUID()).with(comoUsuario())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/produtos/" + produto).with(comoUsuario())).andExpect(status().isForbidden());
        obter("/produtos/" + produto).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Documentação da API e painel web são públicos; os dados não")
    void rotasPublicas() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/index.html")).andExpect(status().isOk());
        mvc.perform(get("/relatorios/painel")).andExpect(status().isUnauthorized());
    }
}
