import { useMemo, useState, type FormEvent } from 'react';
import { ArrowRight, Loader2, ShieldAlert } from 'lucide-react';
import { api } from '../lib/api';
import { NOME_TIPO, efeito, numero } from '../lib/formato';
import type { Movimentacao, PosicaoProduto, TipoMovimentacao } from '../lib/tipos';
import { Campo, Modal, SeloSituacao } from './ui';
import { useNotificacoes } from './Notificacoes';

interface Props {
  produtos: PosicaoProduto[];
  produtoInicial?: string;
  aoFechar: () => void;
  aoSalvar: () => void;
}

const TIPOS: { tipo: Exclude<TipoMovimentacao, 'ESTORNO'>; motivo: string }[] = [
  { tipo: 'SAIDA', motivo: 'Venda ao consumidor' },
  { tipo: 'ENTRADA', motivo: 'Recebimento de mercadoria' },
  { tipo: 'DEVOLUCAO', motivo: 'Devolução de cliente' },
  { tipo: 'AJUSTE', motivo: 'Ajuste de inventário' },
];

export default function FormMovimentacao({ produtos, produtoInicial, aoFechar, aoSalvar }: Props) {
  const notificar = useNotificacoes();
  const disponiveis = useMemo(() => produtos.filter((p) => p.ativo && p.possui_estoque), [produtos]);
  const [produtoId, setProdutoId] = useState(produtoInicial ?? '');
  const [tipo, setTipo] = useState<Exclude<TipoMovimentacao, 'ESTORNO'>>('SAIDA');
  const [quantidade, setQuantidade] = useState('1');
  const [motivo, setMotivo] = useState(TIPOS[0].motivo);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const produto = disponiveis.find((p) => p.produto_id === produtoId);
  const qtd = Number(quantidade || 0);
  const saldoPrevisto = produto ? produto.saldo + efeito(tipo, qtd) : null;
  const ficaNegativo = saldoPrevisto != null && saldoPrevisto < 0;

  const escolherTipo = (t: (typeof TIPOS)[number]) => {
    setTipo(t.tipo);
    setMotivo(t.motivo);
  };

  const salvar = async (e: FormEvent) => {
    e.preventDefault();
    setErro(null);
    setSalvando(true);
    try {
      const r = await api.post<Movimentacao>('/movimentacoesestoque', { produto_id: produtoId, tipo, quantidade: qtd, motivo });
      notificar.sucesso(`${NOME_TIPO[tipo]} registrada`, `${produto?.nome}: saldo ${r.saldo_anterior} → ${r.saldo_posterior}`);
      aoSalvar();
      aoFechar();
    } catch (err) {
      // a regra de negócio é aplicada pela API (422); a mensagem dela é exibida como veio
      setErro((err as Error).message);
    } finally {
      setSalvando(false);
    }
  };

  return (
    <Modal
      titulo="Registrar movimentação"
      descricao="Toda movimentação fica no histórico do produto. Para corrigir um lançamento, use o estorno."
      aoFechar={aoFechar}
      rodape={
        <>
          <button className="btn" type="button" onClick={aoFechar}>Cancelar</button>
          <button className="btn btn-primario" type="submit" form="form-mov" disabled={salvando || !produtoId}>
            {salvando && <Loader2 size={15} className="giro" />} Registrar
          </button>
        </>
      }
    >
      <form id="form-mov" className="form-grade" onSubmit={salvar}>
        {erro && <div className="aviso aviso-perigo inteira"><ShieldAlert size={17} /><div><strong>Movimentação recusada.</strong> {erro}</div></div>}
        <Campo rotulo="Produto" inteira>
          <select className="select" required value={produtoId} onChange={(e) => setProdutoId(e.target.value)}>
            <option value="">Selecione o produto…</option>
            {disponiveis.map((p) => <option key={p.produto_id} value={p.produto_id}>{p.nome} · {p.sku} (saldo {p.saldo})</option>)}
          </select>
        </Campo>
        <Campo rotulo="Tipo" inteira>
          <div className="segmentado">
            {TIPOS.map((t) => (
              <button type="button" key={t.tipo} className={tipo === t.tipo ? 'ativo' : ''} onClick={() => escolherTipo(t)}>
                {NOME_TIPO[t.tipo]}
              </button>
            ))}
          </div>
        </Campo>
        <Campo rotulo="Quantidade" ajuda={tipo === 'AJUSTE' ? 'Negativo para perdas, positivo para sobras' : undefined}>
          <input className="input" type="number" required min={tipo === 'AJUSTE' ? undefined : 1} value={quantidade} onChange={(e) => setQuantidade(e.target.value)} />
        </Campo>
        <Campo rotulo="Motivo">
          <input className="input" required maxLength={255} value={motivo} onChange={(e) => setMotivo(e.target.value)} />
        </Campo>
        {produto && (
          <div className={`aviso inteira ${ficaNegativo ? 'aviso-perigo' : ''}`} style={{ alignItems: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
              <span>Saldo</span>
              <strong className="num">{numero(produto.saldo)}</strong>
              <ArrowRight size={15} />
              <strong className="num">{numero(saldoPrevisto)}</strong>
              <SeloSituacao situacao={produto.situacao} />
              {ficaNegativo && <span>A API vai recusar: o estoque não pode ficar negativo.</span>}
            </div>
          </div>
        )}
      </form>
    </Modal>
  );
}
