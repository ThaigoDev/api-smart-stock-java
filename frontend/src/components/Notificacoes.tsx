import { createContext, useCallback, useContext, useState, type ReactNode } from 'react';
import { AlertCircle, CheckCircle2, X } from 'lucide-react';

interface Notificacao {
  id: number;
  tipo: 'sucesso' | 'erro';
  titulo: string;
  mensagem?: string;
}

interface ApiNotificacoes {
  sucesso: (titulo: string, mensagem?: string) => void;
  erro: (titulo: string, mensagem?: string) => void;
}

const Contexto = createContext<ApiNotificacoes | null>(null);
let sequencia = 0;

export function ProvedorNotificacoes({ children }: { children: ReactNode }) {
  const [lista, setLista] = useState<Notificacao[]>([]);

  const remover = useCallback((id: number) => setLista((l) => l.filter((n) => n.id !== id)), []);
  const adicionar = useCallback((tipo: Notificacao['tipo'], titulo: string, mensagem?: string) => {
    const id = ++sequencia;
    setLista((l) => [...l.slice(-3), { id, tipo, titulo, mensagem }]);
    setTimeout(() => remover(id), tipo === 'erro' ? 7000 : 4000);
  }, [remover]);

  const api: ApiNotificacoes = {
    sucesso: (t, m) => adicionar('sucesso', t, m),
    erro: (t, m) => adicionar('erro', t, m),
  };

  return (
    <Contexto.Provider value={api}>
      {children}
      <div className="toasts" role="status" aria-live="polite">
        {lista.map((n) => (
          <div key={n.id} className={`toast toast-${n.tipo}`}>
            {n.tipo === 'sucesso' ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
            <div style={{ flex: 1 }}>
              <strong>{n.titulo}</strong>
              {n.mensagem && <span className="muted">{n.mensagem}</span>}
            </div>
            <button className="btn btn-fantasma btn-icone btn-pequeno" onClick={() => remover(n.id)} aria-label="Fechar">
              <X size={14} />
            </button>
          </div>
        ))}
      </div>
    </Contexto.Provider>
  );
}

export function useNotificacoes() {
  const c = useContext(Contexto);
  if (!c) throw new Error('useNotificacoes fora do provedor');
  return c;
}
