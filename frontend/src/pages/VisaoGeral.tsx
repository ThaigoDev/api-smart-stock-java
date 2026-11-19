import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertTriangle, ArrowLeftRight, Boxes, PackageX, Sparkles, Wallet } from 'lucide-react';
import { api, consulta } from '../lib/api';
import { useCarregar } from '../lib/hooks';
import { useSessao } from '../lib/sessao';
import {
  COR_SITUACAO, DESCRICAO_SITUACAO, NOME_SITUACAO, SITUACOES, data, decimal1, numero, reais, somarDias,
} from '../lib/formato';
import type { Painel, PedidoCompra, PosicaoProduto, ResumoMensal, Situacao } from '../lib/tipos';
import { Carregando, Confirmar, ErroCarregamento, Kpi, SeloSituacao, Vazio } from '../components/ui';
import { GraficoFaturamento } from '../components/Graficos';
import { useNotificacoes } from '../components/Notificacoes';
import { avisarPedidosAlterados } from '../lib/eventos';

const PRIORIDADE: Record<Situacao, number> = { RUPTURA: 0, CRITICO: 1, REPOR: 2, SUPERESTOQUE: 3, NORMAL: 4 };

export default function VisaoGeral() {
  const { dataReferencia, usuario } = useSessao();
  const navegar = useNavigate();
  const notificar = useNotificacoes();
  const [confirmarSugestao, setConfirmarSugestao] = useState(false);

  const painel = useCarregar(() => api.get<Painel>(`/relatorios/painel${consulta({ data_referencia: dataReferencia })}`), [dataReferencia]);
  const posicao = useCarregar(() => api.get<PosicaoProduto[]>(`/relatorios/posicao-estoque${consulta({ data_referencia: dataReferencia })}`), [dataReferencia]);
  const pedidosAbertos = useCarregar(() => api.get<PedidoCompra[]>('/pedidos-compra?status=ABERTO'), []);
  const emPedido = new Map<string, string>();
  (pedidosAbertos.dados ?? []).forEach((pedido) => pedido.itens.forEach((i) => emPedido.set(i.produto_id, pedido.codigo)));
  const mensal = useCarregar(
    () => api.get<ResumoMensal[]>(`/relatorios/movimentacoes-mensais${consulta({ inicio: somarDias(dataReferencia, -364), fim: dataReferencia })}`),
    [dataReferencia],
  );

  if (painel.erro) return <ErroCarregamento mensagem={painel.erro} tentarNovamente={painel.recarregar} />;
  if (!painel.dados) return <Carregando />;
  const p = painel.dados;

  const atencao = (posicao.dados ?? [])
    .filter((x) => x.ativo && x.situacao && x.situacao !== 'NORMAL' && x.situacao !== 'SUPERESTOQUE')
    .sort((a, b) => PRIORIDADE[a.situacao!] - PRIORIDADE[b.situacao!] || (a.cobertura_dias ?? 0) - (b.cobertura_dias ?? 0));
  const totalSituacao = SITUACOES.reduce((s, k) => s + (p.produtos_por_situacao[k] ?? 0), 0) || 1;
  const exigemReposicao = (p.produtos_por_situacao.RUPTURA ?? 0) + (p.produtos_por_situacao.CRITICO ?? 0) + (p.produtos_por_situacao.REPOR ?? 0);
  const maisVendidos = p.mais_vendidos_ultimos_30_dias;
  const maiorVenda = Math.max(1, ...maisVendidos.map((m) => Number(m.valor_vendido)));

  const gerarSugestoes = async () => {
    try {
      const criados = await api.post<PedidoCompra[]>(`/pedidos-compra/sugestoes${consulta({ data_referencia: dataReferencia })}`);
      if (criados.length === 0) {
        notificar.sucesso('Nada a sugerir', 'Os produtos que precisam de reposição já estão em pedidos abertos.');
      } else {
        notificar.sucesso(`${criados.length} pedido(s) de compra gerado(s)`, 'Revise e confirme o recebimento quando a mercadoria chegar.');
        avisarPedidosAlterados();
        pedidosAbertos.recarregar();
        navegar('/pedidos');
      }
    } catch (e) {
      notificar.erro('Não foi possível gerar os pedidos', (e as Error).message);
    }
  };

  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <h1>Visão geral</h1>
          <p>Situação do estoque em {data(p.data_referencia)} · vendas dos últimos 30 dias</p>
        </div>
        {usuario?.admin && exigemReposicao > 0 && (
          <button className="btn btn-primario" onClick={() => setConfirmarSugestao(true)}>
            <Sparkles size={16} /> Gerar pedidos de compra
          </button>
        )}
      </div>

      <section className="grade grade-kpi">
        <Kpi rotulo="Valor em estoque" valor={reais(p.valor_total_em_estoque)} detalhe={`${numero(p.unidades_em_estoque)} unidades em ${numero(p.produtos_ativos)} produtos`} icone={<Wallet size={17} />} />
        <Kpi rotulo="Faturamento (30 dias)" valor={reais(p.faturamento_ultimos_30_dias)} detalhe={`${numero(p.movimentacoes_ultimos_30_dias)} movimentações no período`} icone={<ArrowLeftRight size={17} />} cor="var(--normal)" />
        <Kpi rotulo="Precisam de reposição" valor={numero(exigemReposicao)} detalhe={`${numero(p.produtos_por_situacao.RUPTURA)} em ruptura`} icone={<AlertTriangle size={17} />} cor="var(--critico)" />
        <Kpi rotulo="Capital em superestoque" valor={reais(p.valor_em_superestoque)} detalhe={`${numero(p.produtos_por_situacao.SUPERESTOQUE)} produtos acima do máximo`} icone={<Boxes size={17} />} cor="var(--superestoque)" />
      </section>

      <section className="card">
        <div className="card-cabecalho">
          <div>
            <h2>Situação dos produtos</h2>
            <p>Clique em uma situação para ver os produtos</p>
          </div>
        </div>
        <div className="card-corpo">
          <div className="barra-empilhada" role="img" aria-label="Distribuição dos produtos por situação">
            {SITUACOES.filter((s) => p.produtos_por_situacao[s] > 0).map((s) => (
              <div key={s} title={`${NOME_SITUACAO[s]}: ${p.produtos_por_situacao[s]}`}
                   style={{ width: `${(100 * p.produtos_por_situacao[s]) / totalSituacao}%`, background: COR_SITUACAO[s] }} />
            ))}
          </div>
          <div className="legenda">
            {SITUACOES.map((s) => (
              <button key={s} className="legenda-item" title={DESCRICAO_SITUACAO[s]} onClick={() => navegar(`/produtos?situacao=${s}`)}>
                <span className="legenda-cor" style={{ background: COR_SITUACAO[s] }} />
                {NOME_SITUACAO[s]} <b>{p.produtos_por_situacao[s] ?? 0}</b>
              </button>
            ))}
          </div>
        </div>
      </section>

      <section className="grade grade-2-1">
        <div className="card">
          <div className="card-cabecalho">
            <div>
              <h2>Faturamento mensal</h2>
              <p>Saídas valorizadas pelo preço de venda, últimos 12 meses</p>
            </div>
          </div>
          <div className="card-corpo">
            {mensal.dados ? (mensal.dados.length ? <GraficoFaturamento meses={mensal.dados} /> : <Vazio titulo="Sem vendas no período" />) : <Carregando />}
          </div>
        </div>

        <div className="card">
          <div className="card-cabecalho">
            <div>
              <h2>Mais vendidos</h2>
              <p>Por valor, últimos 30 dias</p>
            </div>
          </div>
          <div className="lista" style={{ paddingTop: 8 }}>
            {maisVendidos.length === 0 && <Vazio titulo="Nenhuma venda no período" />}
            {maisVendidos.map((item, i) => (
              <div key={item.produto_id} className="lista-item" style={{ cursor: 'pointer' }} onClick={() => navegar(`/produtos/${item.produto_id}`)}>
                <span className="muted num" style={{ width: 16 }}>{i + 1}</span>
                <div className="cresce">
                  <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8 }}>
                    <strong className="truncate">{item.nome}</strong>
                    <span className="num">{reais(item.valor_vendido)}</span>
                  </div>
                  <div className="barra-progresso"><div style={{ width: `${(100 * Number(item.valor_vendido)) / maiorVenda}%` }} /></div>
                  <span className="muted small">{numero(item.quantidade_vendida)} unidades</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="card">
        <div className="card-cabecalho">
          <div>
            <h2>Precisam de atenção</h2>
            <p>Produtos em ruptura, abaixo do mínimo ou no ponto de pedido, do mais urgente ao menos urgente</p>
          </div>
          <button className="btn btn-pequeno" onClick={() => navegar('/produtos?alerta=1')}>Ver todos</button>
        </div>
        <div className="card-corpo" style={{ padding: 0, marginTop: 12 }}>
          {posicao.carregando && !posicao.dados ? <Carregando /> : atencao.length === 0 ? (
            <Vazio titulo="Nenhum produto precisa de reposição" texto="Todos os saldos estão acima do ponto de pedido." icone={<PackageX size={34} />} />
          ) : (
            <div className="tabela-container">
              <table className="tabela">
                <thead>
                  <tr>
                    <th>Produto</th><th>Situação</th><th className="direita">Saldo</th><th className="direita">Ponto de pedido</th>
                    <th className="direita">Cobertura</th><th className="direita">Comprar</th><th>Fornecedor</th>
                  </tr>
                </thead>
                <tbody>
                  {atencao.slice(0, 8).map((x) => (
                    <tr key={x.produto_id} className="clicavel" onClick={() => navegar(`/produtos/${x.produto_id}`)}>
                      <td><div className="celula-produto"><strong>{x.nome}</strong><span>{x.sku}</span></div></td>
                      <td><SeloSituacao situacao={x.situacao} /></td>
                      <td className="direita num">{numero(x.saldo)}</td>
                      <td className="direita num">{numero(x.ponto_pedido)}</td>
                      <td className="direita num">{x.cobertura_dias == null ? '—' : `${decimal1(x.cobertura_dias)} dias`}</td>
                      <td className="direita num">
                        {emPedido.has(x.produto_id)
                          ? <span className="selo selo-azul" title="Já existe um pedido de compra aberto para este produto">{emPedido.get(x.produto_id)}</span>
                          : <strong>{numero(x.sugestao_compra)}</strong>}
                      </td>
                      <td className="muted">{x.fornecedor}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </section>

      {confirmarSugestao && (
        <Confirmar
          titulo="Gerar pedidos de compra"
          mensagem={<>Será criado um pedido por fornecedor com os <strong>{exigemReposicao}</strong> produtos que atingiram o ponto de pedido, na quantidade necessária para voltar ao estoque máximo. Produtos que já estão em pedidos abertos são ignorados.</>}
          textoConfirmar="Gerar pedidos"
          aoConfirmar={gerarSugestoes}
          aoFechar={() => setConfirmarSugestao(false)}
        />
      )}
    </>
  );
}
