package com.thai.finance.api.finance.api.integracao;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Executa o gerador da massa de dados sintética em um período curto e confere que o cenário produzido
 * passa pelas mesmas verificações de integridade aplicadas ao experimento completo.
 */
@TestPropertySource(properties = {
        // banco próprio: este contexto não pode recriar o esquema usado pelas demais classes de teste
        "spring.datasource.url=jdbc:h2:mem:smartstock_seed;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
        "smartstock.seed.enabled=true",
        "smartstock.seed.semanas=2",
        "smartstock.seed.data-fim=2026-01-31"
})
class GeradorDeDadosIntegracaoTest extends ApiTestBase {

    @Test
    @DisplayName("O seed cria o catálogo completo e um histórico consistente")
    void seedGeraCenarioConsistente() throws Exception {
        String produtos = obter("/produtos").andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat((List<?>) JsonPath.read(produtos, "$")).hasSize(59);

        obter("/movimentacoesestoque?inicio=2026-01-18&fim=2026-01-31&tamanho=1")
                .andExpect(jsonPath("$.total_elementos").value(org.hamcrest.Matchers.greaterThan(500)));

        obter("/relatorios/conciliacao")
                .andExpect(jsonPath("$.consistente").value(true))
                .andExpect(jsonPath("$.produtos_verificados").value(59))
                .andExpect(jsonPath("$.quebras_de_encadeamento").value(0));

        obter("/relatorios/curva-abc?inicio=2026-01-18&fim=2026-01-31")
                .andExpect(jsonPath("$.resumo_por_classe[0].classe").value("A"))
                .andExpect(jsonPath("$.valor_total_vendido").value(org.hamcrest.Matchers.greaterThan(0.0)));
    }
}
