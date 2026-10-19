package com.thai.finance.api.finance.api.integracao;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base dos testes de integração: sobe a aplicação completa (controllers, serviços, JPA e segurança)
 * sobre um H2 em memória e envia requisições HTTP autenticadas com um JWT de teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class ApiTestBase {

    private static final AtomicInteger SEQUENCIA = new AtomicInteger(10_000);

    @Autowired
    protected MockMvc mvc;

    /** Token de administrador: pode cadastrar, alterar, excluir e estornar. */
    protected static JwtRequestPostProcessor comoAdmin() {
        return jwt().authorities(new SimpleGrantedAuthority("SCOPE_admin"));
    }

    /** Token de usuário comum (operador de caixa): consulta e registra movimentações. */
    protected static JwtRequestPostProcessor comoUsuario() {
        return jwt().authorities(new SimpleGrantedAuthority("SCOPE_usuario"));
    }

    protected ResultActions postar(String url, String json) throws Exception {
        return mvc.perform(post(url).with(comoAdmin()).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions obter(String url) throws Exception {
        return mvc.perform(get(url).with(comoAdmin()));
    }

    protected ResultActions excluir(String url) throws Exception {
        return mvc.perform(delete(url).with(comoAdmin()));
    }

    protected static String campo(ResultActions resultado, String caminho) throws Exception {
        Object valor = JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), caminho);
        return String.valueOf(valor);
    }

    protected String criarCategoria() throws Exception {
        return campo(postar("/categorias", """
                {"nome": "Mercearia %d", "descricao": "teste"}""".formatted(SEQUENCIA.incrementAndGet()))
                .andExpect(status().isCreated()), "$.id");
    }

    protected String criarFornecedor() throws Exception {
        return campo(postar("/fornecedores", """
                {"nome": "Distribuidora Teste", "email": "contato@teste.com.br", "telefone": "31999998888"}""")
                .andExpect(status().isCreated()), "$.id");
    }

    /** Cria produto (mínimo 10, máximo 100, prazo 5 dias, R$ 10,00) com estoque inicial. */
    protected String criarProdutoComEstoque(int saldoInicial) throws Exception {
        String produtoId = campo(postar("/produtos", """
                {"nome": "Produto %1$d", "sku": "SKU-%1$d", "estoque_minimo": 10, "estoque_maximo": 100,
                 "prazo_reposicao_dias": 5, "categoria_id": "%2$s", "preco": 1000, "fornecedor_id": "%3$s"}"""
                .formatted(SEQUENCIA.incrementAndGet(), criarCategoria(), criarFornecedor()))
                .andExpect(status().isCreated()), "$.id");
        postar("/estoques", """
                {"produto_id": "%s", "quantidade": %d}""".formatted(produtoId, saldoInicial))
                .andExpect(status().isCreated());
        return produtoId;
    }

    protected ResultActions movimentar(String produtoId, String tipo, int quantidade) throws Exception {
        return postar("/movimentacoesestoque", """
                {"produto_id": "%s", "tipo": "%s", "quantidade": %d, "motivo": "teste automatizado"}"""
                .formatted(produtoId, tipo, quantidade));
    }

    protected int saldo(String produtoId) throws Exception {
        String extrato = obter("/produtos/" + produtoId + "/extrato").andReturn().getResponse().getContentAsString();
        java.util.List<Integer> saldos = JsonPath.read(extrato, "$[*].saldo_posterior");
        return saldos.isEmpty() ? 0 : saldos.get(saldos.size() - 1);
    }
}
