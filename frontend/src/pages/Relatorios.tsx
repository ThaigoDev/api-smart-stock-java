import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { CheckCircle2, Download, RefreshCw, ShieldAlert } from 'lucide-react';
import { api, consulta } from '../lib/api';
import { useCarregar } from '../lib/hooks';
import { useSessao } from '../lib/sessao';
import { data, decimal1, mesCurto, numero, percentual, reais, somarDias } from '../lib/formato';
import { baixarCsv } from '../lib/csv';
import type { Conciliacao, CurvaABC, Giro, ResumoMensal } from '../lib/tipos';
import { Carregando, ErroCarregamento, Kpi } from '../components/ui';
import { GraficoCurvaABC, GraficoFaturamento } from '../components/Graficos';

type Aba = 'abc' | 'giro' | 'mensal' | 'conciliacao';
const ABAS: { id: Aba; rotulo: string }[] = [
  { id: 'abc', rotulo: 'Curva ABC' },
  { id: 'giro', rotulo: 'Giro de estoque' },
  { id: 'mensal', rotulo: 'Série mensal' },
  { id: 'conciliacao', rotulo: 'Conciliação' },
];
const PERIODOS = [30, 90, 180, 365];

export default function Relatorios() {
  const { dataReferencia } = useSessao();
  const [aba, setAba] = useState<Aba>('abc');
  const [dias, setDias] = useState(90);
  const inicio = somarDias(dataReferencia, -(dias - 1));

  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <h1>Relatórios</h1>
          <p>Indicadores calculados a partir do histórico de movimentações · {data(inicio)} a {data(dataReferencia)}</p>
        </div>
        {aba !== 'conciliacao' && (
          <div className="segmentado" aria-label="Período">
            {PERIODOS.map((d) => <button key={d} className={dias === d ? 'ativo' : ''} onClick={() => setDias(d)}>{d === 365 ? '12 meses' : `${d} dias`}</button>)}
          </div>
        )}
      </div>
      <section className="card">
        <div className="abas">
          {ABAS.map((a) => <button key={a.id} className={aba === a.id ? 'ativo' : ''} onClick={() => setAba(a.id)}>{a.rotulo}</button>)}
        </div>
        {aba === 'abc' && <AbaCurvaABC inicio={inicio} fim={dataReferencia} />}
        {aba === 'giro' && <AbaGiro inicio={inicio} fim={dataReferencia} />}
        {aba === 'mensal' && <AbaMensal inicio={inicio} fim={dataReferencia} />}
        {aba === 'conciliacao' && <AbaConciliacao />}
      </section>
    </>
  );
}

function AbaCurvaABC({ inicio, fim }: { inicio: string; fim: string }) {
  const { dados, erro, recarregar } = useCarregar(() => api.get<CurvaABC>(`/relatorios/curva-abc${consulta({ inicio, fim })}`), [inicio, fim]);
  if (erro) return <ErroCarregamento mensagem={erro} tentarNovamente={recarregar} />;
  if (!dados) return <Carregando />;
  const exportar = () => baixarCsv(`curva-abc-${inicio}-a-${fim}.csv`,
    ['Posição', 'Produto', 'Quantidade vendida', 'Valor vendido (R$)', '% do valor', '% acumulado', 'Classe'],
    dados.itens.map((i, idx) => [idx + 1, i.nome, i.quantidade_vendida, Number(i.valor_vendido), Number(i.percentual_valor), Number(i.percentual_acumulado), i.classe]));
  return (
    <div className="card-corpo" style={{ display: 'grid', gap: 18 }}>
      <div className="grade grade-kpi">
        {dados.resumo_por_classe.map((c) => (
          <Kpi key={c.classe} rotulo={`Classe ${c.classe}`} valor={`${percentual(Number(c.percentual_valor))} do valor`}
               detalhe={`${c.quantidade_produtos} produtos (${percentual(Number(c.percentual_produtos))} dos itens) · ${reais(c.valor_vendido)}`}
               icone={<strong>{c.classe}</strong>} />
        ))}
      </div>
      <div>
        <h3>Curva acumulada</h3>
        <p className="muted small" style={{ margin: '2px 0 8px' }}>Quanto mais acentuado o início, mais o faturamento se concentra em poucos produtos</p>
        <GraficoCurvaABC itens={dados.itens} />
      </div>
      <div className="card" style={{ boxShadow: 'none' }}>
        <div className="barra-ferramentas">
          <strong>{numero(dados.itens.length)} produtos · {reais(dados.valor_total_vendido)} vendidos</strong>
          <span className="espaco" />
          <button className="btn btn-pequeno" onClick={exportar}><Download size={14} /> Exportar CSV</button>
        </div>
        <div className="tabela-container" style={{ maxHeight: 480 }}>
          <table className="tabela">
            <thead><tr><th>#</th><th>Produto</th><th className="direita">Unidades</th><th className="direita">Valor vendido</th><th className="direita">% do valor</th><th className="direita">% acumulado</th><th className="centro">Classe</th></tr></thead>
            <tbody>
              {dados.itens.map((i, idx) => (
                <tr key={i.produto_id}>
                  <td className="muted num">{idx + 1}</td>
                  <td><Link to={`/produtos/${i.produto_id}`} style={{ color: 'inherit' }}>{i.nome}</Link></td>
                  <td className="direita num">{numero(i.quantidade_vendida)}</td>
                  <td className="direita num">{reais(i.valor_vendido)}</td>
                  <td className="direita num">{percentual(Number(i.percentual_valor))}</td>
                  <td className="direita num">{percentual(Number(i.percentual_acumulado))}</td>
                  <td className="centro"><span className={`selo ${i.classe === 'A' ? 'selo-azul' : 'selo-neutro'}`}>{i.classe}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function AbaGiro({ inicio, fim }: { inicio: string; fim: string }) {
  const { dados, erro, recarregar } = useCarregar(() => api.get<Giro>(`/relatorios/giro${consulta({ inicio, fim })}`), [inicio, fim]);
  const porCategoria = useMemo(() => {
    const mapa = new Map<string, number[]>();
    (dados?.itens ?? []).forEach((i) => { if (i.giro != null) mapa.set(i.categoria, [...(mapa.get(i.categoria) ?? []), i.giro]); });
    return [...mapa.entries()].map(([categoria, g]) => ({ categoria, giro: g.reduce((a, b) => a + b, 0) / g.length, produtos: g.length }))
      .sort((a, b) => b.giro - a.giro);
  }, [dados]);
  if (erro) return <ErroCarregamento mensagem={erro} tentarNovamente={recarregar} />;
  if (!dados) return <Carregando />;
  const maior = Math.max(1, ...porCategoria.map((c) => c.giro));
  const exportar = () => baixarCsv(`giro-${inicio}-a-${fim}.csv`,
    ['Produto', 'Categoria', 'Quantidade vendida', 'Estoque médio', 'Giro', 'Dias médios em estoque', 'Saldo atual', 'Cobertura (dias)'],
    dados.itens.map((i) => [i.nome, i.categoria, i.quantidade_vendida, i.estoque_medio, i.giro, i.dias_medios_em_estoque, i.saldo_atual, i.cobertura_dias]));
  return (
    <div className="card-corpo" style={{ display: 'grid', gap: 18 }}>
      <div className="grade grade-1-1">
        <div>
          <h3>Giro médio por categoria</h3>
          <p className="muted small" style={{ margin: '2px 0 10px' }}>Quantas vezes o estoque médio foi renovado no período · média geral {decimal1(dados.giro_medio)}</p>
          <div className="lista">
            {porCategoria.map((c) => (
              <div key={c.categoria} className="lista-item" style={{ padding: '8px 0' }}>
                <div className="cresce">
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}><span>{c.categoria}</span><strong className="num">{decimal1(c.giro)}</strong></div>
                  <div className="barra-progresso"><div style={{ width: `${(100 * c.giro) / maior}%` }} /></div>
                </div>
              </div>
            ))}
          </div>
        </div>
        <div className="aviso" style={{ alignSelf: 'start' }}>
          <div>
            <strong>Como ler o giro</strong>
            <p style={{ margin: '6px 0 0' }}>Giro alto indica produto que vende rápido e fica pouco tempo parado. Giro baixo indica capital imobilizado: o produto passa muitos dias na prateleira. A coluna "dias em estoque" é o período dividido pelo giro.</p>
          </div>
        </div>
      </div>
      <div className="card" style={{ boxShadow: 'none' }}>
        <div className="barra-ferramentas">
          <strong>Por produto</strong><span className="espaco" />
          <button className="btn btn-pequeno" onClick={exportar}><Download size={14} /> Exportar CSV</button>
        </div>
        <div className="tabela-container" style={{ maxHeight: 480 }}>
          <table className="tabela">
            <thead><tr><th>Produto</th><th>Categoria</th><th className="direita">Vendido</th><th className="direita">Estoque médio</th><th className="direita">Giro</th><th className="direita">Dias em estoque</th><th className="direita">Cobertura</th></tr></thead>
            <tbody>
              {dados.itens.map((i) => (
                <tr key={i.produto_id}>
                  <td><Link to={`/produtos/${i.produto_id}`} style={{ color: 'inherit' }}>{i.nome}</Link></td>
                  <td className="muted">{i.categoria}</td>
                  <td className="direita num">{numero(i.quantidade_vendida)}</td>
                  <td className="direita num">{decimal1(i.estoque_medio)}</td>
                  <td className="direita num"><strong>{decimal1(i.giro)}</strong></td>
                  <td className="direita num">{i.dias_medios_em_estoque == null ? '—' : `${decimal1(i.dias_medios_em_estoque)} d`}</td>
                  <td className="direita num">{i.cobertura_dias == null ? '—' : `${decimal1(i.cobertura_dias)} d`}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function AbaMensal({ inicio, fim }: { inicio: string; fim: string }) {
  const { dados, erro, recarregar } = useCarregar(() => api.get<ResumoMensal[]>(`/relatorios/movimentacoes-mensais${consulta({ inicio, fim })}`), [inicio, fim]);
  if (erro) return <ErroCarregamento mensagem={erro} tentarNovamente={recarregar} />;
  if (!dados) return <Carregando />;
  const exportar = () => baixarCsv(`serie-mensal-${inicio}-a-${fim}.csv`,
    ['Mês', 'Movimentações', 'Unidades de entrada', 'Unidades vendidas', 'Unidades devolvidas', 'Saldo de ajustes', 'Estornos', 'Faturamento (R$)'],
    dados.map((m) => [m.mes, m.quantidade_movimentacoes, m.unidades_entrada, m.unidades_saida, m.unidades_devolvidas, m.saldo_ajustes, m.quantidade_estornos, Number(m.faturamento)]));
  return (
    <div className="card-corpo" style={{ display: 'grid', gap: 18 }}>
      <div>
        <h3>Faturamento por mês</h3>
        <p className="muted small" style={{ margin: '2px 0 8px' }}>Meses no início e no fim do período podem estar incompletos</p>
        <GraficoFaturamento meses={dados} altura={280} />
      </div>
      <div className="card" style={{ boxShadow: 'none' }}>
        <div className="barra-ferramentas"><strong>Detalhamento</strong><span className="espaco" /><button className="btn btn-pequeno" onClick={exportar}><Download size={14} /> Exportar CSV</button></div>
        <div className="tabela-container">
          <table className="tabela">
            <thead><tr><th>Mês</th><th className="direita">Movimentações</th><th className="direita">Entradas</th><th className="direita">Vendidas</th><th className="direita">Devolvidas</th><th className="direita">Ajustes</th><th className="direita">Estornos</th><th className="direita">Faturamento</th></tr></thead>
            <tbody>
              {dados.map((m) => (
                <tr key={m.mes}>
                  <td>{mesCurto(m.mes)}</td>
                  <td className="direita num">{numero(m.quantidade_movimentacoes)}</td>
                  <td className="direita num">{numero(m.unidades_entrada)}</td>
                  <td className="direita num">{numero(m.unidades_saida)}</td>
                  <td className="direita num">{numero(m.unidades_devolvidas)}</td>
                  <td className="direita num">{numero(m.saldo_ajustes)}</td>
                  <td className="direita num">{numero(m.quantidade_estornos)}</td>
                  <td className="direita num"><strong>{reais(m.faturamento)}</strong></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function AbaConciliacao() {
  const { dados, erro, carregando, recarregar } = useCarregar(() => api.get<Conciliacao>('/relatorios/conciliacao'), []);
  if (erro) return <ErroCarregamento mensagem={erro} tentarNovamente={recarregar} />;
  if (!dados) return <Carregando texto="Auditando o histórico…" />;
  return (
    <div className="card-corpo" style={{ display: 'grid', gap: 18 }}>
      <div className={`aviso ${dados.consistente ? 'aviso-sucesso' : 'aviso-perigo'}`} style={{ padding: 18 }}>
        {dados.consistente ? <CheckCircle2 size={22} /> : <ShieldAlert size={22} />}
        <div style={{ flex: 1 }}>
          <strong style={{ fontSize: 15 }}>{dados.consistente ? 'Estoque íntegro' : 'Foram encontradas divergências'}</strong>
          <p style={{ margin: '4px 0 0' }}>
            O saldo de cada produto foi comparado com a soma de todo o seu histórico, e cada movimentação com a anterior.
            {' '}{numero(dados.movimentacoes_verificadas)} movimentações de {numero(dados.produtos_verificados)} produtos verificadas.
          </p>
        </div>
        <button className="btn btn-pequeno" onClick={recarregar} disabled={carregando}><RefreshCw size={14} className={carregando ? 'giro' : ''} /> Auditar novamente</button>
      </div>
      <div className="grade grade-kpi">
        <Kpi rotulo="Produtos consistentes" valor={`${numero(dados.produtos_consistentes)} / ${numero(dados.produtos_verificados)}`} icone={<CheckCircle2 size={17} />} cor="var(--normal)" />
        <Kpi rotulo="Quebras de encadeamento" valor={numero(dados.quebras_de_encadeamento)} icone={<ShieldAlert size={17} />} cor={dados.quebras_de_encadeamento ? 'var(--ruptura)' : 'var(--normal)'} />
        <Kpi rotulo="Saldos negativos" valor={numero(dados.produtos_com_saldo_negativo)} icone={<ShieldAlert size={17} />} cor={dados.produtos_com_saldo_negativo ? 'var(--ruptura)' : 'var(--normal)'} />
      </div>
      {dados.divergencias.length > 0 && (
        <table className="tabela">
          <thead><tr><th>Produto</th><th className="direita">Saldo registrado</th><th className="direita">Saldo recalculado</th><th className="direita">Quebras</th></tr></thead>
          <tbody>
            {dados.divergencias.map((d) => (
              <tr key={d.produto_id}><td>{d.nome}</td><td className="direita num">{numero(d.saldo_registrado)}</td><td className="direita num">{numero(d.saldo_recalculado)}</td><td className="direita num">{numero(d.quebras_de_encadeamento)}</td></tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
