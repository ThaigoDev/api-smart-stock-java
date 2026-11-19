// Cliente HTTP da API: injeta o token JWT e traduz as respostas de erro em mensagens legíveis.

const CHAVE_TOKEN = 'smartstock.token';

export class ErroApi extends Error {
  constructor(public status: number, mensagem: string) {
    super(mensagem);
  }
}

export function lerToken(): string | null {
  try {
    return sessionStorage.getItem(CHAVE_TOKEN);
  } catch {
    return null;
  }
}

export function gravarToken(token: string | null) {
  try {
    if (token) sessionStorage.setItem(CHAVE_TOKEN, token);
    else sessionStorage.removeItem(CHAVE_TOKEN);
  } catch {
    /* armazenamento indisponível: a sessão dura até recarregar a página */
  }
}

let aoExpirar: () => void = () => {};
export function definirAcaoSessaoExpirada(acao: () => void) {
  aoExpirar = acao;
}

const MENSAGENS_PADRAO: Record<number, string> = {
  401: 'Sua sessão expirou. Entre novamente.',
  403: 'Seu perfil não tem permissão para esta operação.',
  404: 'Registro não encontrado.',
  409: 'A operação conflita com os dados existentes.',
  500: 'Erro inesperado no servidor.',
};

async function extrairMensagem(resposta: Response): Promise<string> {
  const corpo = await resposta.json().catch(() => null);
  if (Array.isArray(corpo) && corpo.length) {
    return corpo.map((e: { message?: string }) => e.message).filter(Boolean).join(' ');
  }
  if (corpo && typeof corpo === 'object' && 'message' in corpo && corpo.message) {
    return String(corpo.message);
  }
  return MENSAGENS_PADRAO[resposta.status] ?? `Erro ${resposta.status}`;
}

async function requisitar<T>(metodo: string, caminho: string, corpo?: unknown): Promise<T> {
  const token = lerToken();
  const resposta = await fetch(caminho, {
    method: metodo,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: corpo === undefined ? undefined : JSON.stringify(corpo),
  });
  if (resposta.status === 401 && caminho !== '/auth') {
    aoExpirar();
  }
  if (!resposta.ok) {
    throw new ErroApi(resposta.status, await extrairMensagem(resposta));
  }
  if (resposta.status === 204) return undefined as T;
  const texto = await resposta.text();
  return (texto ? JSON.parse(texto) : undefined) as T;
}

export const api = {
  get: <T>(caminho: string) => requisitar<T>('GET', caminho),
  post: <T>(caminho: string, corpo?: unknown) => requisitar<T>('POST', caminho, corpo ?? {}),
  put: <T>(caminho: string, corpo: unknown) => requisitar<T>('PUT', caminho, corpo),
  patch: <T>(caminho: string, corpo: unknown) => requisitar<T>('PATCH', caminho, corpo),
  delete: <T>(caminho: string) => requisitar<T>('DELETE', caminho),
};

/** Monta uma query string ignorando valores vazios. */
export function consulta(parametros: Record<string, string | number | boolean | null | undefined>): string {
  const p = new URLSearchParams();
  Object.entries(parametros).forEach(([chave, valor]) => {
    if (valor !== undefined && valor !== null && valor !== '') p.set(chave, String(valor));
  });
  const texto = p.toString();
  return texto ? `?${texto}` : '';
}
