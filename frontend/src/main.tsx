import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { HashRouter, Navigate, Route, Routes } from 'react-router-dom';
import './styles.css';
import { ProvedorSessao, useSessao } from './lib/sessao';
import { ProvedorNotificacoes } from './components/Notificacoes';
import Layout from './components/Layout';
import Login from './pages/Login';
import VisaoGeral from './pages/VisaoGeral';
import Produtos from './pages/Produtos';
import ProdutoDetalhe from './pages/ProdutoDetalhe';
import Movimentacoes from './pages/Movimentacoes';
import Pedidos from './pages/Pedidos';
import Relatorios from './pages/Relatorios';
import Cadastros from './pages/Cadastros';

function Aplicacao() {
  const { usuario } = useSessao();
  if (!usuario) return <Login />;
  return (
    // HashRouter: as rotas da interface (/#/produtos) não colidem com as rotas da API (/produtos)
    <HashRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<VisaoGeral />} />
          <Route path="produtos" element={<Produtos />} />
          <Route path="produtos/:id" element={<ProdutoDetalhe />} />
          <Route path="movimentacoes" element={<Movimentacoes />} />
          <Route path="pedidos" element={<Pedidos />} />
          <Route path="relatorios" element={<Relatorios />} />
          <Route path="cadastros" element={<Cadastros />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </HashRouter>
  );
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ProvedorSessao>
      <ProvedorNotificacoes>
        <Aplicacao />
      </ProvedorNotificacoes>
    </ProvedorSessao>
  </StrictMode>,
);
