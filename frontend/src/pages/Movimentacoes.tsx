import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { Download, Plus, X } from 'lucide-react';
import { api, consulta } from '../lib/api';
import { useCarregar } from '../lib/hooks';
import { useSessao } from '../lib/sessao';
import { NOME_TIPO, dataHora, efeito, numero } from '../lib/formato';
import { baixarCsv } from '../lib/csv';
import type { Movimentacao, Pagina, PosicaoProduto, TipoMovimentacao } from '../lib/tipos';
import { Carregando, ErroCarregamento, SeloTipo, Vazio } from '../components/ui';
import FormMovimentacao from '../components/FormMovimentacao';
import { useNotificacoes } from '../components/Notificacoes';

const TAMANHO = 30;
const TIPOS: TipoMovimentacao[] = ['ENTRADA', 'SAIDA', 'DEVOLUCAO', 'AJUSTE', 'ESTORNO'];

export default function Movimentacoes() {
  const { dataReferencia } = useSessao();
  const notificar = useNotificacoes();
  const [filtros, setFiltros] = useState({ produto_id: '', tipo: '', inicio: '', fim: '' });
  const [pagina, setPagina] = useState(0);
  const [novo, setNovo] = useState(false);

  const produtos = useCarregar(() => api.get<PosicaoProduto[]>(`/relatorios/posicao-estoque${consulta({ data_referencia: dataReferencia })}`), [dataReferencia]);
  const lista = useCarregar(
    () => api.get<Pagina<Movimentacao>>(`/movimentacoesestoque${consulta({ ...filtros, pagina, tamanho: TAMANHO })}`),
    [filtros, pagina],
  );
  const nomes = useMemo(() => new Map((produtos.dados ?? []).map((p) => [p.produto_id, p])), [produtos.dados]);

  const alterar = (campo: keyof typeof filtros, valor: string) => {
    setFiltros((f) => ({ ...f, [campo]: valor }));
    setPagina(0);
  };
  const temFiltro = Object.values(filtros).some(Boolean);

  const exportar = async () => {
    try {
      const tudo = await api.get<Pagina<Movimentacao>>(`/movimentacoesestoque${consulta({ ...filtros, pagina: 0, tamanho: 500 })}`);
      baixarCsv('movimentacoes.csv', ['Data', 'Produto', 'SKU', 'Tipo', 'Quantidade', 'Efeito no saldo', 'Saldo anterior', 'Saldo posterior', 'Motivo', 'Registrado por'],
        tudo.conteudo.map((m) => [dataHora(m.data_movimentacao), nomes.get(m.produto_id)?.nome, nomes.get(m.produto_id)?.sku, NOME_TIPO[m.tipo],
          m.quantidade, efeito(m.tipo, m.quantidade), m.saldo_anterior, m.saldo_posterior, m.motivo, m.registrado_por]));
      if (tudo.total_elementos > 500) notificar.sucesso('Exportação parcial', 'Foram exportadas as 500 movimentações mais recentes. Use os filtros para refinar.');
    } catch (e) {
      notificar.erro('Falha na exportação', (e as Error).message);
    }
  };

  const recarregar = () => { lista.recarregar(); produtos.recarregar(); };
  const p = lista.dados;

  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <h1>Movimentações</h1>
          <p>Histórico imutável de entradas, saídas, devoluções, ajustes e estornos</p>
        </div>
        <button className="btn btn-primario" onClick={() => setNovo(true)} disabled={!produtos.dados}><Plus size={16} /> Nova movimentação</button>
      </div>

      <section className="card">
        <div className="barra-ferramentas">
          <select className="select" value={filtros.produto_id} onChange={(e) => alterar('produto_id', e.target.value)} aria-label="Produto" style={{ maxWidth: 260 }}>
            <option value="">Todos os produtos</option>
            {(produtos.dados ?? []).map((x) => <option key={x.produto_id} value={x.produto_id}>{x.nome}</option>)}
          </select>
          <select className="select" value={filtros.tipo} onChange={(e) => alterar('tipo', e.target.value)} aria-label="Tipo">
            <option value="">Todos os tipos</option>
            {TIPOS.map((t) => <option key={t} value={t}>{NOME_TIPO[t]}</option>)}
          </select>
          <input className="input" type="date" style={{ width: 160 }} value={filtros.inicio} onChange={(e) => alterar('inicio', e.target.value)} aria-label="De" title="De" />
          <input className="input" type="date" style={{ width: 160 }} value={filtros.fim} onChange={(e) => alterar('fim', e.target.value)} aria-label="Até" title="Até" />
          {temFiltro && <button className="btn btn-fantasma btn-pequeno" onClick={() => { setFiltros({ produto_id: '', tipo: '', inicio: '', fim: '' }); setPagina(0); }}><X size={14} /> Limpar</button>}
          <span className="espaco" />
          <button className="btn btn-pequeno" onClick={exportar} disabled={!p?.total_elementos}><Download size={14} /> Exportar CSV</button>
        </div>

        {lista.erro ? <ErroCarregamento mensagem={lista.erro} tentarNovamente={lista.recarregar} /> : !p ? <Carregando /> : p.conteudo.length === 0 ? (
          <Vazio titulo="Nenhuma movimentação encontrada" texto={temFiltro ? 'Ajuste os filtros.' : undefined} />
        ) : (
          <>
            <div className="tabela-container" style={{ opacity: lista.carregando ? 0.6 : 1 }}>
              <table className="tabela">
                <thead>
                  <tr><th>Data</th><th>Produto</th><th>Tipo</th><th className="direita">Efeito</th><th className="direita">Saldo</th><th>Motivo</th><th>Registrado por</th></tr>
                </thead>
                <tbody>
                  {p.conteudo.map((m) => {
                    const ef = efeito(m.tipo, m.quantidade);
                    const prod = nomes.get(m.produto_id);
                    return (
                      <tr key={m.id} style={m.estornada ? { opacity: 0.55 } : undefined}>
                        <td className="nowrap num">{dataHora(m.data_movimentacao)}</td>
                        <td>
                          <Link to={`/produtos/${m.produto_id}`} className="celula-produto" style={{ color: 'inherit' }}>
                            <strong>{prod?.nome ?? '—'}</strong><span>{prod?.sku}</span>
                          </Link>
                        </td>
                        <td><SeloTipo tipo={m.tipo} />{m.estornada && <span className="selo selo-neutro" style={{ marginLeft: 6 }}>Estornada</span>}</td>
                        <td className={`direita num ${ef >= 0 ? 'efeito-positivo' : 'efeito-negativo'}`}>{ef > 0 ? '+' : ''}{numero(ef)}</td>
                        <td className="direita num nowrap muted">{numero(m.saldo_anterior)} → <strong style={{ color: 'var(--text)' }}>{numero(m.saldo_posterior)}</strong></td>
                        <td className="truncate" style={{ maxWidth: 300 }} title={m.motivo}>{m.motivo}</td>
                        <td className="muted small">{m.registrado_por ?? '—'}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            <div className="paginacao">
              <span className="muted small">
                {numero(p.pagina * p.tamanho + 1)}–{numero(p.pagina * p.tamanho + p.conteudo.length)} de {numero(p.total_elementos)}
              </span>
              <div style={{ display: 'flex', gap: 8 }}>
                <button className="btn btn-pequeno" disabled={pagina === 0} onClick={() => setPagina((x) => x - 1)}>Anterior</button>
                <button className="btn btn-pequeno" disabled={pagina >= p.total_paginas - 1} onClick={() => setPagina((x) => x + 1)}>Próxima</button>
              </div>
            </div>
          </>
        )}
      </section>

      {novo && produtos.dados && <FormMovimentacao produtos={produtos.dados} aoFechar={() => setNovo(false)} aoSalvar={recarregar} />}
    </>
  );
}
