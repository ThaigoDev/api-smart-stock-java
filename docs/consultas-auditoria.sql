-- Auditoria do banco do Smart Stock, executada diretamente no PostgreSQL,
-- independente do código da aplicação. Todas as consultas devem retornar 0 inconsistências.
-- Uso: docker exec -i postgres_api_stock psql -U postgres -d <banco> < docs/consultas-auditoria.sql

\echo '1. Produtos com saldo negativo (esperado: 0)'
SELECT COUNT(*) AS saldos_negativos FROM estoque WHERE quantidade < 0;

\echo '2. Saldo armazenado x soma dos efeitos do histórico (esperado: divergentes = 0)'
WITH recalculado AS (
    SELECT produto_id,
           SUM(CASE WHEN tipo = 'SAIDA' THEN -quantidade ELSE quantidade END) AS saldo_recalculado,
           COUNT(*) AS movimentacoes
    FROM movimentacao_estoque
    GROUP BY produto_id
)
SELECT COUNT(*)                                                  AS produtos,
       SUM(r.movimentacoes)                                      AS movimentacoes,
       COUNT(*) FILTER (WHERE e.quantidade = r.saldo_recalculado)  AS consistentes,
       COUNT(*) FILTER (WHERE e.quantidade <> r.saldo_recalculado) AS divergentes
FROM estoque e
JOIN recalculado r ON r.produto_id = e.produto_id;

\echo '3. Quebras de encadeamento: saldo_anterior diferente do saldo_posterior da movimentação anterior (esperado: 0)'
SELECT COUNT(*) AS quebras
FROM (
    SELECT saldo_anterior,
           LAG(saldo_posterior, 1, 0) OVER (PARTITION BY produto_id ORDER BY sequencia) AS posterior_da_anterior
    FROM movimentacao_estoque
) t
WHERE saldo_anterior <> posterior_da_anterior;

\echo '4. Movimentações cujo saldo_posterior não corresponde a saldo_anterior + efeito (esperado: 0)'
SELECT COUNT(*) AS calculos_incorretos
FROM movimentacao_estoque
WHERE saldo_posterior <> saldo_anterior + CASE WHEN tipo = 'SAIDA' THEN -quantidade ELSE quantidade END;

\echo '5. Lacunas ou repetições na sequência do kardex (esperado: 0)'
SELECT COUNT(*) AS produtos_com_lacuna
FROM (
    SELECT produto_id, COUNT(*) AS total, MAX(sequencia) AS maior, COUNT(DISTINCT sequencia) AS distintas
    FROM movimentacao_estoque
    GROUP BY produto_id
) s
WHERE total <> maior OR total <> distintas;

\echo '6. Estornos sem origem marcada como estornada, ou com efeito diferente do inverso da origem (esperado: 0)'
SELECT COUNT(*) AS estornos_inconsistentes
FROM movimentacao_estoque est
JOIN movimentacao_estoque ori ON ori.id = est.movimentacao_origem_id
WHERE est.tipo = 'ESTORNO'
  AND (ori.estornada = false
       OR est.quantidade <> -(CASE WHEN ori.tipo = 'SAIDA' THEN -ori.quantidade ELSE ori.quantidade END));

\echo '7. Volume por tipo de movimentação'
SELECT tipo, COUNT(*) AS movimentacoes, SUM(ABS(quantidade)) AS unidades
FROM movimentacao_estoque
GROUP BY tipo
ORDER BY tipo;
