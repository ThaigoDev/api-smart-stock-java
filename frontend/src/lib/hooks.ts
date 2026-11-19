import { useCallback, useEffect, useRef, useState } from 'react';

interface Estado<T> {
  dados: T | null;
  carregando: boolean;
  erro: string | null;
  recarregar: () => void;
}

/** Busca dados de forma declarativa; refaz a busca quando as dependências mudam. */
export function useCarregar<T>(buscar: () => Promise<T>, dependencias: unknown[]): Estado<T> {
  const [dados, setDados] = useState<T | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [versao, setVersao] = useState(0);
  const buscarRef = useRef(buscar);
  buscarRef.current = buscar;

  useEffect(() => {
    let ativo = true;
    setCarregando(true);
    setErro(null);
    buscarRef.current()
      .then((resultado) => ativo && setDados(resultado))
      .catch((e: Error) => ativo && setErro(e.message))
      .finally(() => ativo && setCarregando(false));
    return () => {
      ativo = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...dependencias, versao]);

  const recarregar = useCallback(() => setVersao((v) => v + 1), []);
  return { dados, carregando, erro, recarregar };
}
