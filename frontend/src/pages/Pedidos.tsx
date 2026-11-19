import { useMemo, useState, type FormEvent } from 'react';
import { Ban, ClipboardList, Loader2, PackageCheck, Plus, Sparkles } from 'lucide-react';
import { api, consulta } from '../lib/api';
import { useCarregar } from '../lib/hooks';
import { useSessao } from '../lib/sessao';
import { dataHora, numero, reais } from '../lib/formato';
import type { Fornecedor, PedidoCompra, PosicaoProduto, StatusPedido } from '../lib/tipos';
import { Campo, Carregando, Confirmar, ErroCarregamento, Modal, SeloSituacao, SeloStatusPedido, Vazio } from '../components/ui';
import { useNotificacoes } from '../components/Notificacoes';
import { avisarPedidosAlterados } from '../lib/eventos';

const FILTROS: { valor: StatusPedido | ''; rotulo: string }[] = [
  { valor: 'ABERTO', rotulo: 'Abertos' },
  { valor: 'RECEBIDO', rotulo: 'Recebidos' },
  { valor: 'CANCELADO', rotulo: 'Cancelados' },
  { valor: '', rotulo: 'Todos' },
];

export default function Pedidos() {
  const { usuario, dataReferencia } = useSessao();
  const notificar = useNotificacoes();
  const [status, setStatus] = useState<StatusPedido | ''>('ABERTO');
  const [aberto, setAberto] = useState<PedidoCompra | null>(null);
  const [acao, setAcao] = useState<{ tipo: 'receber' | 'cancelar'; pedido: PedidoCompra } | null>(null);
  const [confirmarSugestao, setConfirmarSugestao] = useState(false);
  const [novo, setNovo] = useState(false);

  const todos = useCarregar(() => api.get<PedidoCompra[]>('/pedidos-compra'), []);
  const contagem = useMemo(() => {
    const c: Record<string, number> = { '': todos.dados?.length ?? 0 };
    (todos.dados ?? []).forEach((p) => { c[p.status] = (c[p.status] ?? 0) + 1; });
    return c;
  }, [todos.dados]);
  const pedidos = (todos.dados ?? []).filter((p) => !status || p.status === status);

  const executar = async () => {
    if (!acao) return;
    const { tipo, pedido } = acao;
    try {
      const atualizado = await api.post<PedidoCompra>(`/pedidos-compra/${pedido.id}/${tipo === 'receber' ? 'recebimento' : 'cancelamento'}`);
      notificar.sucesso(tipo === 'receber' ? `Pedido ${pedido.codigo} recebido` : `Pedido ${pedido.codigo} cancelado`,
        tipo === 'receber' ? `${numero(pedido.quantidade_total)} unidades lançadas no estoque.` : undefined);
      setAberto((a) => (a?.id === atualizado.id ? atualizado : a));
      todos.recarregar();
      avisarPedidosAlterados();
    } catch (e) {
      notificar.erro('Operação recusada', (e as Error).message);
    }
  };

  const gerarSugestoes = async () => {
    try {
      const criados = await api.post<PedidoCompra[]>(`/pedidos-compra/sugestoes${consulta({ data_referencia: dataReferencia })}`);
      notificar.sucesso(criados.length ? `${criados.length} pedido(s) gerado(s)` : 'Nada a sugerir',
        criados.length ? 'Um pedido por fornecedor, com as quantidades para voltar ao estoque máximo.' : 'Nenhum produto precisa de reposição fora dos pedidos abertos.');
      setStatus('ABERTO');
      todos.recarregar();
      avisarPedidosAlterados();
    } catch (e) {
      notificar.erro('Não foi possível gerar os pedidos', (e as Error).message);
    }
  };

  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <h1>Pedidos de compra</h1>
          <p>Do alerta de reposição ao recebimento: ao receber um pedido, as entradas no estoque são lançadas automaticamente</p>
        </div>
        {usuario?.admin && (
          <div style={{ display: 'flex', gap: 10 }}>
            <button className="btn" onClick={() => setNovo(true)}><Plus size={16} /> Novo pedido</button>
            <button className="btn btn-primario" onClick={() => setConfirmarSugestao(true)}><Sparkles size={16} /> Gerar a partir dos alertas</button>
          </div>
        )}
      </div>

      <section className="card">
        <div className="barra-ferramentas">
          <div className="segmentado">
            {FILTROS.map((f) => (
              <button key={f.rotulo} className={status === f.valor ? 'ativo' : ''} onClick={() => setStatus(f.valor)}>
                {f.rotulo} <span className="contagem">{contagem[f.valor] ?? 0}</span>
              </button>
            ))}
          </div>
        </div>
        {todos.erro ? <ErroCarregamento mensagem={todos.erro} tentarNovamente={todos.recarregar} /> : !todos.dados ? <Carregando /> : pedidos.length === 0 ? (
          <Vazio titulo="Nenhum pedido aqui" texto={status === 'ABERTO' && usuario?.admin ? 'Gere pedidos a partir dos alertas de reposição.' : undefined} icone={<ClipboardList size={36} />} />
        ) : (
          <div className="tabela-container">
            <table className="tabela">
              <thead>
                <tr><th>Pedido</th><th>Fornecedor</th><th>Status</th><th className="direita">Itens</th><th className="direita">Unidades</th><th className="direita">Valor de referência</th><th>Criado</th><th className="direita">Ações</th></tr>
              </thead>
              <tbody>
                {pedidos.map((p) => (
                  <tr key={p.id} className="clicavel" onClick={() => setAberto(p)}>
                    <td><strong className="num">{p.codigo}</strong></td>
                    <td>{p.fornecedor}</td>
                    <td><SeloStatusPedido status={p.status} /></td>
                    <td className="direita num">{p.itens.length}</td>
                    <td className="direita num">{numero(p.quantidade_total)}</td>
                    <td className="direita num">{reais(p.valor_estimado)}</td>
                    <td className="muted small nowrap">{dataHora(p.criado_em)}<br />{p.criado_por}</td>
                    <td className="direita nowrap" onClick={(e) => e.stopPropagation()}>
                      {usuario?.admin && p.status === 'ABERTO' && (
                        <>
                          <button className="btn btn-pequeno" onClick={() => setAcao({ tipo: 'receber', pedido: p })}><PackageCheck size={14} /> Receber</button>
                          <button className="btn btn-fantasma btn-icone btn-pequeno" title="Cancelar" onClick={() => setAcao({ tipo: 'cancelar', pedido: p })}><Ban size={14} /></button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {aberto && (
        <Modal
          gaveta
          titulo={`Pedido ${aberto.codigo}`}
          descricao={aberto.fornecedor}
          aoFechar={() => setAberto(null)}
          rodape={usuario?.admin && aberto.status === 'ABERTO' ? (
            <>
              <button className="btn btn-perigo" onClick={() => setAcao({ tipo: 'cancelar', pedido: aberto })}><Ban size={15} /> Cancelar pedido</button>
              <button className="btn btn-primario" onClick={() => setAcao({ tipo: 'receber', pedido: aberto })}><PackageCheck size={15} /> Confirmar recebimento</button>
            </>
          ) : undefined}
        >
          <div style={{ display: 'grid', gap: 18 }}>
            <div className="detalhes">
              <div><div className="detalhe-rotulo">Status</div><SeloStatusPedido status={aberto.status} /></div>
              <div><div className="detalhe-rotulo">Unidades</div><div className="detalhe-valor">{numero(aberto.quantidade_total)}</div></div>
              <div><div className="detalhe-rotulo">Valor de referência</div><div className="detalhe-valor">{reais(aberto.valor_estimado)}</div></div>
              <div><div className="detalhe-rotulo">Criado</div><div className="small">{dataHora(aberto.criado_em)}<br /><span className="muted">{aberto.criado_por}</span></div></div>
              {aberto.recebido_em && <div><div className="detalhe-rotulo">Recebido</div><div className="small">{dataHora(aberto.recebido_em)}<br /><span className="muted">{aberto.recebido_por}</span></div></div>}
              {aberto.cancelado_em && <div><div className="detalhe-rotulo">Cancelado</div><div className="small">{dataHora(aberto.cancelado_em)}</div></div>}
            </div>
            {aberto.observacao && <div className="aviso"><div>{aberto.observacao}</div></div>}
            <div className="card" style={{ boxShadow: 'none' }}>
              <table className="tabela">
                <thead><tr><th>Produto</th><th className="direita">Quantidade</th><th className="direita">Valor</th></tr></thead>
                <tbody>
                  {aberto.itens.map((i) => (
                    <tr key={i.produto_id}>
                      <td><div className="celula-produto"><strong>{i.produto}</strong><span>{i.sku}</span></div></td>
                      <td className="direita num">{numero(i.quantidade)}</td>
                      <td className="direita num">{reais(i.valor_estimado)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <p className="muted small" style={{ margin: 0 }}>O valor de referência usa o preço de venda, pois o sistema não registra o custo de aquisição.</p>
          </div>
        </Modal>
      )}

      {acao && (
        <Confirmar
          titulo={acao.tipo === 'receber' ? `Receber pedido ${acao.pedido.codigo}` : `Cancelar pedido ${acao.pedido.codigo}`}
          mensagem={acao.tipo === 'receber'
            ? <>Confirme que a mercadoria chegou. Serão lançadas <strong>{acao.pedido.itens.length}</strong> entradas ({numero(acao.pedido.quantidade_total)} unidades) no estoque, todas de uma vez: se alguma falhar, nenhuma é registrada.</>
            : <>O pedido será cancelado e nenhuma entrada será lançada. Esta ação não pode ser desfeita.</>}
          textoConfirmar={acao.tipo === 'receber' ? 'Confirmar recebimento' : 'Cancelar pedido'}
          perigo={acao.tipo === 'cancelar'}
          aoConfirmar={executar}
          aoFechar={() => setAcao(null)}
        />
      )}
      {confirmarSugestao && (
        <Confirmar
          titulo="Gerar pedidos a partir dos alertas"
          mensagem="Será criado um pedido por fornecedor com os produtos que atingiram o ponto de pedido, na quantidade necessária para voltar ao estoque máximo. Produtos que já estão em pedidos abertos são ignorados."
          textoConfirmar="Gerar pedidos"
          aoConfirmar={gerarSugestoes}
          aoFechar={() => setConfirmarSugestao(false)}
        />
      )}
      {novo && <NovoPedido aoFechar={() => setNovo(false)} aoSalvar={() => { setStatus('ABERTO'); todos.recarregar(); avisarPedidosAlterados(); }} />}
    </>
  );
}

function NovoPedido({ aoFechar, aoSalvar }: { aoFechar: () => void; aoSalvar: () => void }) {
  const { dataReferencia } = useSessao();
  const notificar = useNotificacoes();
  const fornecedores = useCarregar(() => api.get<Fornecedor[]>('/fornecedores'), []);
  const posicao = useCarregar(() => api.get<PosicaoProduto[]>(`/relatorios/posicao-estoque${consulta({ data_referencia: dataReferencia })}`), [dataReferencia]);
  const [fornecedorId, setFornecedorId] = useState('');
  const [observacao, setObservacao] = useState('');
  const [quantidades, setQuantidades] = useState<Record<string, string>>({});
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const produtos = (posicao.dados ?? []).filter((p) => p.ativo && p.fornecedor_id === fornecedorId)
    .sort((a, b) => b.sugestao_compra - a.sugestao_compra || a.nome.localeCompare(b.nome, 'pt-BR'));

  const escolherFornecedor = (id: string) => {
    setFornecedorId(id);
    // já preenche com a sugestão calculada para quem precisa de reposição
    const iniciais: Record<string, string> = {};
    (posicao.dados ?? []).filter((p) => p.fornecedor_id === id && p.sugestao_compra > 0).forEach((p) => { iniciais[p.produto_id] = String(p.sugestao_compra); });
    setQuantidades(iniciais);
  };

  const itens = Object.entries(quantidades).filter(([, q]) => Number(q) > 0).map(([produto_id, q]) => ({ produto_id, quantidade: Number(q) }));

  const salvar = async (e: FormEvent) => {
    e.preventDefault();
    setErro(null);
    setSalvando(true);
    try {
      const criado = await api.post<PedidoCompra>('/pedidos-compra', { fornecedor_id: fornecedorId, observacao: observacao || null, itens });
      notificar.sucesso(`Pedido ${criado.codigo} criado`, `${criado.itens.length} item(ns), ${numero(criado.quantidade_total)} unidades.`);
      aoSalvar();
      aoFechar();
    } catch (err) {
      setErro((err as Error).message);
    } finally {
      setSalvando(false);
    }
  };

  return (
    <Modal
      titulo="Novo pedido de compra"
      descricao="Escolha o fornecedor e informe as quantidades. Produtos que precisam de reposição já vêm preenchidos com a sugestão."
      aoFechar={aoFechar}
      largo
      rodape={
        <>
          <button className="btn" onClick={aoFechar}>Cancelar</button>
          <button className="btn btn-primario" type="submit" form="form-pedido" disabled={salvando || !fornecedorId || itens.length === 0}>
            {salvando && <Loader2 size={15} className="giro" />} Criar pedido ({itens.length})
          </button>
        </>
      }
    >
      <form id="form-pedido" onSubmit={salvar} style={{ display: 'grid', gap: 14 }}>
        {erro && <div className="aviso aviso-perigo">{erro}</div>}
        <div className="form-grade">
          <Campo rotulo="Fornecedor">
            <select className="select" required value={fornecedorId} onChange={(e) => escolherFornecedor(e.target.value)}>
              <option value="">Selecione…</option>
              {[...(fornecedores.dados ?? [])].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')).map((f) => <option key={f.id} value={f.id}>{f.nome}</option>)}
            </select>
          </Campo>
          <Campo rotulo="Observação">
            <input className="input" maxLength={255} value={observacao} onChange={(e) => setObservacao(e.target.value)} placeholder="Opcional" />
          </Campo>
        </div>
        {fornecedorId && (produtos.length === 0 ? <Vazio titulo="Este fornecedor não tem produtos ativos" /> : (
          <div className="card" style={{ boxShadow: 'none', maxHeight: 380, overflowY: 'auto' }}>
            <table className="tabela">
              <thead><tr><th>Produto</th><th>Situação</th><th className="direita">Saldo</th><th className="direita">Sugestão</th><th className="direita" style={{ width: 120 }}>Comprar</th></tr></thead>
              <tbody>
                {produtos.map((p) => (
                  <tr key={p.produto_id}>
                    <td><div className="celula-produto"><strong>{p.nome}</strong><span>{p.sku}</span></div></td>
                    <td><SeloSituacao situacao={p.situacao} /></td>
                    <td className="direita num">{numero(p.saldo)}</td>
                    <td className="direita num muted">{p.sugestao_compra ? numero(p.sugestao_compra) : '—'}</td>
                    <td className="direita">
                      <input className="input" type="number" min={0} style={{ height: 32, textAlign: 'right' }}
                             value={quantidades[p.produto_id] ?? ''} placeholder="0"
                             onChange={(e) => setQuantidades((q) => ({ ...q, [p.produto_id]: e.target.value }))} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ))}
      </form>
    </Modal>
  );
}
