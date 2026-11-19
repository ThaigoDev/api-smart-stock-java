import type { Situacao, StatusPedido, TipoMovimentacao } from './tipos';

const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const moedaCompacta = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL', notation: 'compact', maximumFractionDigits: 1 });
const inteiro = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 0 });
const decimal = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 1 });

export const reais = (v: number | null | undefined) => moeda.format(Number(v ?? 0));
export const reaisCompacto = (v: number | null | undefined) => moedaCompacta.format(Number(v ?? 0));
export const numero = (v: number | null | undefined) => (v == null ? '—' : inteiro.format(v));
export const decimal1 = (v: number | null | undefined) => (v == null ? '—' : decimal.format(v));
export const percentual = (v: number | null | undefined) => (v == null ? '—' : `${decimal.format(v)}%`);

export function dataHora(iso: string | null | undefined) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

export function data(iso: string | null | undefined) {
  if (!iso) return '—';
  const [a, m, d] = iso.slice(0, 10).split('-');
  return `${d}/${m}/${a}`;
}

const MESES = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];
export function mesCurto(anoMes: string) {
  const [a, m] = anoMes.split('-');
  return `${MESES[Number(m) - 1]}/${a.slice(2)}`;
}

/** Data local no formato AAAA-MM-DD (sem conversão para UTC). */
export function hojeISO() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

export function somarDias(iso: string, dias: number) {
  const [a, m, d] = iso.split('-').map(Number);
  const dt = new Date(a, m - 1, d + dias);
  return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`;
}

export const SITUACOES: Situacao[] = ['RUPTURA', 'CRITICO', 'REPOR', 'NORMAL', 'SUPERESTOQUE'];

export const NOME_SITUACAO: Record<Situacao, string> = {
  RUPTURA: 'Ruptura',
  CRITICO: 'Crítico',
  REPOR: 'Repor',
  NORMAL: 'Normal',
  SUPERESTOQUE: 'Superestoque',
};

export const DESCRICAO_SITUACAO: Record<Situacao, string> = {
  RUPTURA: 'Saldo zerado: vendas estão sendo perdidas',
  CRITICO: 'Abaixo do estoque mínimo (reserva de segurança)',
  REPOR: 'Atingiu o ponto de pedido: hora de comprar',
  NORMAL: 'Saldo adequado à demanda',
  SUPERESTOQUE: 'Acima do estoque máximo: capital imobilizado',
};

export const COR_SITUACAO: Record<Situacao, string> = {
  RUPTURA: 'var(--ruptura)',
  CRITICO: 'var(--critico)',
  REPOR: 'var(--repor)',
  NORMAL: 'var(--normal)',
  SUPERESTOQUE: 'var(--superestoque)',
};

export const NOME_TIPO: Record<TipoMovimentacao, string> = {
  ENTRADA: 'Entrada',
  SAIDA: 'Saída',
  DEVOLUCAO: 'Devolução',
  AJUSTE: 'Ajuste',
  ESTORNO: 'Estorno',
};

export const NOME_STATUS_PEDIDO: Record<StatusPedido, string> = {
  ABERTO: 'Aberto',
  RECEBIDO: 'Recebido',
  CANCELADO: 'Cancelado',
};

/** Efeito da movimentação no saldo, com sinal. */
export function efeito(tipo: TipoMovimentacao, quantidade: number) {
  return tipo === 'SAIDA' ? -quantidade : quantidade;
}
