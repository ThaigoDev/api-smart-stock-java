import { useMemo } from 'react';
import {
  Area, AreaChart, Bar, BarChart, CartesianGrid, Line, LineChart, ReferenceLine, ResponsiveContainer, Tooltip,
  XAxis, YAxis,
} from 'recharts';
import { useSessao } from '../lib/sessao';
import { data, mesCurto, numero, percentual, reais, reaisCompacto } from '../lib/formato';
import type { ItemCurvaABC, Movimentacao, ResumoMensal } from '../lib/tipos';

/** Resolve os tokens de cor do tema ativo (atributos SVG não aceitam var() de forma confiável). */
function useCores() {
  const { tema } = useSessao();
  return useMemo(() => {
    const estilo = getComputedStyle(document.documentElement);
    const ler = (nome: string) => estilo.getPropertyValue(nome).trim();
    return {
      serie: ler('--serie-1'),
      serieSuave: ler('--serie-1-soft'),
      grade: ler('--grade'),
      texto: ler('--text-3'),
      ruptura: ler('--ruptura'),
      superestoque: ler('--superestoque'),
      repor: ler('--repor'),
      superficie: ler('--surface'),
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tema]);
}

const eixo = { fontSize: 12, tickLine: false, axisLine: false } as const;

function CaixaTooltip({ titulo, linhas }: { titulo: string; linhas: [string, string][] }) {
  return (
    <div className="grafico-tooltip">
      <div className="titulo">{titulo}</div>
      {linhas.map(([rotulo, valor]) => (
        <div key={rotulo}><span className="muted">{rotulo}: </span><strong className="num">{valor}</strong></div>
      ))}
    </div>
  );
}

// ------------------------------------------------------------ faturamento mensal

export function GraficoFaturamento({ meses, altura = 260 }: { meses: ResumoMensal[]; altura?: number }) {
  const cores = useCores();
  const dados = meses.map((m) => ({ ...m, rotulo: mesCurto(m.mes), faturamento: Number(m.faturamento) }));
  return (
    <ResponsiveContainer width="100%" height={altura}>
      <BarChart data={dados} margin={{ top: 8, right: 8, left: 4, bottom: 0 }} barCategoryGap="28%">
        <CartesianGrid vertical={false} stroke={cores.grade} />
        <XAxis dataKey="rotulo" {...eixo} tick={{ fill: cores.texto }} />
        <YAxis {...eixo} tick={{ fill: cores.texto }} tickFormatter={(v) => reaisCompacto(v)} width={72} />
        <Tooltip
          cursor={{ fill: cores.grade, opacity: 0.5 }}
          content={({ active, payload }) => {
            if (!active || !payload?.length) return null;
            const m = payload[0].payload as ResumoMensal & { rotulo: string };
            return <CaixaTooltip titulo={m.rotulo} linhas={[
              ['Faturamento', reais(m.faturamento)],
              ['Unidades vendidas', numero(m.unidades_saida)],
              ['Movimentações', numero(m.quantidade_movimentacoes)],
            ]} />;
          }}
        />
        <Bar dataKey="faturamento" fill={cores.serie} radius={[4, 4, 0, 0]} maxBarSize={56} isAnimationActive={false} />
      </BarChart>
    </ResponsiveContainer>
  );
}

// ------------------------------------------------------------ curva ABC acumulada

export function GraficoCurvaABC({ itens }: { itens: ItemCurvaABC[] }) {
  const cores = useCores();
  const n = itens.length || 1;
  const dados = [{ x: 0, acumulado: 0, nome: '', classe: '' }].concat(
    itens.map((i, idx) => ({ x: Math.round(((idx + 1) / n) * 1000) / 10, acumulado: Number(i.percentual_acumulado), nome: i.nome, classe: i.classe })),
  );
  return (
    <ResponsiveContainer width="100%" height={300}>
      <AreaChart data={dados} margin={{ top: 10, right: 16, left: 0, bottom: 4 }}>
        <defs>
          <linearGradient id="gradAbc" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor={cores.serie} stopOpacity={0.25} />
            <stop offset="100%" stopColor={cores.serie} stopOpacity={0.02} />
          </linearGradient>
        </defs>
        <CartesianGrid vertical={false} stroke={cores.grade} />
        <XAxis dataKey="x" type="number" domain={[0, 100]} {...eixo} tick={{ fill: cores.texto }} tickFormatter={(v) => `${v}%`} />
        <YAxis domain={[0, 100]} {...eixo} tick={{ fill: cores.texto }} tickFormatter={(v) => `${v}%`} width={44} />
        <ReferenceLine y={80} stroke={cores.texto} strokeDasharray="4 4" label={{ value: 'Limite da classe A (80%)', position: 'insideTopLeft', fill: cores.texto, fontSize: 11 }} />
        <ReferenceLine y={95} stroke={cores.texto} strokeDasharray="4 4" label={{ value: 'Limite da classe B (95%)', position: 'insideTopLeft', fill: cores.texto, fontSize: 11 }} />
        <Tooltip
          content={({ active, payload }) => {
            if (!active || !payload?.length) return null;
            const p = payload[0].payload as { x: number; acumulado: number; nome: string; classe: string };
            if (!p.nome) return null;
            return <CaixaTooltip titulo={`${p.nome} · classe ${p.classe}`} linhas={[
              ['Itens acumulados', percentual(p.x)],
              ['Valor acumulado', percentual(p.acumulado)],
            ]} />;
          }}
        />
        <Area type="monotone" dataKey="acumulado" stroke={cores.serie} strokeWidth={2} fill="url(#gradAbc)" dot={false} activeDot={{ r: 4 }} isAnimationActive={false} />
      </AreaChart>
    </ResponsiveContainer>
  );
}

// ------------------------------------------------------------ histórico de saldo (kardex)

export function GraficoSaldo({ extrato, minimo, maximo, pontoPedido }: { extrato: Movimentacao[]; minimo: number; maximo: number | null; pontoPedido: number }) {
  const cores = useCores();
  // saldo de fechamento de cada dia: lançamentos corrigidos no mesmo dia (ex.: estorno) não distorcem a escala
  const porDia = new Map<string, { t: number; saldo: number; data: string }>();
  extrato.forEach((m) => {
    const dia = m.data_movimentacao.slice(0, 10);
    const [a, mes, d] = dia.split('-').map(Number);
    porDia.set(dia, { t: new Date(a, mes - 1, d).getTime(), saldo: m.saldo_posterior ?? 0, data: dia });
  });
  const dados = [...porDia.values()];
  if (!dados.length) return null;
  const referencias = [
    ...(maximo != null ? [{ valor: maximo, cor: cores.superestoque, nome: 'Estoque máximo' }] : []),
    { valor: pontoPedido, cor: cores.repor, nome: 'Ponto de pedido' },
    { valor: minimo, cor: cores.ruptura, nome: 'Estoque mínimo' },
  ];
  return (
    <>
    <ResponsiveContainer width="100%" height={260}>
      <LineChart data={dados} margin={{ top: 10, right: 16, left: 0, bottom: 0 }}>
        <CartesianGrid vertical={false} stroke={cores.grade} />
        <XAxis
          dataKey="t" type="number" scale="time" domain={['dataMin', 'dataMax']} {...eixo} tick={{ fill: cores.texto }}
          tickFormatter={(t) => new Date(t).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })}
        />
        <YAxis {...eixo} tick={{ fill: cores.texto }} width={44} allowDecimals={false} />
        {referencias.map((r) => <ReferenceLine key={r.nome} y={r.valor} stroke={r.cor} strokeDasharray="4 4" />)}
        <Tooltip
          content={({ active, payload }) => {
            if (!active || !payload?.length) return null;
            const p = payload[0].payload as { saldo: number; data: string };
            return <CaixaTooltip titulo={data(p.data)} linhas={[['Saldo no fim do dia', numero(p.saldo)]]} />;
          }}
        />
        <Line type="stepAfter" dataKey="saldo" stroke={cores.serie} strokeWidth={2} dot={false} activeDot={{ r: 4 }} isAnimationActive={false} />
      </LineChart>
    </ResponsiveContainer>
    <div className="legenda" style={{ marginTop: 8 }}>
      <span className="legenda-item" style={{ cursor: 'default' }}><span className="legenda-cor" style={{ background: cores.serie, height: 3 }} />Saldo no fim do dia</span>
      {referencias.map((r) => (
        <span key={r.nome} className="legenda-item" style={{ cursor: 'default' }}>
          <span style={{ width: 14, borderTop: `2px dashed ${r.cor}` }} />{r.nome} <b>{numero(r.valor)}</b>
        </span>
      ))}
    </div>
    </>
  );
}
