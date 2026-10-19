package com.thai.finance.api.finance.api.integracao;

import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import com.thai.finance.api.finance.api.exceptions.RegraNegocioException;
import com.thai.finance.api.finance.api.services.ServiceMovimentacaoEstoque;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integridade transacional (ACID): atomicidade das operações e isolamento sob concorrência. */
class TransacaoEConcorrenciaIntegracaoTest extends ApiTestBase {

    @Autowired
    private ServiceMovimentacaoEstoque serviceMovimentacaoEstoque;

    @Test
    @DisplayName("Atomicidade: se a gravação do histórico falha, a alteração de saldo é desfeita")
    void falhaNoMeioDaTransacaoDesfazTudo() throws Exception {
        String produto = criarProdutoComEstoque(40);
        // motivo excede a coluna VARCHAR(255): o banco recusa o INSERT da movimentação,
        // que ocorre depois de o saldo já ter sido alterado na mesma transação
        var requisicao = new MovimentacaoEstoqueRequisicaoDTO(UUID.fromString(produto), TipoMovimentacaoEstoque.SAIDA, 15, "x".repeat(300));

        assertThatThrownBy(() -> serviceMovimentacaoEstoque.registrar(requisicao, LocalDateTime.now()))
                .isNotInstanceOf(RegraNegocioException.class);

        assertThat(saldo(produto)).isEqualTo(40);
        obter("/produtos/" + produto + "/extrato").andExpect(jsonPath("$", hasSize(1)));
        obter("/relatorios/conciliacao").andExpect(jsonPath("$.consistente").value(true));
    }

    @Test
    @DisplayName("Isolamento: 20 saídas simultâneas de 10 unidades sobre saldo 100 aprovam exatamente 10")
    void saidasSimultaneasNaoNegativamOEstoque() throws Exception {
        String produto = criarProdutoComEstoque(100);
        int requisicoes = 20;
        ExecutorService executor = Executors.newFixedThreadPool(requisicoes);
        CountDownLatch largada = new CountDownLatch(1);
        AtomicInteger aprovadas = new AtomicInteger();
        AtomicInteger recusadas = new AtomicInteger();
        List<Throwable> errosInesperados = new CopyOnWriteArrayList<>();

        List<Future<?>> tarefas = new ArrayList<>();
        for (int i = 0; i < requisicoes; i++) {
            tarefas.add(executor.submit(() -> {
                try {
                    largada.await();
                    serviceMovimentacaoEstoque.salvar(new MovimentacaoEstoqueRequisicaoDTO(
                            UUID.fromString(produto), TipoMovimentacaoEstoque.SAIDA, 10, "venda concorrente"));
                    aprovadas.incrementAndGet();
                } catch (RegraNegocioException e) {
                    recusadas.incrementAndGet();
                } catch (Throwable t) {
                    errosInesperados.add(t);
                }
                return null;
            }));
        }
        largada.countDown();
        for (Future<?> tarefa : tarefas) {
            tarefa.get(60, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertThat(errosInesperados).isEmpty();
        assertThat(aprovadas.get()).isEqualTo(10);
        assertThat(recusadas.get()).isEqualTo(10);
        assertThat(saldo(produto)).isZero();
        obter("/produtos/" + produto + "/extrato").andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(11)));
        obter("/relatorios/conciliacao").andExpect(jsonPath("$.consistente").value(true));
    }

    @Test
    @DisplayName("Isolamento: entradas e saídas simultâneas preservam o saldo exato")
    void entradasESaidasSimultaneas() throws Exception {
        String produto = criarProdutoComEstoque(1000);
        int pares = 25;
        ExecutorService executor = Executors.newFixedThreadPool(16);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<?>> tarefas = new ArrayList<>();
        for (int i = 0; i < pares; i++) {
            for (TipoMovimentacaoEstoque tipo : List.of(TipoMovimentacaoEstoque.ENTRADA, TipoMovimentacaoEstoque.SAIDA)) {
                int quantidade = tipo == TipoMovimentacaoEstoque.ENTRADA ? 7 : 3;
                tarefas.add(executor.submit(() -> {
                    largada.await();
                    return serviceMovimentacaoEstoque.salvar(new MovimentacaoEstoqueRequisicaoDTO(
                            UUID.fromString(produto), tipo, quantidade, "concorrência"));
                }));
            }
        }
        largada.countDown();
        for (Future<?> tarefa : tarefas) {
            tarefa.get(60, TimeUnit.SECONDS);
        }
        executor.shutdown();

        // 1000 + 25 x 7 - 25 x 3 = 1100
        assertThat(saldo(produto)).isEqualTo(1100);
        obter("/relatorios/conciliacao").andExpect(jsonPath("$.consistente").value(true));
    }
}
