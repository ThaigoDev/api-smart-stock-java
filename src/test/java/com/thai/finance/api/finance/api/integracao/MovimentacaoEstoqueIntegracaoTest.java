package com.thai.finance.api.finance.api.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Corretude funcional: regras de saldo, alertas e estorno, exercitadas pela API HTTP. */
class MovimentacaoEstoqueIntegracaoTest extends ApiTestBase {

    @Test
    @DisplayName("Estoque inicial é registrado como ENTRADA no histórico")
    void saldoInicialGeraEntrada() throws Exception {
        String produto = criarProdutoComEstoque(40);
        obter("/produtos/" + produto + "/extrato")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("ENTRADA"))
                .andExpect(jsonPath("$[0].saldo_anterior").value(0))
                .andExpect(jsonPath("$[0].saldo_posterior").value(40));
    }

    @Test
    @DisplayName("Saída válida retorna 201 com saldo anterior, posterior e situação do estoque")
    void saidaValida() throws Exception {
        String produto = criarProdutoComEstoque(50);
        // 40 vendidos em 30 dias => 1,33/dia; ponto de pedido = ceil(1,33 x 5) + 10 = 17; saldo 10 => REPOR
        movimentar(produto, "SAIDA", 40)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo_anterior").value(50))
                .andExpect(jsonPath("$.saldo_posterior").value(10))
                .andExpect(jsonPath("$.situacao_estoque").value("REPOR"));
    }

    @Test
    @DisplayName("Saída maior que o saldo retorna 422 e não altera o estoque")
    void saidaComSaldoInsuficiente() throws Exception {
        String produto = criarProdutoComEstoque(20);
        movimentar(produto, "SAIDA", 21)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value(422))
                .andExpect(jsonPath("$.message", containsString("Saldo insuficiente")));

        assertThat(saldo(produto)).isEqualTo(20);
        obter("/produtos/" + produto + "/extrato").andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Saída que zera o estoque é aceita e sinaliza RUPTURA")
    void saidaQueZeraEstoque() throws Exception {
        String produto = criarProdutoComEstoque(15);
        movimentar(produto, "SAIDA", 15)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo_posterior").value(0))
                .andExpect(jsonPath("$.situacao_estoque").value("RUPTURA"));
        movimentar(produto, "SAIDA", 1).andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("Entrada acima do estoque máximo sinaliza SUPERESTOQUE")
    void superestoque() throws Exception {
        String produto = criarProdutoComEstoque(90);
        movimentar(produto, "ENTRADA", 20)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo_posterior").value(110))
                .andExpect(jsonPath("$.situacao_estoque").value("SUPERESTOQUE"));
    }

    @Test
    @DisplayName("Ajuste aceita quantidade negativa (perda) e devolução soma ao saldo")
    void ajusteEDevolucao() throws Exception {
        String produto = criarProdutoComEstoque(50);
        movimentar(produto, "AJUSTE", -7).andExpect(status().isCreated()).andExpect(jsonPath("$.saldo_posterior").value(43));
        movimentar(produto, "DEVOLUCAO", 2).andExpect(status().isCreated()).andExpect(jsonPath("$.saldo_posterior").value(45));
        movimentar(produto, "AJUSTE", -46).andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("Estorno desfaz a movimentação, preserva a original e não pode ser repetido")
    void estorno() throws Exception {
        String produto = criarProdutoComEstoque(50);
        String saida = campo(movimentar(produto, "SAIDA", 30).andExpect(status().isCreated()), "$.id");

        postar("/movimentacoesestoque/" + saida + "/estorno", """
                {"motivo": "venda cancelada no caixa"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ESTORNO"))
                .andExpect(jsonPath("$.quantidade").value(30))
                .andExpect(jsonPath("$.saldo_posterior").value(50))
                .andExpect(jsonPath("$.movimentacao_origem_id").value(saida));

        obter("/movimentacoesestoque/" + saida).andExpect(jsonPath("$.estornada").value(true));
        postar("/movimentacoesestoque/" + saida + "/estorno", """
                {"motivo": "de novo"}""").andExpect(status().isUnprocessableContent());
        obter("/produtos/" + produto + "/extrato").andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("Estorno de movimentação inexistente retorna 404")
    void estornoInexistente() throws Exception {
        postar("/movimentacoesestoque/" + UUID.randomUUID() + "/estorno", """
                {"motivo": "x"}""").andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Filtro de movimentações por produto e tipo")
    void filtroDeMovimentacoes() throws Exception {
        String produto = criarProdutoComEstoque(50);
        movimentar(produto, "SAIDA", 1).andExpect(status().isCreated());
        movimentar(produto, "SAIDA", 2).andExpect(status().isCreated());
        movimentar(produto, "ENTRADA", 3).andExpect(status().isCreated());

        obter("/movimentacoesestoque?produto_id=" + produto + "&tipo=SAIDA")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].tipo", everyItem(is("SAIDA"))));
    }

    @Test
    @DisplayName("Movimentações não podem ser editadas nem excluídas (histórico imutável)")
    void historicoImutavel() throws Exception {
        String produto = criarProdutoComEstoque(5);
        String id = campo(movimentar(produto, "ENTRADA", 1), "$.id");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/movimentacoesestoque/" + id)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(status().isMethodNotAllowed());
    }
}
