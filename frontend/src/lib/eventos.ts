// Avisos entre telas independentes (ex.: o contador de pedidos abertos no menu lateral)
const PEDIDOS_ALTERADOS = 'smartstock:pedidos-alterados';

export function avisarPedidosAlterados() {
  window.dispatchEvent(new Event(PEDIDOS_ALTERADOS));
}

export function aoAlterarPedidos(acao: () => void) {
  window.addEventListener(PEDIDOS_ALTERADOS, acao);
  return () => window.removeEventListener(PEDIDOS_ALTERADOS, acao);
}
