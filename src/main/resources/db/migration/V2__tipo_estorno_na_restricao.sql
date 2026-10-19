-- Bancos criados pelo antigo ddl-auto: update antes da existência do estorno ficaram com a restrição
-- de tipo sem o valor ESTORNO, o que fazia todo estorno falhar. Recria a restrição com os cinco tipos.
-- Em bancos criados pela V1 a restrição já está correta e a operação apenas a redefine.
ALTER TABLE movimentacao_estoque DROP CONSTRAINT IF EXISTS movimentacao_estoque_tipo_check;

ALTER TABLE movimentacao_estoque
    ADD CONSTRAINT movimentacao_estoque_tipo_check
        CHECK (tipo IN ('ENTRADA', 'SAIDA', 'DEVOLUCAO', 'AJUSTE', 'ESTORNO'));
