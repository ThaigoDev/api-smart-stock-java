import { useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, Download, Pencil, Shuffle, Undo2 } from 'lucide-react';
import { api, consulta } from '../lib/api';
import { useCarregar } from '../lib/hooks';
import { useSessao } from '../lib/sessao';
import { NOME_TIPO, dataHora, decimal1, efeito, numero, reais } from '../lib/formato';
import { baixarCsv } from '../lib/csv';
import type { Movimentacao, PosicaoProduto } from '../lib/tipos';
import { Campo, Carregando, ErroCarregamento, Modal, SeloSituacao, SeloTipo, Vazio } from '../components/ui';
import { GraficoSaldo } from '../components/Graficos';
import FormMovimentacao from '../components/FormMovimentacao';
import FormProduto from '../components/FormProduto';
import { useNotificacoes } from '../components/Notificacoes';

const POR_PAGINA = 25;

export default function ProdutoDetalhe() {
  const { id = '' } = useParams();
  const { dataReferencia, usuario } = useSessao();
  const notificar = useNotificacoes();
  const [pagina, setPagina] = useState(0);
  const [movimentando, setMovimentando] = useState(false);
  const [editando, setEditando] = useState(false);
  const [estornando, setEstornando] = useState<Movimentacao | null>(null);
  const [motivoEstorno, setMotivoEstorno] = useState('');

  const posicao = useCarregar(() => api.get<PosicaoProduto[]>(`/relatorios/posicao-estoque${consulta({ data_referencia: dataReferencia })}`), [dataReferencia]);
  const extrato = useCarregar(() => api.get<Movimentacao[]>(`/produtos/${id}/extrato`), [id]);
  const recarregar = () => { posicao.recarregar(); extrato.recarregar(); };

  const produto = posicao.dados?.find((p) => p.produto_id === id);
  const recentes = useMemo(() => [...(extrato.dados ?? [])].reverse(), [extrato.dados]);
  const totalPaginas = Math.max(1, Math.ceil(recentes.length / POR_PAGINA));

  if (posicao.erro || extrato.erro) return <ErroCarregamento mensagem={(posicao.erro || extrato.erro)!} tentarNovamente={recarregar} />;
  if (!posicao.dados || !extrato.dados) return <Carregando />;
  if (!produto) return <Vazio titulo="Produto não encontrado" texto="Ele pode ter sido excluído." />;

  const estornar = async () => {
    if (!estornando) return;
    try {
      await api.post(`/movimentacoesestoque/${estornando.id}/estorno`, { motivo: motivoEstorno });
      notificar.sucesso('Movimentação estornada', `${NOME_TIPO[estornando.tipo]} de ${estornando.quantidade} unidade(s) desfeita.`);
      setEstornando(null);
      recarregar();
    } catch (e) {
      notificar.erro('Estorno recusado', (e as Error).message);
    }
  };

  const exportar = () => baixarCsv(`extrato-${produto.sku}.csv`,
    ['Data', 'Tipo', 'Quantidade', 'Efeito no saldo', 'Saldo anterior', 'Saldo posterior', 'Motivo', 'Registrado por', 'Estornada'],
    recentes.map((m) => [dataHora(m.data_movimentacao), NOME_TIPO[m.tipo], m.quantidade, efeito(m.tipo, m.quantidade), m.saldo_anterior,
      m.saldo_posterior, m.motivo, m.registrado_por, m.estornada ? 'Sim' : 'Não']));

  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <Link to="/produtos" className="voltar"><ArrowLeft size={15} /> Produtos</Link>
          <h1 style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
            {produto.nome} <SeloSituacao situacao={produto.situacao} />
            {!produto.ativo && <span className="selo selo-neutro">Inativo</span>}
          </h1>
          <p>{produto.sku} · {produto.categoria} · fornecido por {produto.fornecedor}</p>
        </div>
        <div style={{ display: 'flex', gap: 10 }}>
          {usuario?.admin && <button className="btn" onClick={() => setEditando(true)}><Pencil size={15} /> Editar</button>}
          {produto.ativo && produto.possui_estoque && (
            <button className="btn btn-primario" onClick={() => setMovimentando(true)}><Shuffle size={15} /> Registrar movimentação</button>
          )}
        </div>
      </div>

      <section className="card card-corpo">
        <div className="detalhes">
          <div><div className="detalhe-rotulo">Saldo atual</div><div className="detalhe-valor" style={{ fontSize: 22 }}>{numero(produto.saldo)}</div></div>
          <div><div className="detalhe-rotulo">Estoque mínimo / máximo</div><div className="detalhe-valor">{numero(produto.estoque_minimo)} / {produto.estoque_maximo == null ? '—' : numero(produto.estoque_maximo)}</div></div>
          <div><div className="detalhe-rotulo">Ponto de pedido</div><div className="detalhe-valor">{numero(produto.ponto_pedido)}</div></div>
          <div><div className="detalhe-rotulo">Demanda média</div><div className="detalhe-valor">{decimal1(produto.demanda_media_diaria)} / dia</div></div>
          <div><div className="detalhe-rotulo">Cobertura</div><div className="detalhe-valor">{produto.cobertura_dias == null ? '—' : `${decimal1(produto.cobertura_dias)} dias`}</div></div>
          <div><div className="detalhe-rotulo">Prazo do fornecedor</div><div className="detalhe-valor">{produto.prazo_reposicao_dias} dias</div></div>
          <div><div className="detalhe-rotulo">Preço de venda</div><div className="detalhe-valor">{reais(produto.preco)}</div></div>
          <div><div className="detalhe-rotulo">Valor em estoque</div><div className="detalhe-valor">{reais(produto.valor_em_estoque)}</div></div>
          {produto.sugestao_compra > 0 && (
            <div><div className="detalhe-rotulo">Sugestão de compra</div><div className="detalhe-valor" style={{ color: 'var(--critico)' }}>{numero(produto.sugestao_compra)} unidades</div></div>
          )}
        </div>
      </section>

      <section className="card">
        <div className="card-cabecalho">
          <div>
            <h2>Evolução do saldo</h2>
            <p>Saldo no fechamento de cada dia; as linhas tracejadas mostram os limites de reposição</p>
          </div>
        </div>
        <div className="card-corpo">
          {extrato.dados.length ? (
            <GraficoSaldo extrato={extrato.dados} minimo={produto.estoque_minimo} maximo={produto.estoque_maximo} pontoPedido={produto.ponto_pedido} />
          ) : <Vazio titulo="Ainda não há movimentações" />}
        </div>
      </section>

      <section className="card">
        <div className="card-cabecalho" style={{ paddingBottom: 14 }}>
          <div>
            <h2>Histórico de movimentações (kardex)</h2>
            <p>{numero(recentes.length)} lançamentos, do mais recente ao mais antigo</p>
          </div>
          <button className="btn btn-pequeno" onClick={exportar} disabled={!recentes.length}><Download size={14} /> Exportar CSV</button>
        </div>
        <div className="tabela-container">
          <table className="tabela">
            <thead>
              <tr>
                <th>Data</th><th>Tipo</th><th className="direita">Efeito</th><th className="direita">Saldo</th><th>Motivo</th><th>Registrado por</th><th />
              </tr>
            </thead>
            <tbody>
              {recentes.slice(pagina * POR_PAGINA, (pagina + 1) * POR_PAGINA).map((m) => {
                const ef = efeito(m.tipo, m.quantidade);
                return (
                  <tr key={m.id} style={m.estornada ? { opacity: 0.55 } : undefined}>
                    <td className="nowrap num">{dataHora(m.data_movimentacao)}</td>
                    <td><SeloTipo tipo={m.tipo} />{m.estornada && <span className="selo selo-neutro" style={{ marginLeft: 6 }}>Estornada</span>}</td>
                    <td className={`direita num ${ef >= 0 ? 'efeito-positivo' : 'efeito-negativo'}`}>{ef > 0 ? '+' : ''}{numero(ef)}</td>
                    <td className="direita num nowrap muted">{numero(m.saldo_anterior)} → <strong style={{ color: 'var(--text)' }}>{numero(m.saldo_posterior)}</strong></td>
                    <td className="truncate" style={{ maxWidth: 320 }} title={m.motivo}>{m.motivo}</td>
                    <td className="muted small">{m.registrado_por ?? '—'}</td>
                    <td className="direita">
                      {usuario?.admin && !m.estornada && m.tipo !== 'ESTORNO' && (
                        <button className="btn btn-fantasma btn-icone btn-pequeno" onClick={() => { setEstornando(m); setMotivoEstorno(''); }} title="Estornar esta movimentação" aria-label="Estornar">
                          <Undo2 size={15} />
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
        {totalPaginas > 1 && (
          <div className="paginacao">
            <span className="muted small">Página {pagina + 1} de {totalPaginas}</span>
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn btn-pequeno" disabled={pagina === 0} onClick={() => setPagina((p) => p - 1)}>Anterior</button>
              <button className="btn btn-pequeno" disabled={pagina >= totalPaginas - 1} onClick={() => setPagina((p) => p + 1)}>Próxima</button>
            </div>
          </div>
        )}
      </section>

      {movimentando && <FormMovimentacao produtos={posicao.dados} produtoInicial={produto.produto_id} aoFechar={() => setMovimentando(false)} aoSalvar={recarregar} />}
      {editando && <FormProduto produtoId={produto.produto_id} aoFechar={() => setEditando(false)} aoSalvar={recarregar} />}
      {estornando && (
        <Modal
          titulo="Estornar movimentação"
          descricao="O estorno registra uma movimentação com efeito inverso. A original é mantida no histórico, marcada como estornada."
          aoFechar={() => setEstornando(null)}
          rodape={
            <>
              <button className="btn" onClick={() => setEstornando(null)}>Voltar</button>
              <button className="btn btn-perigo-solido" disabled={!motivoEstorno.trim()} onClick={estornar}><Undo2 size={15} /> Estornar</button>
            </>
          }
        >
          <div style={{ display: 'grid', gap: 14 }}>
            <div className="aviso">
              <div>{NOME_TIPO[estornando.tipo]} de <strong>{numero(estornando.quantidade)}</strong> unidade(s) em {dataHora(estornando.data_movimentacao)} · efeito do estorno no saldo: <strong>{numero(-efeito(estornando.tipo, estornando.quantidade))}</strong></div>
            </div>
            <Campo rotulo="Motivo do estorno">
              <input className="input" autoFocus value={motivoEstorno} onChange={(e) => setMotivoEstorno(e.target.value)} placeholder="Ex.: venda cancelada no caixa" />
            </Campo>
          </div>
        </Modal>
      )}
    </>
  );
}
