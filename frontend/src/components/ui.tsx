import { useEffect, useState, type ReactNode } from 'react';
import { AlertTriangle, Inbox, Loader2, X } from 'lucide-react';
import { NOME_SITUACAO, NOME_STATUS_PEDIDO, NOME_TIPO } from '../lib/formato';
import type { Situacao, StatusPedido, TipoMovimentacao } from '../lib/tipos';

// ------------------------------------------------------------ selos

export function SeloSituacao({ situacao }: { situacao: Situacao | null }) {
  if (!situacao) return <span className="selo selo-neutro">Sem estoque</span>;
  return (
    <span className={`selo selo-${situacao}`}>
      <span className="ponto" />
      {NOME_SITUACAO[situacao]}
    </span>
  );
}

export function SeloTipo({ tipo }: { tipo: TipoMovimentacao }) {
  const classe = tipo === 'SAIDA' ? 'selo-neutro' : tipo === 'ESTORNO' ? 'selo-CANCELADO' : tipo === 'AJUSTE' ? 'selo-REPOR' : 'selo-azul';
  return <span className={`selo ${classe}`}>{NOME_TIPO[tipo]}</span>;
}

export function SeloStatusPedido({ status }: { status: StatusPedido }) {
  return <span className={`selo selo-${status}`}><span className="ponto" />{NOME_STATUS_PEDIDO[status]}</span>;
}

// ------------------------------------------------------------ estados

export function Carregando({ texto = 'Carregando…' }: { texto?: string }) {
  return (
    <div className="carregando-tela">
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <Loader2 size={18} className="giro" /> {texto}
      </div>
    </div>
  );
}

export function Vazio({ titulo, texto, icone }: { titulo: string; texto?: string; icone?: ReactNode }) {
  return (
    <div className="vazio">
      {icone ?? <Inbox size={36} />}
      <strong>{titulo}</strong>
      {texto && <span>{texto}</span>}
    </div>
  );
}

export function ErroCarregamento({ mensagem, tentarNovamente }: { mensagem: string; tentarNovamente: () => void }) {
  return (
    <div className="vazio">
      <AlertTriangle size={32} style={{ color: 'var(--ruptura)' }} />
      <strong>Não foi possível carregar os dados</strong>
      <span>{mensagem}</span>
      <button className="btn btn-pequeno" onClick={tentarNovamente}>Tentar novamente</button>
    </div>
  );
}

// ------------------------------------------------------------ modal e gaveta

interface PropsModal {
  titulo: string;
  descricao?: string;
  aoFechar: () => void;
  children: ReactNode;
  rodape?: ReactNode;
  largo?: boolean;
  gaveta?: boolean;
}

export function Modal({ titulo, descricao, aoFechar, children, rodape, largo, gaveta }: PropsModal) {
  useEffect(() => {
    const tecla = (e: KeyboardEvent) => e.key === 'Escape' && aoFechar();
    window.addEventListener('keydown', tecla);
    return () => window.removeEventListener('keydown', tecla);
  }, [aoFechar]);

  return (
    <div className={`sobreposicao ${gaveta ? 'gaveta-sobreposicao' : ''}`} onMouseDown={(e) => e.target === e.currentTarget && aoFechar()}>
      <div className={gaveta ? 'gaveta' : `modal ${largo ? 'modal-largo' : ''}`} role="dialog" aria-modal="true" aria-label={titulo}>
        <div className="modal-cabecalho">
          <div>
            <h2>{titulo}</h2>
            {descricao && <p>{descricao}</p>}
          </div>
          <button className="btn btn-fantasma btn-icone btn-pequeno" onClick={aoFechar} aria-label="Fechar">
            <X size={16} />
          </button>
        </div>
        <div className="modal-corpo" style={gaveta ? { flex: 1 } : undefined}>{children}</div>
        {rodape && <div className="modal-rodape">{rodape}</div>}
      </div>
    </div>
  );
}

interface PropsConfirmar {
  titulo: string;
  mensagem: ReactNode;
  textoConfirmar: string;
  perigo?: boolean;
  aoConfirmar: () => Promise<void> | void;
  aoFechar: () => void;
}

export function Confirmar({ titulo, mensagem, textoConfirmar, perigo, aoConfirmar, aoFechar }: PropsConfirmar) {
  const [executando, setExecutando] = useState(false);
  const confirmar = async () => {
    setExecutando(true);
    try {
      await aoConfirmar();
      aoFechar();
    } finally {
      setExecutando(false);
    }
  };
  return (
    <Modal
      titulo={titulo}
      aoFechar={aoFechar}
      rodape={
        <>
          <button className="btn" onClick={aoFechar} disabled={executando}>Voltar</button>
          <button className={`btn ${perigo ? 'btn-perigo-solido' : 'btn-primario'}`} onClick={confirmar} disabled={executando}>
            {executando && <Loader2 size={15} className="giro" />} {textoConfirmar}
          </button>
        </>
      }
    >
      <div style={{ color: 'var(--text-2)' }}>{mensagem}</div>
    </Modal>
  );
}

// ------------------------------------------------------------ indicadores

export function Kpi({ rotulo, valor, detalhe, icone, cor }: { rotulo: string; valor: ReactNode; detalhe?: ReactNode; icone: ReactNode; cor?: string }) {
  return (
    <div className="card kpi">
      <div className="kpi-topo">
        <span>{rotulo}</span>
        <span className="kpi-icone" style={cor ? { color: cor, background: `color-mix(in srgb, ${cor} 14%, transparent)` } : undefined}>{icone}</span>
      </div>
      <div className="kpi-valor num">{valor}</div>
      {detalhe && <div className="kpi-detalhe">{detalhe}</div>}
    </div>
  );
}

export function Campo({ rotulo, ajuda, erro, children, inteira }: { rotulo: string; ajuda?: string; erro?: string; children: ReactNode; inteira?: boolean }) {
  return (
    <div className={`campo ${inteira ? 'inteira' : ''}`}>
      <label>{rotulo}</label>
      {children}
      {erro ? <span className="erro">{erro}</span> : ajuda && <span className="ajuda">{ajuda}</span>}
    </div>
  );
}
