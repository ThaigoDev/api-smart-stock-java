# Smart Stock: resultados para o TCC

Todos os números abaixo foram medidos em execução real em 13/10/2026. Os arquivos brutos estão em [`docs/resultados/`](resultados/). Eles estão organizados pelas três métricas da Seção 3.3 do TCC (Corretude Funcional, Integridade Transacional e Confiabilidade da API), mais os indicadores gerenciais que o sistema agora calcula.

---

## 1. Evolução do sistema (base para a descrição do desenvolvimento)

| Funcionalidade | Endpoint / componente | Lacuna do TCC que resolve |
|---|---|---|
| Bloqueio de estoque negativo (HTTP 422) | `POST /movimentacoesestoque` | "não foram reportados testes de saída com saldo insuficiente" |
| Situação do estoque após cada movimentação: RUPTURA, CRITICO, REPOR, NORMAL, SUPERESTOQUE | resposta de `POST /movimentacoesestoque` | "nem de emissão de alertas de ruptura e superestoque" |
| Ponto de pedido calculado: demanda média dos últimos 30 dias × prazo de reposição + estoque mínimo | `PoliticaEstoque` | Ponto de pedido citado na Seção 2.2 |
| Alertas com sugestão de compra (quantidade e valor) | `GET /relatorios/alertas` | Ruptura e superestoque (Seção 2.2) |
| Curva ABC calculada a partir das vendas reais | `GET /relatorios/curva-abc` | Decisão baseada em dados (Seções 2.1 e 2.2) |
| Giro de estoque, estoque médio e cobertura em dias | `GET /relatorios/giro` | "monitoramento preciso do giro" (Seção 2.2) |
| Painel gerencial e série mensal | `GET /relatorios/painel`, `GET /relatorios/movimentacoes-mensais` | Suporte à decisão |
| Kardex: saldo anterior/posterior e número sequencial por movimentação | `GET /produtos/{id}/extrato` | Rastreabilidade e integridade |
| Histórico imutável: correções por **estorno**, sem PUT/DELETE | `POST /movimentacoesestoque/{id}/estorno` | Integridade e auditoria |
| Conciliação automática (saldo = soma do histórico) | `GET /relatorios/conciliacao` | Integridade (ACID) |
| Transação única por movimentação + lock pessimista (`SELECT ... FOR UPDATE`) + `UNIQUE(produto_id, sequencia)` | `ServiceMovimentacaoEstoque`, `RepositoryEstoque` | "sem avaliação de concorrência" |
| Novos códigos de erro distintos: 409 (conflito/vínculo), 422 (regra de negócio), 405 (histórico imutável) | `ManipuladorExceptionsGlobal` | Confiabilidade da API |
| 79 testes automatizados (unidade + integração) | `src/test` | "testes automatizados de unidade e de integração" |
| Teste de carga e de disputa concorrente | `scripts/teste_carga.py` | "testes de carga para mensurar a escalabilidade" |

Destaque de engenharia de software (SOLID): cada `TipoMovimentacaoEstoque` declara o próprio efeito sobre o saldo (`efeitoNoSaldo`). Incluir um novo tipo de movimentação não exige alterar o serviço (Princípio Aberto/Fechado). Os cálculos de indicadores (`PoliticaEstoque`, `CalculadoraCurvaABC`, `CalculadoraGiro`) são classes puras, sem dependência de banco nem de framework. Isso permitiu testá-los isoladamente (Responsabilidade Única).

Correções de defeitos encontradas no caminho: `PUT /produtos` não gravava o preço; a pesquisa de produtos sem categoria retornava 500; o cabeçalho `Location` era montado sem barra (`/produtos<id>`); exclusões e edições de movimentação não revertiam o saldo; a validação devolvia o mesmo campo repetido em todas as mensagens; num banco novo, a aplicação não subia (a tabela `funcao` vazia abortava a inicialização); e o cadastro de usuário procurava duplicidade de e-mail pelo nome.

---

## 2. Cenário experimental (massa de dados sintética)

Um minimercado fictício operando por 26 semanas. Todas as movimentações passam pelo mesmo serviço e pelas mesmas regras da API. A simulação é determinística (semente fixa): duas execuções independentes produziram exatamente os mesmos totais.

| Item | Valor |
|---|---|
| Período simulado | 13/04/2026 a 11/10/2026 (182 dias) |
| Categorias / fornecedores / produtos | 10 / 6 / 59 |
| Prazo de entrega dos fornecedores | 2 a 10 dias |
| Modelo de demanda | Poisson; fator por dia da semana (sábado 1,35; domingo 0,70); crescimento de 3% ao mês |
| Política de reposição **adequada** (compra no ponto de pedido) | 35 produtos (59,3%) |
| Política **tardia** (só compra quando o produto acaba) | 6 produtos (10,2%) |
| Política **excessiva** (compra o triplo) | 18 produtos (30,5%) |
| **Movimentações registradas** | **15.635** |
|   saldos iniciais / recebimentos | 59 / 474 |
|   saídas (vendas) | 14.715 (22 com atendimento parcial) |
|   devoluções / ajustes de inventário / estornos | 216 / 152 / 19 |
| Pedidos de compra gerados | 466 |
| **Saídas recusadas por saldo insuficiente (422)** | **192** (1,29% dos cupons) |
| Unidades vendidas / demandadas e não atendidas | 68.094 / 490 → **nível de serviço de 99,29%** |
| Tempo de geração | 77 s |

**Achado para a discussão:** os 6 produtos com reposição tardia (10% do mix) responderam por **177 das 192 recusas (92,2%)**. Os 35 com política adequada tiveram apenas 12. O sistema não só impediu o estoque negativo como tornou mensurável o custo de uma política de compra ruim, o que dialoga diretamente com Silva, Moura e Ribas (2023) sobre decisões reativas.

---

## 3. Corretude funcional

### 3.1 Testes automatizados (JUnit 5, Mockito, MockMvc)

| Suíte | Tipo | Testes | Aprovados |
|---|---|---:|---:|
| `TipoMovimentacaoEstoqueTest`: efeito de cada tipo no saldo | Unidade | 9 | 9 |
| `PoliticaEstoqueTest`: ponto de pedido, classificação, sugestão de compra | Unidade | 15 | 15 |
| `CalculadoraCurvaABCTest`: classes A/B/C, fronteiras, período sem vendas | Unidade | 5 | 5 |
| `CalculadoraGiroTest`: estoque médio e giro | Unidade | 4 | 4 |
| `ServiceMovimentacaoEstoqueTest`: regras do serviço isoladas do banco | Unidade | 13 | 13 |
| `MovimentacaoEstoqueIntegracaoTest`: fluxo HTTP de movimentações | Integração | 10 | 10 |
| `ConfiabilidadeApiIntegracaoTest`: códigos de erro | Integração | 13 | 13 |
| `RelatorioIntegracaoTest`: indicadores | Integração | 6 | 6 |
| `TransacaoEConcorrenciaIntegracaoTest`: atomicidade e concorrência | Integração | 3 | 3 |
| `ApplicationTests`: inicialização do contexto | Integração | 1 | 1 |
| **Total** | | **79** | **79 (100%)** |

### 3.2 Regras de negócio verificadas

| Cenário | Esperado | Obtido |
|---|---|---|
| Saída de 21 un. com saldo 20 | 422, saldo inalterado, nada gravado | 422, saldo 20, histórico com 1 registro |
| Saída que zera o estoque (15 de 15) | 201 + situação RUPTURA | 201, RUPTURA |
| Saída após zerar (1 un.) | 422 | 422 |
| Saída deixando saldo 10 (mín. 10, ponto de pedido 17) | 201 + REPOR | 201, REPOR |
| Ajuste de −3 deixando saldo 9 (mín. 10) | 201 + CRITICO | 201, CRITICO |
| Entrada levando o saldo a 110 (máx. 100) | 201 + SUPERESTOQUE | 201, SUPERESTOQUE |
| Ajuste negativo maior que o saldo | 422 | 422 |
| Estorno de saída de 30 | +30 no saldo, original marcada como estornada | conforme |
| Segundo estorno da mesma movimentação | 422 | 422 |
| Estorno de entrada já consumida | 422 (evita saldo negativo) | 422 |
| Movimentação de produto inativo | 422 | 422 |

### 3.3 Situação do estoque ao fim do período (painel em 11/10/2026)

| Situação | Produtos | % |
|---|---:|---:|
| Ruptura | 1 | 1,7 |
| Crítico (abaixo do mínimo) | 1 | 1,7 |
| Repor (atingiu o ponto de pedido) | 14 | 23,7 |
| Normal | 26 | 44,1 |
| Superestoque | 17 | 28,8 |

Valor total em estoque: **R$ 36.733,87**, dos quais **R$ 7.809,72 (21,3%)** estão acima do estoque máximo, ou seja, capital imobilizado. Os 16 produtos que exigem reposição recebem quantidade e valor sugeridos de compra (`alertas.json`).

---

## 4. Integridade transacional (ACID)

| Verificação | Resultado |
|---|---|
| **Atomicidade**: falha forçada na gravação do histórico depois de o saldo já ter sido alterado | Transação inteira desfeita; saldo permaneceu 40 |
| **Isolamento (H2, teste automatizado)**: 20 saídas simultâneas de 10 un. sobre saldo 100 | Exatamente 10 aprovadas e 10 recusadas; saldo final 0 |
| **Isolamento (H2)**: 25 entradas de 7 e 25 saídas de 3, simultâneas, sobre saldo 1000 | Saldo final exato: 1100 |
| **Isolamento (PostgreSQL, via HTTP)**: 50 saídas simultâneas de 5 un. sobre saldo 100 | 20 × 201, 30 × 422, saldo final 0 |
| **Consistência**: conciliação após seed, carga e roteiro Postman | 61/61 produtos consistentes; 0 quebras |

### Auditoria SQL independente da aplicação

Executada diretamente no PostgreSQL ([`consultas-auditoria.sql`](consultas-auditoria.sql)) sobre **21.640 movimentações**, após seed e duas rodadas de carga:

| Consulta | Inconsistências |
|---|---:|
| Produtos com saldo negativo | 0 |
| Saldo armazenado ≠ soma dos efeitos do histórico | 0 de 61 |
| Quebras de encadeamento (saldo anterior ≠ posterior da anterior) | 0 |
| Saldo posterior ≠ saldo anterior + efeito | 0 |
| Lacunas ou repetições na sequência do kardex | 0 |
| Estornos inconsistentes com a movimentação de origem | 0 |

---

## 5. Confiabilidade da API

### 5.1 Roteiro HTTP (Postman/Newman)

Coleção [`docs/postman/SmartStock.postman_collection.json`](postman/SmartStock.postman_collection.json), executada pelo Newman: **29 requisições, 42 asserções, 0 falhas**, tempo médio de resposta de 43 ms. O roteiro amplia de 12 para 29 os cenários do TCC atual e inclui os que o texto aponta como não testados.

| Código | Significado | Cenários cobertos |
|---|---|---|
| 200 | OK | consultas, pesquisa, extrato, 5 relatórios |
| 201 | Created | categoria, fornecedor, produto, estoque, 4 tipos de movimentação, estorno |
| 204 | No Content | atualização de fornecedor |
| 400 | Bad Request | produto malformado (≥ 7 erros), quantidade zero, tipo inexistente |
| 401 | Unauthorized | requisição sem token JWT |
| 404 | Not Found | categoria inexistente, produto inexistente |
| 405 | Method Not Allowed | tentativa de excluir movimentação |
| 409 | Conflict | estoque duplicado, exclusão de produto com histórico |
| 422 | Unprocessable Content | saída sem saldo, estorno repetido |

A API distingue **erro de formato (400)**, **recurso inexistente (404)**, **conflito com o estado (409)** e **violação de regra de negócio (422)**. A aplicação cliente recebe, portanto, a origem exata da recusa.

### 5.2 Desempenho sob carga (PostgreSQL)

`scripts/teste_carga.py`, com 1.000 requisições por nível de concorrência. Cliente, API e banco rodaram na mesma máquina.

| Cenário | Clientes simultâneos | Throughput (req/s) | Latência média (ms) | p95 (ms) | p99 (ms) | Erros 5xx |
|---|---:|---:|---:|---:|---:|---:|
| Leitura (produtos, estoques, alertas, curva ABC) | 1 | 36,0 | 27,6 | 57,0 | 68,8 | 0% |
| Leitura | 10 | 384,1 | 25,5 | 58,2 | 66,8 | 0% |
| Leitura | 50 | 513,9 | 93,1 | 132,2 | 147,7 | 0% |
| Escrita (entradas e saídas) | 1 | 46,9 | 21,2 | 33,6 | 35,6 | 0% |
| Escrita | 10 | 847,7 | 11,6 | 29,4 | 32,2 | 0% |
| Escrita | 50 | 1.233,1 | 38,7 | 60,4 | 70,2 | 0% |

Nenhuma das 6.050 requisições da rodada resultou em erro de servidor. As poucas respostas 422 na escrita são saídas sorteadas para produtos sem saldo, ou seja, a regra funcionando sob carga. De 1 para 10 clientes, o throughput cresceu cerca de 11× (leitura) e 18× (escrita) sem aumento relevante de latência. Com 50 clientes, a latência sobe enquanto o throughput cresce menos. Isso é compatível com o pool padrão de 10 conexões com o banco, que passa a enfileirar requisições. Ajustar o pool seria o próximo passo de um estudo de desempenho.

---

## 6. Indicadores gerenciais gerados pelo sistema

### 6.1 Curva ABC (13/04 a 11/10/2026; R$ 389.050,48 vendidos)

| Classe | Produtos | % dos itens | Valor vendido (R$) | % do valor |
|---|---:|---:|---:|---:|
| A | 26 | 44,07 | 314.083,79 | 80,73 |
| B | 15 | 25,42 | 56.017,78 | 14,40 |
| C | 18 | 30,51 | 18.948,91 | 4,87 |

Os 5 itens de maior valor são Arroz 5 kg, Café 500 g, Leite 1 L, Refrigerante 2 L e Pão Francês. Lista completa em `curva-abc.csv`, pronta para o gráfico de Pareto. A concentração ficou menos acentuada que o 80/20 clássico, o que é coerente com um minimercado em que muitos itens básicos têm saída parecida.

### 6.2 Giro de estoque no semestre (média geral 12,46)

| Categoria | Giro médio | | Categoria | Giro médio |
|---|---:|---|---|---:|
| Padaria | 15,87 | | Congelados | 13,31 |
| Bebidas | 15,25 | | Mercearia | 12,63 |
| Limpeza | 14,92 | | Bazar | 9,80 |
| Hortifruti | 14,36 | | Higiene | 9,41 |
| Laticínios | 14,08 | | Papelaria | 5,52 |

Maior giro: Detergente (20,4, ~9 dias em estoque). Menor: Grampeador (1,2, ~147 dias em estoque). Por produto, ver `giro.csv`.

### 6.3 Série mensal (`movimentacoes-mensais.csv`)

| Mês | Movimentações | Unidades vendidas | Faturamento (R$) |
|---|---:|---:|---:|
| abr/26 (a partir de 13/04) | 1.602 | 6.358 | 35.993,41 |
| mai/26 | 2.553 | 11.025 | 62.348,84 |
| jun/26 | 2.542 | 10.904 | 62.928,18 |
| jul/26 | 2.663 | 11.551 | 65.746,52 |
| ago/26 | 2.674 | 11.963 | 68.261,17 |
| set/26 | 2.636 | 11.777 | 67.799,33 |
| out/26 (até 11/10) | 965 | 4.516 | 25.973,03 |

Entre maio e setembro (meses completos), o faturamento cresceu 8,7%, reproduzindo a tendência de crescimento da simulação.

---

## 7. Limitações que permanecem (para atualizar as Considerações Finais)

- Os dados continuam **sintéticos**. A simulação é calibrada, mas não substitui um piloto com uma empresa real.
- A carga foi medida com **cliente, API e banco na mesma máquina**. Os números servem para comparar níveis de concorrência entre si, não como capacidade de produção.
- Os testes automatizados usam **H2 em modo PostgreSQL**. A concorrência no PostgreSQL real foi validada pelo script de carga, não pela suíte JUnit.
- O **faturamento usa o preço atual** do produto (não há histórico de preços).
- A demanda é estimada por **média móvel de 30 dias**, sem tratamento de sazonalidade.
- O sistema ainda **não tem interface gráfica**.
- **Atenção ao texto atual do TCC:** os trabalhos futuros citam "implementação de mecanismos de autenticação e autorização", mas a API já exige token JWT em todas as rotas (exceto login), e o roteiro confirma o 401. Vale corrigir esse trecho e citar a autenticação como parte do sistema.

---

## 8. Como reproduzir

```bash
# 1. banco vazio (o esquema mudou; use um banco novo)
docker exec postgres_api_stock psql -U postgres -c "CREATE DATABASE apistock_tcc"

# 2. subir a API: o seed roda sozinho quando não há produtos (~80 s)
DATASOURCE_URL=jdbc:postgresql://localhost:5432/apistock_tcc ./mvnw spring-boot:run

# 3. testes automatizados (não precisam do PostgreSQL)
./mvnw test

# 4. roteiro HTTP, carga e auditoria
npx newman run docs/postman/SmartStock.postman_collection.json --env-var baseUrl=http://localhost:8080
py scripts/teste_carga.py --url http://127.0.0.1:8080 --requisicoes 1000
docker exec -i postgres_api_stock psql -U postgres -d apistock_tcc < docs/consultas-auditoria.sql
```

Variáveis do seed: `SEED_ENABLED` (padrão `true`), `SEED_DATA_FIM` (padrão: domingo anterior à data atual) e `SEED_SEMANAS` (padrão `26`). Com o mesmo período, os totais são sempre os mesmos. Mudando o dia de execução, apenas as datas se deslocam.
