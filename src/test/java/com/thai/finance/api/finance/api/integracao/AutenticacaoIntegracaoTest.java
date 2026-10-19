package com.thai.finance.api.finance.api.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autenticação de ponta a ponta com tokens JWT reais (assinados pela aplicação), sem simulação:
 * login, cadastro de conta e aplicação das permissões do perfil no token emitido.
 */
class AutenticacaoIntegracaoTest extends ApiTestBase {

    private org.springframework.test.web.servlet.ResultActions login(String email, String senha) throws Exception {
        return mvc.perform(post("/auth").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha)));
    }

    @Test
    @DisplayName("Administrador inicial faz login e o token emitido dá acesso às rotas de cadastro")
    void loginDoAdministrador() throws Exception {
        String token = campo(login("admin@admin.com", "admin331400")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acessToken", not(emptyString()))), "$.acessToken");

        mvc.perform(post("/categorias").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"Via token real\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Senha incorreta ou usuário inexistente retornam 401")
    void credenciaisInvalidas() throws Exception {
        login("admin@admin.com", "senha-errada").andExpect(status().isUnauthorized());
        login("ninguem@teste.com", "qualquer").andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Conta criada pelo cadastro recebe perfil de usuário: consulta, mas não cadastra")
    void contaNovaTemPerfilDeUsuario() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Operador\", \"email\": \"operador@teste.com\", \"senha\": \"caixa123\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Operador\", \"email\": \"operador@teste.com\", \"senha\": \"caixa123\"}"))
                .andExpect(status().isConflict());

        String token = campo(login("operador@teste.com", "caixa123").andExpect(status().isOk()), "$.acessToken");
        mvc.perform(get("/produtos").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(post("/categorias").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"Proibida\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Token adulterado é rejeitado com 401")
    void tokenAdulterado() throws Exception {
        String token = campo(login("admin@admin.com", "admin331400"), "$.acessToken");
        String adulterado = token.substring(0, token.length() - 4) + "AAAA";
        mvc.perform(get("/produtos").header("Authorization", "Bearer " + adulterado)).andExpect(status().isUnauthorized());
    }
}
