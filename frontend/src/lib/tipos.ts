// Contratos da API Smart Stock (espelham os DTOs do back-end)

export type Situacao = 'RUPTURA' | 'CRITICO' | 'REPOR' | 'NORMAL' | 'SUPERESTOQUE';
export type TipoMovimentacao = 'ENTRADA' | 'SAIDA' | 'DEVOLUCAO' | 'AJUSTE' | 'ESTORNO';
export type StatusPedido = 'ABERTO' | 'RECEBIDO' | 'CANCELADO';

export interface Categoria {
  id: string;
  nome: string;
  descricao: string | null;
}

export interface Fornecedor {
  id: string;
  nome: string;
  email: string;
  telefone: string;
}

export interface Produto {
  id: string;
  nome: string;
  sku: string;
  estoque_minimo: number;
  categoria_id: string;
  preco: number; // centavos
  fornecedor_id: string;
  ativo: boolean;
  estoque_maximo: number | null;
  prazo_reposicao_dias: number | null;
}

export interface PosicaoProduto {
  produto_id: string;
  nome: string;
  sku: string;
  categoria_id: string;
  categoria: string;
  fornecedor_id: string;
  fornecedor: string;
  preco: number; // reais
  ativo: boolean;
  possui_estoque: boolean;
  saldo: number;
  estoque_minimo: number;
  estoque_maximo: number | null;
  prazo_reposicao_dias: number;
  ponto_pedido: number;
  demanda_media_diaria: number;
  cobertura_dias: number | null;
  situacao: Situacao | null;
  sugestao_compra: number;
  valor_em_estoque: number;
}

export interface Movimentacao {
  id: string;
  produto_id: string;
  tipo: TipoMovimentacao;
  quantidade: number;
  motivo: string;
  data_movimentacao: string;
  saldo_anterior: number | null;
  saldo_posterior: number | null;
  estornada: boolean;
  movimentacao_origem_id: string | null;
  registrado_por: string | null;
  situacao_estoque: Situacao | null;
}

export interface Pagina<T> {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  total_elementos: number;
  total_paginas: number;
}

export interface ItemCurvaABC {
  produto_id: string;
  nome: string;
  quantidade_vendida: number;
  valor_vendido: number;
  percentual_valor: number;
  percentual_acumulado: number;
  classe: 'A' | 'B' | 'C';
}

export interface CurvaABC {
  inicio: string;
  fim: string;
  valor_total_vendido: number;
  resumo_por_classe: {
    classe: 'A' | 'B' | 'C';
    quantidade_produtos: number;
    percentual_produtos: number;
    valor_vendido: number;
    percentual_valor: number;
  }[];
  itens: ItemCurvaABC[];
}

export interface Painel {
  data_referencia: string;
  produtos_ativos: number;
  unidades_em_estoque: number;
  valor_total_em_estoque: number;
  produtos_por_situacao: Record<Situacao, number>;
  valor_em_superestoque: number;
  movimentacoes_ultimos_30_dias: number;
  faturamento_ultimos_30_dias: number;
  mais_vendidos_ultimos_30_dias: ItemCurvaABC[];
}

export interface ResumoMensal {
  mes: string;
  quantidade_movimentacoes: number;
  unidades_entrada: number;
  unidades_saida: number;
  unidades_devolvidas: number;
  saldo_ajustes: number;
  quantidade_estornos: number;
  faturamento: number;
}

export interface GiroProduto {
  produto_id: string;
  nome: string;
  categoria: string;
  quantidade_vendida: number;
  estoque_medio: number;
  giro: number | null;
  dias_medios_em_estoque: number | null;
  saldo_atual: number;
  cobertura_dias: number | null;
}

export interface Giro {
  inicio: string;
  fim: string;
  dias: number;
  giro_medio: number | null;
  itens: GiroProduto[];
}

export interface Conciliacao {
  produtos_verificados: number;
  produtos_consistentes: number;
  produtos_divergentes: number;
  movimentacoes_verificadas: number;
  quebras_de_encadeamento: number;
  produtos_com_saldo_negativo: number;
  consistente: boolean;
  divergencias: { produto_id: string; nome: string; saldo_registrado: number; saldo_recalculado: number; quebras_de_encadeamento: number }[];
}

export interface ItemPedido {
  produto_id: string;
  produto: string;
  sku: string;
  quantidade: number;
  valor_estimado: number;
}

export interface PedidoCompra {
  id: string;
  codigo: string;
  fornecedor_id: string;
  fornecedor: string;
  status: StatusPedido;
  observacao: string | null;
  criado_por: string | null;
  criado_em: string;
  recebido_por: string | null;
  recebido_em: string | null;
  cancelado_em: string | null;
  itens: ItemPedido[];
  quantidade_total: number;
  valor_estimado: number;
}
