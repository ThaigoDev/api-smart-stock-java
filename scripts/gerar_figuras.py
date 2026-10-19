"""
Gera as figuras do TCC em docs/figuras/ a partir dos resultados medidos (docs/resultados/*.json).

Uso:  py scripts/gerar_figuras.py        (requer: py -m pip install matplotlib)

Figuras:
  1. arquitetura.png          - camadas do sistema implementado
  2. modelo-dados.png         - tabelas do banco e relacionamentos
  3. curva-abc.png            - curva ABC acumulada (valor vendido x itens)
  4. faturamento-mensal.png   - faturamento por mês no período simulado
"""
import json
import os

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Rectangle

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTADOS = os.path.join(RAIZ, "docs", "resultados")
SAIDA = os.path.join(RAIZ, "docs", "figuras")

# paleta: um único matiz (azul) em tons, legível em impressão em escala de cinza
TINTA = "#0b0b0b"
TINTA_2 = "#52514e"
GRADE = "#d9d8d4"
AZUL = "#2a78d6"
AZUL_ESCURO = "#1c4f8f"
TONS_CLASSE = {"A": "#c9dcf3", "B": "#e2ecf8", "C": "#f3f7fc"}
CAIXA = "#f5f8fc"

plt.rcParams.update({
    "font.family": "Arial",
    "font.size": 10,
    "text.color": TINTA,
    "axes.edgecolor": TINTA_2,
    "axes.labelcolor": TINTA,
    "xtick.color": TINTA_2,
    "ytick.color": TINTA_2,
    "axes.spines.top": False,
    "axes.spines.right": False,
    "savefig.dpi": 220,
    "savefig.bbox": "tight",
})


def carregar(nome):
    with open(os.path.join(RESULTADOS, nome), encoding="utf-8") as f:
        return json.load(f)


def brl(valor, casas=0):
    texto = f"{valor:,.{casas}f}"
    return texto.replace(",", "X").replace(".", ",").replace("X", ".")


# ---------------------------------------------------------------- 1. arquitetura

def caixa(ax, x, y, l, a, titulo, itens=(), cor=CAIXA, borda=AZUL_ESCURO, tam_titulo=10):
    ax.add_patch(FancyBboxPatch((x, y), l, a, boxstyle="round,pad=0.008,rounding_size=0.012",
                                facecolor=cor, edgecolor=borda, linewidth=1.1))
    ax.text(x + l / 2, y + a - 0.028, titulo, ha="center", va="top", fontsize=tam_titulo, fontweight="bold")
    for i, item in enumerate(itens):
        ax.text(x + l / 2, y + a - 0.072 - i * 0.034, item, ha="center", va="top", fontsize=8.6, color=TINTA_2)


def seta(ax, x, y1, y2, rotulo=None):
    ax.annotate("", xy=(x, y2), xytext=(x, y1),
                arrowprops=dict(arrowstyle="-|>", color=TINTA_2, lw=1.2, shrinkA=0, shrinkB=0))
    if rotulo:
        ax.text(x + 0.012, (y1 + y2) / 2, rotulo, va="center", fontsize=8, color=TINTA_2)


def figura_arquitetura():
    fig, ax = plt.subplots(figsize=(8.2, 8.3))
    ax.set_xlim(0, 1)
    ax.set_ylim(-0.035, 1)
    ax.axis("off")

    caixa(ax, 0.06, 0.875, 0.88, 0.105, "Clientes (HTTP + JSON)",
          ["Painel web  ·  Postman / Newman  ·  Swagger UI  ·  outras aplicações"], cor="#ffffff", borda=TINTA_2)
    seta(ax, 0.5, 0.875, 0.835, "requisição com token JWT")

    caixa(ax, 0.06, 0.635, 0.88, 0.2, "Camada de apresentação",
          ["Controllers REST: Produto, Categoria, Fornecedor, Estoque,",
           "MovimentacaoEstoque, Relatorio, Auth",
           "Segurança: autenticação JWT e autorização por perfil (admin / usuário)",
           "ManipuladorExceptionsGlobal: 400 · 401 · 403 · 404 · 409 · 422"])
    seta(ax, 0.5, 0.635, 0.595, "DTOs validados (Bean Validation)")

    caixa(ax, 0.06, 0.275, 0.88, 0.32, "Camada de negócio",
          ["Services: transação única por operação (@Transactional)",
           "ServiceMovimentacaoEstoque: saldo, bloqueio de estoque negativo, estorno",
           "ServiceRelatorio: alertas, curva ABC, giro, painel, conciliação"])
    caixa(ax, 0.12, 0.29, 0.76, 0.13, "Domínio (classes sem dependência de framework)",
          ["TipoMovimentacaoEstoque (efeito no saldo)  ·  PoliticaEstoque (ponto de pedido)",
           "CalculadoraCurvaABC  ·  CalculadoraGiro"], cor="#ffffff", tam_titulo=9)
    seta(ax, 0.5, 0.275, 0.24, "injeção de dependências (interfaces de repositório)")

    caixa(ax, 0.06, 0.11, 0.88, 0.13, "Camada de persistência",
          ["Repositories Spring Data JPA (Hibernate)",
           "SELECT ... FOR UPDATE no saldo durante cada movimentação"])
    seta(ax, 0.5, 0.11, 0.08)

    caixa(ax, 0.06, -0.02, 0.88, 0.1, "PostgreSQL",
          ["Esquema versionado pelo Flyway (V1, V2)  ·  restrições UNIQUE, CHECK e chaves estrangeiras"],
          cor="#ffffff", borda=TINTA_2)

    fig.savefig(os.path.join(SAIDA, "arquitetura.png"))
    plt.close(fig)


# ---------------------------------------------------------------- 2. modelo de dados

TABELAS = {
    "categoria": (0.03, 0.70, ["id  (PK)", "nome", "descricao"]),
    "fornecedor": (0.03, 0.20, ["id  (PK)", "nome", "email", "telefone"]),
    "produto": (0.37, 0.43, ["id  (PK)", "nome, sku, preco", "estoque_minimo", "estoque_maximo",
                             "prazo_reposicao_dias", "ativo", "categoria_id  (FK)", "fornecedor_id  (FK)"]),
    "estoque": (0.72, 0.72, ["id  (PK)", "produto_id  (FK, único)", "quantidade", "ultima_sequencia"]),
    "movimentacao_estoque": (0.72, 0.06, ["id  (PK)", "produto_id  (FK)", "tipo  (CHECK)", "quantidade, motivo",
                                          "data_movimentacao", "saldo_anterior", "saldo_posterior",
                                          "sequencia", "estornada", "movimentacao_origem_id  (FK)",
                                          "UNIQUE (produto_id, sequencia)"]),
}
LARGURA_TABELA = 0.26
LINHA = 0.042


def tabela(ax, nome):
    x, y, colunas = TABELAS[nome]
    altura = LINHA * (len(colunas) + 1.3)
    ax.add_patch(Rectangle((x, y), LARGURA_TABELA, altura, facecolor="#ffffff", edgecolor=AZUL_ESCURO, lw=1.1))
    ax.add_patch(Rectangle((x, y + altura - LINHA * 1.15), LARGURA_TABELA, LINHA * 1.15,
                           facecolor=TONS_CLASSE["A"], edgecolor=AZUL_ESCURO, lw=1.1))
    ax.text(x + LARGURA_TABELA / 2, y + altura - LINHA * 0.58, nome, ha="center", va="center",
            fontsize=9.2, fontweight="bold")
    for i, coluna in enumerate(colunas):
        ax.text(x + 0.012, y + altura - LINHA * (1.75 + i), coluna, va="center", fontsize=8.3,
                color=TINTA if "PK" in coluna or "FK" in coluna else TINTA_2)
    return x, y, altura


def ligacao(ax, origem, destino, rotulo_origem, rotulo_destino):
    (x1, y1), (x2, y2) = origem, destino
    ax.plot([x1, x2], [y1, y2], color=TINTA_2, lw=1.1)
    # rótulos de cardinalidade junto às pontas, deslocados perpendicularmente para não tocar a linha
    dx, dy = x2 - x1, y2 - y1
    comprimento = (dx ** 2 + dy ** 2) ** 0.5
    ux, uy = dx / comprimento, dy / comprimento
    nx, ny = -uy, ux
    if ny < 0:
        nx, ny = -nx, -ny
    for (px, py), sentido, rotulo in (((x1, y1), 1, rotulo_origem), ((x2, y2), -1, rotulo_destino)):
        ax.text(px + sentido * ux * 0.03 + nx * 0.025, py + sentido * uy * 0.03 + ny * 0.025, rotulo,
                fontsize=8.5, fontweight="bold", ha="center", va="center")


def figura_modelo_dados():
    fig, ax = plt.subplots(figsize=(9.2, 6.6))
    ax.set_xlim(0, 1)
    ax.set_ylim(0, 1.02)
    ax.axis("off")
    caixas = {nome: tabela(ax, nome) for nome in TABELAS}

    def borda(nome, lado, frac=0.5):
        x, y, a = caixas[nome]
        return (x + (LARGURA_TABELA if lado == "d" else 0), y + a * frac)

    ligacao(ax, borda("categoria", "d"), borda("produto", "e", 0.85), "1", "N")
    ligacao(ax, borda("fornecedor", "d"), borda("produto", "e", 0.15), "1", "N")
    ligacao(ax, borda("produto", "d", 0.85), borda("estoque", "e"), "1", "1")
    ligacao(ax, borda("produto", "d", 0.15), borda("movimentacao_estoque", "e", 0.75), "1", "N")
    # autorrelacionamento: estorno aponta para a movimentação original
    x, y, a = caixas["movimentacao_estoque"]
    ax.annotate("", xy=(x + LARGURA_TABELA, y + a * 0.15), xytext=(x + LARGURA_TABELA, y + a * 0.35),
                arrowprops=dict(arrowstyle="-", color=TINTA_2, lw=1.1, connectionstyle="arc3,rad=-0.9"))
    ax.text(x + LARGURA_TABELA + 0.008, y + a * 0.06, "estorno → origem", fontsize=7.8, color=TINTA_2)

    fig.savefig(os.path.join(SAIDA, "modelo-dados.png"))
    plt.close(fig)


# ---------------------------------------------------------------- 3. curva ABC

def figura_curva_abc():
    abc = carregar("curva-abc.json")
    itens = abc["itens"]
    n = len(itens)
    xs = [0.0] + [100 * (i + 1) / n for i in range(n)]
    ys = [0.0] + [float(i["percentual_acumulado"]) for i in itens]

    fig, ax = plt.subplots(figsize=(7.6, 4.4))
    inicio = 0.0
    for resumo in abc["resumo_por_classe"]:
        fim = inicio + float(resumo["percentual_produtos"])
        ax.axvspan(inicio, fim, color=TONS_CLASSE[resumo["classe"]], lw=0, zorder=0)
        ax.text((inicio + fim) / 2, 6,
                f"Classe {resumo['classe']}\n{resumo['quantidade_produtos']} itens\n"
                f"{brl(float(resumo['percentual_valor']), 1)}% do valor",
                ha="center", va="bottom", fontsize=8.6, color=TINTA)
        inicio = fim

    for limite in (80, 95):
        ax.axhline(limite, color=TINTA_2, lw=0.8, ls=(0, (4, 3)), zorder=1)
        ax.text(101, limite, f"{limite}%", va="center", fontsize=8.5, color=TINTA_2)

    ax.plot(xs, ys, color=AZUL, lw=2, zorder=3)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 102)
    ax.set_xlabel("Itens em ordem decrescente de valor vendido (% acumulado do total de 59 produtos)")
    ax.set_ylabel("Valor vendido (% acumulado)")
    ax.grid(axis="y", color=GRADE, lw=0.6, zorder=0)
    ax.set_axisbelow(True)
    fig.savefig(os.path.join(SAIDA, "curva-abc.png"))
    plt.close(fig)


# ---------------------------------------------------------------- 4. faturamento mensal

MESES = ["jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"]


def figura_faturamento_mensal():
    mensal = carregar("movimentacoes-mensais.json")
    rotulos, valores, parciais = [], [], []
    for i, m in enumerate(mensal):
        ano, mes = m["mes"].split("-")
        parcial = i == 0 or i == len(mensal) - 1  # o período começa e termina no meio do mês
        rotulos.append(f"{MESES[int(mes) - 1]}/{ano[2:]}" + ("*" if parcial else ""))
        valores.append(float(m["faturamento"]) / 1000)
        parciais.append(parcial)

    fig, ax = plt.subplots(figsize=(7.6, 3.9))
    barras = ax.bar(rotulos, valores, width=0.62, color=[TONS_CLASSE["A"] if p else AZUL for p in parciais],
                    edgecolor=[AZUL if p else AZUL for p in parciais], linewidth=1, hatch=None, zorder=2)
    for barra, parcial in zip(barras, parciais):
        if parcial:
            barra.set_hatch("///")
    for barra, valor in zip(barras, valores):
        ax.text(barra.get_x() + barra.get_width() / 2, valor + 1.2, brl(valor, 1), ha="center", va="bottom",
                fontsize=8.6, color=TINTA)
    ax.set_ylabel("Faturamento (R$ mil)")
    ax.set_ylim(0, max(valores) * 1.15)
    ax.grid(axis="y", color=GRADE, lw=0.6)
    ax.set_axisbelow(True)
    ax.text(0, -0.2, "* meses parciais: o período simulado vai de 13/04 a 11/10/2026",
            transform=ax.transAxes, fontsize=8.2, color=TINTA_2)
    fig.savefig(os.path.join(SAIDA, "faturamento-mensal.png"))
    plt.close(fig)


if __name__ == "__main__":
    os.makedirs(SAIDA, exist_ok=True)
    figura_arquitetura()
    figura_modelo_dados()
    figura_curva_abc()
    figura_faturamento_mensal()
    print("Figuras geradas em", SAIDA)
