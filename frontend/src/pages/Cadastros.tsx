import { useState, type FormEvent } from 'react';
import { Loader2, Pencil, Plus, Trash2 } from 'lucide-react';
import { api } from '../lib/api';
import { useCarregar } from '../lib/hooks';
import { useSessao } from '../lib/sessao';
import type { Categoria, Fornecedor } from '../lib/tipos';
import { Campo, Carregando, Confirmar, ErroCarregamento, Modal, Vazio } from '../components/ui';
import { useNotificacoes } from '../components/Notificacoes';

type Aba = 'categorias' | 'fornecedores';

export default function Cadastros() {
  const [aba, setAba] = useState<Aba>('categorias');
  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <h1>Cadastros</h1>
          <p>Categorias e fornecedores usados pelos produtos</p>
        </div>
      </div>
      <section className="card">
        <div className="abas">
          <button className={aba === 'categorias' ? 'ativo' : ''} onClick={() => setAba('categorias')}>Categorias</button>
          <button className={aba === 'fornecedores' ? 'ativo' : ''} onClick={() => setAba('fornecedores')}>Fornecedores</button>
        </div>
        {aba === 'categorias' ? <Categorias /> : <Fornecedores />}
      </section>
    </>
  );
}

function Categorias() {
  const { usuario } = useSessao();
  const notificar = useNotificacoes();
  const { dados, erro, recarregar } = useCarregar(() => api.get<Categoria[]>('/categorias'), []);
  const [editando, setEditando] = useState<Categoria | null | undefined>(undefined);
  const [removendo, setRemovendo] = useState<Categoria | null>(null);
  const [form, setForm] = useState({ nome: '', descricao: '' });
  const [salvando, setSalvando] = useState(false);
  const [erroForm, setErroForm] = useState<string | null>(null);

  const abrir = (c: Categoria | null) => {
    setEditando(c);
    setErroForm(null);
    setForm({ nome: c?.nome ?? '', descricao: c?.descricao ?? '' });
  };
  const salvar = async (e: FormEvent) => {
    e.preventDefault();
    setSalvando(true);
    try {
      if (editando) await api.put(`/categorias/${editando.id}`, form);
      else await api.post('/categorias', form);
      notificar.sucesso(editando ? 'Categoria atualizada' : 'Categoria criada', form.nome);
      setEditando(undefined);
      recarregar();
    } catch (err) {
      setErroForm((err as Error).message);
    } finally {
      setSalvando(false);
    }
  };
  const remover = async () => {
    if (!removendo) return;
    try {
      await api.delete(`/categorias/${removendo.id}`);
      notificar.sucesso('Categoria excluída', removendo.nome);
      recarregar();
    } catch (err) {
      notificar.erro('Não foi possível excluir', (err as Error).message);
    }
  };

  if (erro) return <ErroCarregamento mensagem={erro} tentarNovamente={recarregar} />;
  if (!dados) return <Carregando />;
  const lista = [...dados].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
  return (
    <>
      <div className="barra-ferramentas">
        <span className="muted">{lista.length} categorias</span><span className="espaco" />
        {usuario?.admin && <button className="btn btn-primario btn-pequeno" onClick={() => abrir(null)}><Plus size={14} /> Nova categoria</button>}
      </div>
      {lista.length === 0 ? <Vazio titulo="Nenhuma categoria cadastrada" /> : (
        <table className="tabela">
          <thead><tr><th>Nome</th><th>Descrição</th><th className="direita">Ações</th></tr></thead>
          <tbody>
            {lista.map((c) => (
              <tr key={c.id}>
                <td><strong>{c.nome}</strong></td>
                <td className="muted">{c.descricao || '—'}</td>
                <td className="direita nowrap">
                  {usuario?.admin && (
                    <>
                      <button className="btn btn-fantasma btn-icone btn-pequeno" title="Editar" onClick={() => abrir(c)}><Pencil size={15} /></button>
                      <button className="btn btn-fantasma btn-icone btn-pequeno btn-perigo" title="Excluir" onClick={() => setRemovendo(c)}><Trash2 size={15} /></button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {editando !== undefined && (
        <Modal titulo={editando ? 'Editar categoria' : 'Nova categoria'} aoFechar={() => setEditando(undefined)}
               rodape={<><button className="btn" onClick={() => setEditando(undefined)}>Cancelar</button><button className="btn btn-primario" type="submit" form="form-categoria" disabled={salvando}>{salvando && <Loader2 size={15} className="giro" />} Salvar</button></>}>
          <form id="form-categoria" onSubmit={salvar} style={{ display: 'grid', gap: 14 }}>
            {erroForm && <div className="aviso aviso-perigo">{erroForm}</div>}
            <Campo rotulo="Nome" ajuda="De 3 a 20 caracteres"><input className="input" required minLength={3} maxLength={20} autoFocus value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} /></Campo>
            <Campo rotulo="Descrição"><input className="input" value={form.descricao} onChange={(e) => setForm({ ...form, descricao: e.target.value })} /></Campo>
          </form>
        </Modal>
      )}
      {removendo && (
        <Confirmar titulo="Excluir categoria" perigo textoConfirmar="Excluir"
                   mensagem={<>Excluir a categoria <strong>{removendo.nome}</strong>? Categorias com produtos vinculados não podem ser excluídas.</>}
                   aoConfirmar={remover} aoFechar={() => setRemovendo(null)} />
      )}
    </>
  );
}

function Fornecedores() {
  const { usuario } = useSessao();
  const notificar = useNotificacoes();
  const { dados, erro, recarregar } = useCarregar(() => api.get<Fornecedor[]>('/fornecedores'), []);
  const [editando, setEditando] = useState<Fornecedor | null | undefined>(undefined);
  const [removendo, setRemovendo] = useState<Fornecedor | null>(null);
  const [form, setForm] = useState({ nome: '', email: '', telefone: '' });
  const [salvando, setSalvando] = useState(false);
  const [erroForm, setErroForm] = useState<string | null>(null);

  const abrir = (f: Fornecedor | null) => {
    setEditando(f);
    setErroForm(null);
    setForm({ nome: f?.nome ?? '', email: f?.email ?? '', telefone: f?.telefone ?? '' });
  };
  const salvar = async (e: FormEvent) => {
    e.preventDefault();
    setSalvando(true);
    try {
      if (editando) await api.put(`/fornecedores/${editando.id}`, form);
      else await api.post('/fornecedores', form);
      notificar.sucesso(editando ? 'Fornecedor atualizado' : 'Fornecedor criado', form.nome);
      setEditando(undefined);
      recarregar();
    } catch (err) {
      setErroForm((err as Error).message);
    } finally {
      setSalvando(false);
    }
  };
  const remover = async () => {
    if (!removendo) return;
    try {
      await api.delete(`/fornecedores/${removendo.id}`);
      notificar.sucesso('Fornecedor excluído', removendo.nome);
      recarregar();
    } catch (err) {
      notificar.erro('Não foi possível excluir', (err as Error).message);
    }
  };

  if (erro) return <ErroCarregamento mensagem={erro} tentarNovamente={recarregar} />;
  if (!dados) return <Carregando />;
  const lista = [...dados].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
  return (
    <>
      <div className="barra-ferramentas">
        <span className="muted">{lista.length} fornecedores</span><span className="espaco" />
        {usuario?.admin && <button className="btn btn-primario btn-pequeno" onClick={() => abrir(null)}><Plus size={14} /> Novo fornecedor</button>}
      </div>
      {lista.length === 0 ? <Vazio titulo="Nenhum fornecedor cadastrado" /> : (
        <table className="tabela">
          <thead><tr><th>Nome</th><th>E-mail</th><th>Telefone</th><th className="direita">Ações</th></tr></thead>
          <tbody>
            {lista.map((f) => (
              <tr key={f.id}>
                <td><strong>{f.nome}</strong></td>
                <td><a href={`mailto:${f.email}`}>{f.email}</a></td>
                <td className="num">{f.telefone}</td>
                <td className="direita nowrap">
                  {usuario?.admin && (
                    <>
                      <button className="btn btn-fantasma btn-icone btn-pequeno" title="Editar" onClick={() => abrir(f)}><Pencil size={15} /></button>
                      <button className="btn btn-fantasma btn-icone btn-pequeno btn-perigo" title="Excluir" onClick={() => setRemovendo(f)}><Trash2 size={15} /></button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {editando !== undefined && (
        <Modal titulo={editando ? 'Editar fornecedor' : 'Novo fornecedor'} aoFechar={() => setEditando(undefined)}
               rodape={<><button className="btn" onClick={() => setEditando(undefined)}>Cancelar</button><button className="btn btn-primario" type="submit" form="form-fornecedor" disabled={salvando}>{salvando && <Loader2 size={15} className="giro" />} Salvar</button></>}>
          <form id="form-fornecedor" onSubmit={salvar} style={{ display: 'grid', gap: 14 }}>
            {erroForm && <div className="aviso aviso-perigo">{erroForm}</div>}
            <Campo rotulo="Nome"><input className="input" required autoFocus value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} /></Campo>
            <Campo rotulo="E-mail"><input className="input" type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></Campo>
            <Campo rotulo="Telefone" ajuda="Com DDD, ao menos 11 dígitos"><input className="input" required minLength={11} value={form.telefone} onChange={(e) => setForm({ ...form, telefone: e.target.value })} /></Campo>
          </form>
        </Modal>
      )}
      {removendo && (
        <Confirmar titulo="Excluir fornecedor" perigo textoConfirmar="Excluir"
                   mensagem={<>Excluir o fornecedor <strong>{removendo.nome}</strong>? Fornecedores com produtos ou pedidos vinculados não podem ser excluídos.</>}
                   aoConfirmar={remover} aoFechar={() => setRemovendo(null)} />
      )}
    </>
  );
}
