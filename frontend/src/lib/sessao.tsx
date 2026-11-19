import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { api, definirAcaoSessaoExpirada, gravarToken, lerToken } from './api';
import { hojeISO } from './formato';

interface Usuario {
  email: string;
  nome: string;
  admin: boolean;
}

interface Sessao {
  usuario: Usuario | null;
  entrar: (email: string, senha: string) => Promise<void>;
  sair: () => void;
  dataReferencia: string;
  setDataReferencia: (d: string) => void;
  tema: 'claro' | 'escuro';
  alternarTema: () => void;
}

const ContextoSessao = createContext<Sessao | null>(null);

/** Lê as informações do usuário do próprio token (a assinatura é validada pela API a cada requisição). */
function usuarioDoToken(token: string | null): Usuario | null {
  if (!token) return null;
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64).split('').map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0')).join(''),
    );
    const claims = JSON.parse(json);
    if (claims.exp && claims.exp * 1000 < Date.now()) return null;
    const escopos = String(claims.scope ?? '').split(' ');
    return {
      email: claims.email ?? '',
      nome: claims.nome ?? claims.email ?? 'Usuário',
      admin: escopos.includes('admin'),
    };
  } catch {
    return null;
  }
}

function temaInicial(): 'claro' | 'escuro' {
  try {
    const salvo = localStorage.getItem('smartstock.tema');
    if (salvo === 'claro' || salvo === 'escuro') return salvo;
  } catch {
    /* ignora */
  }
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'escuro' : 'claro';
}

export function ProvedorSessao({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(() => usuarioDoToken(lerToken()));
  // a data de referência sobrevive a um recarregamento da página (útil para analisar um período passado)
  const [dataReferencia, setDataReferenciaEstado] = useState(() => {
    try {
      return sessionStorage.getItem('smartstock.dataReferencia') || hojeISO();
    } catch {
      return hojeISO();
    }
  });
  const setDataReferencia = useCallback((d: string) => {
    setDataReferenciaEstado(d);
    try {
      sessionStorage.setItem('smartstock.dataReferencia', d);
    } catch {
      /* ignora */
    }
  }, []);
  const [tema, setTema] = useState<'claro' | 'escuro'>(temaInicial);

  const sair = useCallback(() => {
    gravarToken(null);
    setUsuario(null);
  }, []);

  useEffect(() => definirAcaoSessaoExpirada(sair), [sair]);

  useEffect(() => {
    document.documentElement.dataset.theme = tema === 'escuro' ? 'dark' : 'light';
    try {
      localStorage.setItem('smartstock.tema', tema);
    } catch {
      /* ignora */
    }
  }, [tema]);

  const entrar = useCallback(async (email: string, senha: string) => {
    const resposta = await api.post<{ acessToken: string }>('/auth', { email, senha });
    gravarToken(resposta.acessToken);
    setUsuario(usuarioDoToken(resposta.acessToken));
  }, []);

  const valor = useMemo<Sessao>(() => ({
    usuario, entrar, sair, dataReferencia, setDataReferencia, tema,
    alternarTema: () => setTema((t) => (t === 'claro' ? 'escuro' : 'claro')),
  }), [usuario, entrar, sair, dataReferencia, setDataReferencia, tema]);

  return <ContextoSessao.Provider value={valor}>{children}</ContextoSessao.Provider>;
}

export function useSessao() {
  const contexto = useContext(ContextoSessao);
  if (!contexto) throw new Error('useSessao fora do ProvedorSessao');
  return contexto;
}
