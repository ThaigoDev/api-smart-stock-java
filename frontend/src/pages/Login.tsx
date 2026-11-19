import { useState, type FormEvent } from 'react';
import { AlertCircle, BellRing, Loader2, Package, ShieldCheck, TrendingUp } from 'lucide-react';
import { useSessao } from '../lib/sessao';

export default function Login() {
  const { entrar } = useSessao();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      await entrar(email, senha);
    } catch {
      setErro('E-mail ou senha inválidos.');
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="login">
      <section className="login-marca">
        <div className="brand" style={{ padding: 0 }}>
          <div className="brand-logo"><Package size={19} color="#fff" /></div>
          <div className="brand-name">Smart Stock</div>
        </div>
        <div>
          <h1>Controle de estoque inteligente para pequenos negócios</h1>
          <p>Saiba o que comprar, quando comprar e quanto dinheiro está parado na prateleira.</p>
          <div className="login-beneficios">
            <div className="login-beneficio">
              <div className="icone"><BellRing size={18} /></div>
              <div><strong>Alertas de reposição</strong><span>Ponto de pedido calculado pela demanda real de cada produto.</span></div>
            </div>
            <div className="login-beneficio">
              <div className="icone"><TrendingUp size={18} /></div>
              <div><strong>Indicadores gerenciais</strong><span>Curva ABC, giro de estoque e faturamento mensal.</span></div>
            </div>
            <div className="login-beneficio">
              <div className="icone"><ShieldCheck size={18} /></div>
              <div><strong>Histórico auditável</strong><span>Toda alteração de saldo registrada, sem estoque negativo.</span></div>
            </div>
          </div>
        </div>
        <span style={{ color: '#7f92b5', fontSize: 13 }}>API RESTful · Java · Spring Boot · PostgreSQL</span>
      </section>

      <section className="login-form">
        <form onSubmit={enviar}>
          <div>
            <h1>Entrar</h1>
            <p className="muted" style={{ margin: '6px 0 0' }}>Acesse com seu e-mail e senha.</p>
          </div>
          {erro && (
            <div className="aviso aviso-perigo"><AlertCircle size={17} />{erro}</div>
          )}
          <div className="campo">
            <label htmlFor="email">E-mail</label>
            <input id="email" className="input" type="email" autoComplete="username" required value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div className="campo">
            <label htmlFor="senha">Senha</label>
            <input id="senha" className="input" type="password" autoComplete="current-password" required value={senha} onChange={(e) => setSenha(e.target.value)} />
          </div>
          <button className="btn btn-primario" style={{ height: 42 }} disabled={enviando}>
            {enviando && <Loader2 size={16} className="giro" />} Entrar
          </button>
          <p className="muted small" style={{ margin: 0, textAlign: 'center' }}>
            Documentação da API em <a href="/swagger-ui.html">/swagger-ui.html</a>
          </p>
        </form>
      </section>
    </div>
  );
}
