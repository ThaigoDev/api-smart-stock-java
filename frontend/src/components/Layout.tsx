import { useEffect, useState } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import {
  ArrowLeftRight, BarChart3, BookOpen, CalendarDays, FolderCog, LayoutDashboard, LogOut, Menu, Moon, Package,
  ShoppingCart, Sun,
} from 'lucide-react';
import { useSessao } from '../lib/sessao';
import { useCarregar } from '../lib/hooks';
import { api } from '../lib/api';
import type { PedidoCompra } from '../lib/tipos';
import { aoAlterarPedidos } from '../lib/eventos';

const ITENS = [
  { para: '/', rotulo: 'Visão geral', icone: LayoutDashboard },
  { para: '/produtos', rotulo: 'Produtos', icone: Package },
  { para: '/movimentacoes', rotulo: 'Movimentações', icone: ArrowLeftRight },
  { para: '/pedidos', rotulo: 'Pedidos de compra', icone: ShoppingCart },
  { para: '/relatorios', rotulo: 'Relatórios', icone: BarChart3 },
  { para: '/cadastros', rotulo: 'Cadastros', icone: FolderCog },
];

const TITULOS: Record<string, string> = {
  '': 'Visão geral',
  produtos: 'Produtos',
  movimentacoes: 'Movimentações',
  pedidos: 'Pedidos de compra',
  relatorios: 'Relatórios',
  cadastros: 'Cadastros',
};

function iniciais(nome: string) {
  return nome.split(/[\s@.]+/).filter(Boolean).slice(0, 2).map((p) => p[0]?.toUpperCase()).join('');
}

export default function Layout() {
  const { usuario, sair, dataReferencia, setDataReferencia, tema, alternarTema } = useSessao();
  const [menuAberto, setMenuAberto] = useState(false);
  const local = useLocation();
  const secao = local.pathname.split('/')[1] ?? '';
  const pedidosAbertos = useCarregar(() => api.get<PedidoCompra[]>('/pedidos-compra?status=ABERTO'), [local.pathname]);
  const recarregarPedidos = pedidosAbertos.recarregar;
  useEffect(() => aoAlterarPedidos(recarregarPedidos), [recarregarPedidos]);

  return (
    <div className="app">
      <aside className={`sidebar ${menuAberto ? 'aberta' : ''}`}>
        <div className="brand">
          <div className="brand-logo"><Package size={19} color="#fff" /></div>
          <div>
            <div className="brand-name">Smart Stock</div>
            <div className="brand-sub">Gestão de estoque</div>
          </div>
        </div>
        <nav className="nav" onClick={() => setMenuAberto(false)}>
          <div className="nav-section">Operação</div>
          {ITENS.slice(0, 4).map(({ para, rotulo, icone: Icone }) => (
            <NavLink key={para} to={para} end={para === '/'}>
              <Icone size={18} /> {rotulo}
              {para === '/pedidos' && (pedidosAbertos.dados?.length ?? 0) > 0 && (
                <span className="contador">{pedidosAbertos.dados?.length}</span>
              )}
            </NavLink>
          ))}
          <div className="nav-section">Gestão</div>
          {ITENS.slice(4).map(({ para, rotulo, icone: Icone }) => (
            <NavLink key={para} to={para}>
              <Icone size={18} /> {rotulo}
            </NavLink>
          ))}
          <a href="/swagger-ui.html" target="_blank" rel="noreferrer">
            <BookOpen size={18} /> Documentação da API
          </a>
        </nav>
        <div className="sidebar-rodape">Smart Stock · TCC 2026</div>
      </aside>

      <div className="main">
        <header className="topbar">
          <button className="btn btn-fantasma btn-icone menu-mobile" onClick={() => setMenuAberto((v) => !v)} aria-label="Menu">
            <Menu size={18} />
          </button>
          <div className="topbar-titulo">{TITULOS[secao] ?? 'Smart Stock'}</div>
          <div className="topbar-acoes">
            <label className="input-com-icone seletor-data" title="Data de referência dos indicadores">
              <CalendarDays size={16} />
              <input
                className="input"
                type="date"
                value={dataReferencia}
                onChange={(e) => e.target.value && setDataReferencia(e.target.value)}
                aria-label="Data de referência"
              />
            </label>
            <button className="btn btn-fantasma btn-icone" onClick={alternarTema} aria-label="Alternar tema" title="Alternar tema">
              {tema === 'escuro' ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <div className="usuario">
              <div className="avatar">{iniciais(usuario?.nome ?? '?')}</div>
              <div className="usuario-texto">
                <div className="usuario-nome">{usuario?.nome}</div>
                <div className="usuario-papel">{usuario?.admin ? 'Administrador' : 'Operador'}</div>
              </div>
              <button className="btn btn-fantasma btn-icone" onClick={sair} aria-label="Sair" title="Sair">
                <LogOut size={17} />
              </button>
            </div>
          </div>
        </header>
        <main className="conteudo">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
