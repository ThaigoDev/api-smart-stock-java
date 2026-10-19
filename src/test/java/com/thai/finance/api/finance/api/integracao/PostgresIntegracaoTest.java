package com.thai.finance.api.finance.api.integracao;

import com.thai.finance.api.finance.api.domain.dtos.EstoqueDTO.EstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.entities.Categoria;
import com.thai.finance.api.finance.api.domain.entities.Fornecedor;
import com.thai.finance.api.finance.api.domain.entities.Produto;
import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import com.thai.finance.api.finance.api.exceptions.RegraNegocioException;
import com.thai.finance.api.finance.api.respository.RepositoryCategoria;
import com.thai.finance.api.finance.api.respository.RepositoryFornecedor;
import com.thai.finance.api.finance.api.respository.RepositoryProduto;
import com.thai.finance.api.finance.api.services.ServiceEstoque;
import com.thai.finance.api.finance.api.services.ServiceMovimentacaoEstoque;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Executa a aplicação sobre um PostgreSQL real, criado em contêiner Docker a cada execução:
 * o esquema vem das migrações do Flyway e o Hibernate valida que ele corresponde às entidades.
 * Sem Docker disponível, a classe é ignorada e o restante da suíte (H2) continua rodando.
 */
@SpringBootTest
@ActiveProfiles("postgres-test")
@Testcontainers(disabledWithoutDocker = true)
class PostgresIntegracaoTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private RepositoryCategoria repositoryCategoria;
    @Autowired
    private RepositoryFornecedor repositoryFornecedor;
    @Autowired
    private RepositoryProduto repositoryProduto;
    @Autowired
    private ServiceEstoque serviceEstoque;
    @Autowired
    private ServiceMovimentacaoEstoque serviceMovimentacaoEstoque;

    private UUID criarProdutoComEstoque(int saldo) {
        Categoria categoria = new Categoria();
        categoria.setNome("Mercearia");
        Fornecedor fornecedor = new Fornecedor("Distribuidora Teste");
        Produto produto = new Produto();
        produto.setNome("Produto " + UUID.randomUUID().toString().substring(0, 8));
        produto.setSku("SKU-" + UUID.randomUUID().toString().substring(0, 8));
        produto.setEstoque_minimo(10);
        produto.setEstoque_maximo(100);
        produto.setPreco(BigInteger.valueOf(1000));
        produto.setCategoria(repositoryCategoria.save(categoria));
        produto.setFornecedor(repositoryFornecedor.save(fornecedor));
        UUID id = repositoryProduto.save(produto).getId();
        serviceEstoque.salvar(new EstoqueRequisicaoDTO(id, saldo));
        return id;
    }

    @Test
    @DisplayName("Flyway aplica as migrações V1 e V2 e o Hibernate valida o esquema sem divergências")
    void migracoesAplicadas() {
        Integer versao = jdbc.queryForObject(
                "select max(cast(version as integer)) from flyway_schema_history where success", Integer.class);
        assertThat(versao).isEqualTo(2);
        assertThat(jdbc.queryForList("select tablename from pg_tables where schemaname = 'public'", String.class))
                .contains("produto", "estoque", "movimentacao_estoque", "categoria", "fornecedor", "usuario");
    }

    @Test
    @DisplayName("PostgreSQL: 20 saídas simultâneas de 10 unidades sobre saldo 100 aprovam exatamente 10")
    void concorrenciaNoPostgres() throws Exception {
        UUID produto = criarProdutoComEstoque(100);
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch largada = new CountDownLatch(1);
        AtomicInteger aprovadas = new AtomicInteger();
        AtomicInteger recusadas = new AtomicInteger();
        List<Future<?>> tarefas = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            tarefas.add(executor.submit(() -> {
                largada.await();
                try {
                    serviceMovimentacaoEstoque.salvar(new MovimentacaoEstoqueRequisicaoDTO(
                            produto, TipoMovimentacaoEstoque.SAIDA, 10, "venda concorrente"));
                    aprovadas.incrementAndGet();
                } catch (RegraNegocioException e) {
                    recusadas.incrementAndGet();
                }
                return null;
            }));
        }
        largada.countDown();
        for (Future<?> tarefa : tarefas) {
            tarefa.get(60, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertThat(aprovadas.get()).isEqualTo(10);
        assertThat(recusadas.get()).isEqualTo(10);
        assertThat(jdbc.queryForObject("select quantidade from estoque where produto_id = ?", Integer.class, produto)).isZero();
        // mesma verificação da auditoria SQL: saldo armazenado = soma do histórico
        Integer recalculado = jdbc.queryForObject("""
                select sum(case when tipo = 'SAIDA' then -quantidade else quantidade end)
                from movimentacao_estoque where produto_id = ?""", Integer.class, produto);
        assertThat(recalculado).isZero();
    }

    @Test
    @DisplayName("A restrição UNIQUE(produto, sequência) impede duas movimentações na mesma posição do kardex")
    void restricaoDeSequencia() {
        UUID produto = criarProdutoComEstoque(10);
        // a movimentação de saldo inicial já ocupa a sequência 1
        assertThatThrownBy(() -> jdbc.update("""
                insert into movimentacao_estoque (id, produto_id, tipo, quantidade, motivo, sequencia, estornada)
                values (?, ?, 'ENTRADA', 1, 'inserção direta', 1, false)""", UUID.randomUUID(), produto))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("A restrição CHECK do banco rejeita tipos de movimentação desconhecidos")
    void restricaoDeTipo() {
        UUID produto = criarProdutoComEstoque(10);
        assertThatThrownBy(() -> jdbc.update("""
                insert into movimentacao_estoque (id, produto_id, tipo, quantidade, motivo, sequencia, estornada)
                values (?, ?, 'VENDA', 1, 'inserção direta', 99, false)""", UUID.randomUUID(), produto))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
