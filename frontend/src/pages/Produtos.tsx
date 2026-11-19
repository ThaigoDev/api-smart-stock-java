import { useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowDown, ArrowUp, ArrowUpDown, Download, PackagePlus, Pencil, Power, Search, Shuffle } from 'lucide-react';
import { api, consulta } from '../lib/api';
import { useCarregar } from '../lib/hooks';
import { useSessao } from '../lib/sessao';
import { NOME_SITUACAO, SITUACOES, decimal1, numero, reais } from '../lib/formato';
import { baixarCsv } from '../lib/csv';
import type { PosicaoProduto, Situacao } from '../lib/tipos';
import { Carregando, Confirmar, ErroCarregamento, SeloSituacao, Vazio } from '../components/ui';
import FormProduto from '../components/FormProduto';
import FormMovimentacao from '../components/FormMovimentacao';
import { useNotificacoes } from '../components/Notificacoes';

type Coluna = 'nome' | 'categoria' | 'saldo' | 'cobertura_dias' | 'situacao' | 'valor_em_estoque';
const ORDEM_SITUACAO: Record<Situacao, number> = { RUPTURA: 0, CRITICO: 1, REPOR: 2, NORMAL: 3, SUPERESTOQUE: 4 };

export default function Produtos() {
  const { dataReferencia, usuario } = useSessao();
  const navegar = useNavigate();
  const notificar = useNotificacoes();
  const [parametros, setParametros] = useSearchParams();
  const [busca, setBusca] = useState('');
  const [categoria, setCategoria] = useState('');
  const [mostrarInativos, setMostrarInativos] = useState(false);
  const [ordem, setOrdem] = useState<{ coluna: Coluna; asc: boolean }>({ coluna: 'nome', asc: true });
  const [editando, setEditando] = useState<string | null | undefined>(undefined); // undefined = fechado, null = novo
  const [movimentando, setMovimentando] = useState<string | null>(null);
  const [alterandoStatus, setAlterandoStatus] = useState<PosicaoProduto | null>(null);

  const situacaoFiltro = (parametros.get('situacao') as Situacao | null) ?? '';
  const somenteAlertas = parametros.get('alerta') === '1';

  const { dados, carregando, erro, recarregar } = useCarregar(
    () => api.get<PosicaoProduto[]>(`/relatorios/posicao-estoque${consulta({ data_referencia: dataReferencia })}`),
    [dataReferencia],
  );

  const categorias = useMemo(() => Array.from(new Set((dados ?? []).map((p) => p.categoria))).sort((a, b) => a.localeCompare(b, 'pt-BR')), [dados]);

  const filtrados = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    const lista = (dados ?? []).filter((p) =>
      (mostrarInativos || p.ativo)
      && (!termo || p.nome.toLowerCase().includes(termo) || p.sku.toLowerCase().includes(termo))
      && (!categoria || p.categoria === categoria)
      && (!situacaoFiltro || p.situacao === situacaoFiltro)
      && (!somenteAlertas || (p.situacao != null && ['RUPTURA', 'CRITICO', 'REPOR'].includes(p.situacao))));
    const valor = (p: PosicaoProduto): string | number => {
      switch (ordem.coluna) {
        case 'situacao': return p.situacao ? ORDEM_SITUACAO[p.situacao] : 9;
        case 'cobertura_dias': return p.cobertura_dias ?? Number.MAX_SAFE_INTEGER;
        case 'nome': case 'categoria': return p[ordem.coluna].toLowerCase();
        default: return Number(p[ordem.coluna]);
      }
    };
    return [...lista].sort((a, b) => {
      const va = valor(a); const vb = valor(b);
      const c = typeof va === 'string' && typeof vb === 'string' ? va.localeCompare(vb, 'pt-BR') : va < vb ? -1 : va > vb ? 1 : 0;
      return ordem.asc ? c : -c;
    });
  }, [dados, busca, categoria, situacaoFiltro, somenteAlertas, mostrarInativos, ordem]);

  const definirSituacao = (s: string) => {
    const novo = new URLSearchParams(parametros);
    novo.delete('alerta');
    if (s) novo.set('situacao', s); else novo.delete('situacao');
    setParametros(novo, { replace: true });
  };

  const ordenar = (coluna: Coluna) => setOrdem((o) => ({ coluna, asc: o.coluna === coluna ? !o.asc : coluna === 'nome' || coluna === 'categoria' }));
  const Cabecalho = ({ coluna, rotulo, direita }: { coluna: Coluna; rotulo: string; direita?: boolean }) => (
    <th className={`th-ordenavel ${direita ? 'direita' : ''}`} onClick={() => ordenar(coluna)}>
      <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
        {rotulo}
        {ordem.coluna === coluna ? (ordem.asc ? <ArrowUp size={12} /> : <ArrowDown size={12} />) : <ArrowUpDown size={12} opacity={0.4} />}
      </span>
    </th>
  );

  const exportar = () => baixarCsv(`produtos-${dataReferencia}.csv`,
    ['Produto', 'SKU', 'Categoria', 'Fornecedor', 'Ativo', 'Saldo', 'Mínimo', 'Máximo', 'Ponto de pedido', 'Demanda diária', 'Cobertura (dias)', 'Situação', 'Sugestão de compra', 'Preço (R$)', 'Valor em estoque (R$)'],
    filtrados.map((p) => [p.nome, p.sku, p.categoria, p.fornecedor, p.ativo ? 'Sim' : 'Não', p.saldo, p.estoque_minimo, p.estoque_maximo,
      p.ponto_pedido, p.demanda_media_diaria, p.cobertura_dias, p.situacao ? NOME_SITUACAO[p.situacao] : 'Sem estoque',
      p.sugestao_compra, Number(p.preco), Number(p.valor_em_estoque)]));

  const alternarStatus = async () => {
    if (!alterandoStatus) return;
    try {
      await api.patch(`/produtos/${alterandoStatus.produto_id}/status`, { ativo: !alterandoStatus.ativo });
      notificar.sucesso(alterandoStatus.ativo ? 'Produto inativado' : 'Produto reativado', alterandoStatus.nome);
      recarregar();
    } catch (e) {
      notificar.erro('Não foi possível alterar o produto', (e as Error).message);
    }
  };

  const abrirEstoque = async (p: PosicaoProduto) => {
    try {
      await api.post('/estoques', { produto_id: p.produto_id, quantidade: 0 });
      notificar.sucesso('Estoque aberto', `${p.nome} já pode receber movimentações.`);
      recarregar();
    } catch (e) {
      notificar.erro('Não foi possível abrir o estoque', (e as Error).message);
    }
  };

  if (erro) return <ErroCarregamento mensagem={erro} tentarNovamente={recarregar} />;

  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <h1>Produtos</h1>
          <p>{dados ? `${numero(filtrados.length)} de ${numero(dados.filter((p) => mostrarInativos || p.ativo).length)} produtos` : 'Carregando…'}</p>
        </div>
        <div style={{ display: 'flex', gap: 10 }}>
          <button className="btn" onClick={() => setMovimentando('')}><Shuffle size={16} /> Movimentar</button>
          {usuario?.admin && <button className="btn btn-primario" onClick={() => setEditando(null)}><PackagePlus size={16} /> Novo produto</button>}
        </div>
      </div>

      <section className="card">
        <div className="barra-ferramentas">
          <label className="input-com-icone">
            <Search size={16} />
            <input className="input" placeholder="Buscar por nome ou SKU" value={busca} onChange={(e) => setBusca(e.target.value)} />
          </label>
          <select className="select" value={categoria} onChange={(e) => setCategoria(e.target.value)} aria-label="Categoria">
            <option value="">Todas as categorias</option>
            {categorias.map((c) => <option key={c}>{c}</option>)}
          </select>
          <select className="select" value={somenteAlertas ? 'ALERTA' : situacaoFiltro} onChange={(e) => e.target.value === 'ALERTA' ? setParametros({ alerta: '1' }, { replace: true }) : definirSituacao(e.target.value)} aria-label="Situação">
            <option value="">Todas as situações</option>
            <option value="ALERTA">Precisam de reposição</option>
            {SITUACOES.map((s) => <option key={s} value={s}>{NOME_SITUACAO[s]}</option>)}
          </select>
          <label className="small" style={{ display: 'flex', alignItems: 'center', gap: 6, color: 'var(--text-2)' }}>
            <input type="checkbox" checked={mostrarInativos} onChange={(e) => setMostrarInativos(e.target.checked)} /> Mostrar inativos
          </label>
          <span className="espaco" />
          <button className="btn btn-pequeno" onClick={exportar} disabled={!filtrados.length}><Download size={14} /> Exportar CSV</button>
        </div>

        {carregando && !dados ? <Carregando /> : filtrados.length === 0 ? (
          <Vazio titulo="Nenhum produto encontrado" texto="Ajuste a busca ou os filtros." />
        ) : (
          <div className="tabela-container">
            <table className="tabela">
              <thead>
                <tr>
                  <Cabecalho coluna="nome" rotulo="Produto" />
                  <Cabecalho coluna="categoria" rotulo="Categoria" />
                  <Cabecalho coluna="saldo" rotulo="Saldo" direita />
                  <th className="direita">Mín / Máx</th>
                  <Cabecalho coluna="cobertura_dias" rotulo="Cobertura" direita />
                  <Cabecalho coluna="situacao" rotulo="Situação" />
                  <Cabecalho coluna="valor_em_estoque" rotulo="Valor em estoque" direita />
                  <th className="direita">Ações</th>
                </tr>
              </thead>
              <tbody>
                {filtrados.map((p) => (
                  <tr key={p.produto_id} className="clicavel" onClick={() => navegar(`/produtos/${p.produto_id}`)} style={p.ativo ? undefined : { opacity: 0.55 }}>
                    <td>
                      <div className="celula-produto">
                        <strong>{p.nome} {!p.ativo && <span className="selo selo-neutro" style={{ marginLeft: 6 }}>Inativo</span>}</strong>
                        <span>{p.sku} · {reais(p.preco)}</span>
                      </div>
                    </td>
                    <td className="muted">{p.categoria}</td>
                    <td className="direita num"><strong>{numero(p.saldo)}</strong></td>
                    <td className="direita num muted">{numero(p.estoque_minimo)} / {p.estoque_maximo == null ? '—' : numero(p.estoque_maximo)}</td>
                    <td className="direita num">{p.cobertura_dias == null ? '—' : `${decimal1(p.cobertura_dias)} d`}</td>
                    <td><SeloSituacao situacao={p.situacao} /></td>
                    <td className="direita num">{reais(p.valor_em_estoque)}</td>
                    <td className="direita nowrap" onClick={(e) => e.stopPropagation()}>
                      {p.possui_estoque ? (
                        p.ativo && <button className="btn btn-fantasma btn-icone btn-pequeno" title="Registrar movimentação" onClick={() => setMovimentando(p.produto_id)}><Shuffle size={15} /></button>
                      ) : usuario?.admin && (
                        <button className="btn btn-pequeno" onClick={() => abrirEstoque(p)}>Abrir estoque</button>
                      )}
                      {usuario?.admin && (
                        <>
                          <button className="btn btn-fantasma btn-icone btn-pequeno" title="Editar" onClick={() => setEditando(p.produto_id)}><Pencil size={15} /></button>
                          <button className="btn btn-fantasma btn-icone btn-pequeno" title={p.ativo ? 'Inativar' : 'Reativar'} onClick={() => setAlterandoStatus(p)}><Power size={15} /></button>
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

      {editando !== undefined && <FormProduto produtoId={editando ?? undefined} aoFechar={() => setEditando(undefined)} aoSalvar={recarregar} />}
      {movimentando !== null && dados && (
        <FormMovimentacao produtos={dados} produtoInicial={movimentando || undefined} aoFechar={() => setMovimentando(null)} aoSalvar={recarregar} />
      )}
      {alterandoStatus && (
        <Confirmar
          titulo={alterandoStatus.ativo ? 'Inativar produto' : 'Reativar produto'}
          mensagem={alterandoStatus.ativo
            ? <>O produto <strong>{alterandoStatus.nome}</strong> deixará de aceitar movimentações, mas seu histórico é preservado e ele continua nos relatórios.</>
            : <>O produto <strong>{alterandoStatus.nome}</strong> voltará a aceitar movimentações.</>}
          textoConfirmar={alterandoStatus.ativo ? 'Inativar' : 'Reativar'}
          perigo={alterandoStatus.ativo}
          aoConfirmar={alternarStatus}
          aoFechar={() => setAlterandoStatus(null)}
        />
      )}
    </>
  );
}
