import { useEffect, useState, type FormEvent } from 'react';
import { Loader2 } from 'lucide-react';
import { api } from '../lib/api';
import type { Categoria, Fornecedor, Produto } from '../lib/tipos';
import { Campo, Modal } from './ui';
import { useNotificacoes } from './Notificacoes';

interface Props {
  produtoId?: string; // ausente = cadastro novo
  aoFechar: () => void;
  aoSalvar: () => void;
}

const vazio = { nome: '', sku: '', categoria_id: '', fornecedor_id: '', preco: '', estoque_minimo: '', estoque_maximo: '', prazo_reposicao_dias: '7', saldo_inicial: '0' };

export default function FormProduto({ produtoId, aoFechar, aoSalvar }: Props) {
  const notificar = useNotificacoes();
  const [form, setForm] = useState(vazio);
  const [categorias, setCategorias] = useState<Categoria[]>([]);
  const [fornecedores, setFornecedores] = useState<Fornecedor[]>([]);
  const [ativo, setAtivo] = useState(true);
  const [carregando, setCarregando] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      api.get<Categoria[]>('/categorias'),
      api.get<Fornecedor[]>('/fornecedores'),
      produtoId ? api.get<Produto>(`/produtos/${produtoId}`) : Promise.resolve(null),
    ]).then(([cats, forns, produto]) => {
      setCategorias(cats.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')));
      setFornecedores(forns.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')));
      if (produto) {
        setAtivo(produto.ativo);
        setForm({
          nome: produto.nome, sku: produto.sku, categoria_id: produto.categoria_id, fornecedor_id: produto.fornecedor_id,
          preco: (Number(produto.preco) / 100).toFixed(2).replace('.', ','), estoque_minimo: String(produto.estoque_minimo),
          estoque_maximo: produto.estoque_maximo == null ? '' : String(produto.estoque_maximo),
          prazo_reposicao_dias: produto.prazo_reposicao_dias == null ? '' : String(produto.prazo_reposicao_dias), saldo_inicial: '0',
        });
      }
    }).catch((e: Error) => setErro(e.message)).finally(() => setCarregando(false));
  }, [produtoId]);

  const alterar = (campo: keyof typeof vazio) => (e: { target: { value: string } }) => setForm((f) => ({ ...f, [campo]: e.target.value }));

  const salvar = async (e: FormEvent) => {
    e.preventDefault();
    setErro(null);
    const centavos = Math.round(Number(form.preco.replace(/\./g, '').replace(',', '.')) * 100);
    if (Number.isNaN(centavos)) {
      setErro('Informe um preço válido, por exemplo 12,90.');
      return;
    }
    const corpo = {
      nome: form.nome.trim(), sku: form.sku.trim(), categoria_id: form.categoria_id, fornecedor_id: form.fornecedor_id,
      preco: centavos, estoque_minimo: Number(form.estoque_minimo),
      estoque_maximo: form.estoque_maximo === '' ? null : Number(form.estoque_maximo),
      prazo_reposicao_dias: form.prazo_reposicao_dias === '' ? null : Number(form.prazo_reposicao_dias),
      ativo,
    };
    setSalvando(true);
    try {
      if (produtoId) {
        await api.put(`/produtos/${produtoId}`, corpo);
        notificar.sucesso('Produto atualizado', corpo.nome);
      } else {
        const criado = await api.post<Produto>('/produtos', corpo);
        await api.post('/estoques', { produto_id: criado.id, quantidade: Number(form.saldo_inicial || 0) });
        notificar.sucesso('Produto cadastrado', `${corpo.nome} com saldo inicial de ${form.saldo_inicial || 0} unidade(s)`);
      }
      aoSalvar();
      aoFechar();
    } catch (err) {
      setErro((err as Error).message);
    } finally {
      setSalvando(false);
    }
  };

  return (
    <Modal
      titulo={produtoId ? 'Editar produto' : 'Novo produto'}
      descricao="O ponto de pedido é calculado a partir do estoque mínimo, do prazo de reposição e da demanda real."
      aoFechar={aoFechar}
      largo
      rodape={
        <>
          <button className="btn" type="button" onClick={aoFechar}>Cancelar</button>
          <button className="btn btn-primario" type="submit" form="form-produto" disabled={salvando || carregando}>
            {salvando && <Loader2 size={15} className="giro" />} Salvar
          </button>
        </>
      }
    >
      {carregando ? <div className="esqueleto" style={{ height: 260 }} /> : (
        <form id="form-produto" className="form-grade" onSubmit={salvar}>
          {erro && <div className="aviso aviso-perigo inteira">{erro}</div>}
          <Campo rotulo="Nome" ajuda="De 5 a 20 caracteres">
            <input className="input" required minLength={5} maxLength={20} value={form.nome} onChange={alterar('nome')} />
          </Campo>
          <Campo rotulo="SKU (código)" ajuda="De 5 a 20 caracteres">
            <input className="input" required minLength={5} maxLength={20} value={form.sku} onChange={alterar('sku')} />
          </Campo>
          <Campo rotulo="Categoria">
            <select className="select" required value={form.categoria_id} onChange={alterar('categoria_id')}>
              <option value="">Selecione…</option>
              {categorias.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
            </select>
          </Campo>
          <Campo rotulo="Fornecedor">
            <select className="select" required value={form.fornecedor_id} onChange={alterar('fornecedor_id')}>
              <option value="">Selecione…</option>
              {fornecedores.map((f) => <option key={f.id} value={f.id}>{f.nome}</option>)}
            </select>
          </Campo>
          <Campo rotulo="Preço de venda (R$)">
            <input className="input" required inputMode="decimal" placeholder="0,00" value={form.preco} onChange={alterar('preco')} />
          </Campo>
          <Campo rotulo="Prazo de reposição (dias)" ajuda="Tempo de entrega do fornecedor">
            <input className="input" type="number" min={1} value={form.prazo_reposicao_dias} onChange={alterar('prazo_reposicao_dias')} />
          </Campo>
          <Campo rotulo="Estoque mínimo" ajuda="Reserva de segurança">
            <input className="input" type="number" min={0} required value={form.estoque_minimo} onChange={alterar('estoque_minimo')} />
          </Campo>
          <Campo rotulo="Estoque máximo" ajuda="Acima disso, o produto fica em superestoque">
            <input className="input" type="number" min={1} value={form.estoque_maximo} onChange={alterar('estoque_maximo')} />
          </Campo>
          {!produtoId && (
            <Campo rotulo="Saldo inicial" ajuda="Registrado como uma entrada no histórico">
              <input className="input" type="number" min={0} value={form.saldo_inicial} onChange={alterar('saldo_inicial')} />
            </Campo>
          )}
        </form>
      )}
    </Modal>
  );
}
