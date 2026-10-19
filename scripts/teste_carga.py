"""
Teste de carga e de concorrência da API Smart Stock (somente biblioteca padrão do Python 3.10+).

Uso (com a API rodando e populada pelo seed):
    py scripts/teste_carga.py --url http://localhost:8080

Cenários:
  1. Leitura  - GET de produtos, alertas e curva ABC sob 1, 10 e 50 clientes simultâneos.
  2. Escrita  - POST de entradas e saídas em produtos aleatórios sob 1, 10 e 50 clientes simultâneos.
  3. Disputa  - 50 saídas simultâneas de 5 unidades sobre um único produto com saldo 100:
                o esperado é exatamente 20 aprovações (201), 30 recusas (422) e saldo final 0.
  4. Conciliação - verifica, ao final, que todo saldo continua igual à soma do seu histórico.

Os resultados são gravados em docs/resultados/teste-carga.json e teste-carga.md.
"""
import argparse
import json
import os
import random
import statistics
import threading
import time
import urllib.error
import urllib.request
from collections import Counter
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime

NIVEIS_CONCORRENCIA = [1, 10, 50]


class Cliente:
    def __init__(self, url_base, token=None):
        self.url_base = url_base.rstrip("/")
        self.token = token

    def requisitar(self, metodo, caminho, corpo=None):
        dados = json.dumps(corpo).encode() if corpo is not None else None
        req = urllib.request.Request(self.url_base + caminho, data=dados, method=metodo)
        req.add_header("Content-Type", "application/json")
        if self.token:
            req.add_header("Authorization", "Bearer " + self.token)
        inicio = time.perf_counter()
        try:
            with urllib.request.urlopen(req, timeout=60) as resp:
                conteudo = resp.read()
                status = resp.status
        except urllib.error.HTTPError as e:
            conteudo = e.read()
            status = e.code
        except Exception:  # falha de conexão/timeout
            return 0, (time.perf_counter() - inicio) * 1000, None
        duracao_ms = (time.perf_counter() - inicio) * 1000
        try:
            corpo_resp = json.loads(conteudo) if conteudo else None
        except ValueError:
            corpo_resp = None
        return status, duracao_ms, corpo_resp


def percentil(valores, p):
    if not valores:
        return 0.0
    ordenados = sorted(valores)
    k = (len(ordenados) - 1) * p / 100
    f, c = int(k), min(int(k) + 1, len(ordenados) - 1)
    return ordenados[f] + (ordenados[c] - ordenados[f]) * (k - f)


def executar_carga(nome, total, concorrencia, tarefa):
    """Executa `total` chamadas de `tarefa()` com `concorrencia` threads e mede o resultado."""
    latencias, status = [], Counter()
    trava = threading.Lock()

    def trabalho(_):
        s, ms, _corpo = tarefa()
        with trava:
            latencias.append(ms)
            status[s] += 1

    inicio = time.perf_counter()
    with ThreadPoolExecutor(max_workers=concorrencia) as ex:
        list(ex.map(trabalho, range(total)))
    duracao = time.perf_counter() - inicio
    erros_servidor = sum(q for s, q in status.items() if s == 0 or s >= 500)
    return {
        "cenario": nome,
        "concorrencia": concorrencia,
        "requisicoes": total,
        "duracao_s": round(duracao, 2),
        "throughput_req_s": round(total / duracao, 1),
        "latencia_media_ms": round(statistics.mean(latencias), 1),
        "latencia_p50_ms": round(percentil(latencias, 50), 1),
        "latencia_p95_ms": round(percentil(latencias, 95), 1),
        "latencia_p99_ms": round(percentil(latencias, 99), 1),
        "latencia_max_ms": round(max(latencias), 1),
        "taxa_erro_servidor_pct": round(100 * erros_servidor / total, 2),
        "status": {str(k): v for k, v in sorted(status.items())},
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--url", default="http://localhost:8080")
    parser.add_argument("--email", default=os.environ.get("SMARTSTOCK_EMAIL", "admin@admin.com"))
    parser.add_argument("--senha", default=os.environ.get("SMARTSTOCK_SENHA", "admin331400"))
    parser.add_argument("--requisicoes", type=int, default=500, help="requisições por nível de concorrência")
    parser.add_argument("--saida", default=os.path.join("docs", "resultados"))
    args = parser.parse_args()
    random.seed(42)

    anonimo = Cliente(args.url)
    status, _, corpo = anonimo.requisitar("POST", "/auth", {"email": args.email, "senha": args.senha})
    if status != 200:
        raise SystemExit(f"Falha no login ({status}): {corpo}")
    api = Cliente(args.url, corpo["acessToken"])

    _, _, produtos = api.requisitar("GET", "/produtos")
    ids = [p["id"] for p in produtos if p.get("ativo")]
    print(f"{len(ids)} produtos ativos encontrados")

    # aquecimento da JVM (JIT) para não penalizar o primeiro nível
    for _ in range(50):
        api.requisitar("GET", "/produtos")

    resultados = []

    leituras = ["/produtos", "/relatorios/alertas", "/relatorios/curva-abc", "/estoques"]
    for c in NIVEIS_CONCORRENCIA:
        r = executar_carga("Leitura (produtos, estoques, alertas, curva ABC)", args.requisicoes, c,
                           lambda: api.requisitar("GET", random.choice(leituras)))
        resultados.append(r)
        print(json.dumps(r, ensure_ascii=False))

    def escrita():
        tipo = random.choice(["ENTRADA", "SAIDA", "SAIDA"])
        return api.requisitar("POST", "/movimentacoesestoque", {
            "produto_id": random.choice(ids), "tipo": tipo,
            "quantidade": random.randint(1, 3) if tipo == "SAIDA" else random.randint(5, 15),
            "motivo": "teste de carga"})

    for c in NIVEIS_CONCORRENCIA:
        r = executar_carga("Escrita (movimentações de entrada e saída)", args.requisicoes, c, escrita)
        resultados.append(r)
        print(json.dumps(r, ensure_ascii=False))

    # ---- disputa pelo mesmo produto
    _, _, categorias = api.requisitar("GET", "/categorias")
    _, _, fornecedores = api.requisitar("GET", "/fornecedores")
    sufixo = datetime.now().strftime("%H%M%S")
    _, _, produto = api.requisitar("POST", "/produtos", {
        "nome": "Disputa " + sufixo, "sku": "DSP-" + sufixo, "estoque_minimo": 5, "estoque_maximo": 200,
        "categoria_id": categorias[0]["id"], "fornecedor_id": fornecedores[0]["id"], "preco": 1000})
    api.requisitar("POST", "/estoques", {"produto_id": produto["id"], "quantidade": 100})

    barreira = threading.Barrier(50)

    def saida_disputada():
        barreira.wait()
        return api.requisitar("POST", "/movimentacoesestoque", {
            "produto_id": produto["id"], "tipo": "SAIDA", "quantidade": 5, "motivo": "disputa concorrente"})

    disputa = executar_carga("Disputa: 50 saídas simultâneas de 5 un. (saldo 100)", 50, 50, saida_disputada)
    _, _, extrato = api.requisitar("GET", f"/produtos/{produto['id']}/extrato")
    disputa["saldo_final"] = extrato[-1]["saldo_posterior"]
    disputa["aprovadas_esperadas"] = 20
    disputa["correto"] = disputa["status"].get("201", 0) == 20 and disputa["status"].get("422", 0) == 30 and disputa["saldo_final"] == 0
    resultados.append(disputa)
    print(json.dumps(disputa, ensure_ascii=False))

    _, ms, conciliacao = api.requisitar("GET", "/relatorios/conciliacao")
    conciliacao.pop("divergencias", None)
    conciliacao["tempo_ms"] = round(ms, 1)
    print("Conciliação:", json.dumps(conciliacao, ensure_ascii=False))

    os.makedirs(args.saida, exist_ok=True)
    with open(os.path.join(args.saida, "teste-carga.json"), "w", encoding="utf-8") as f:
        json.dump({"executado_em": datetime.now().isoformat(timespec="seconds"), "resultados": resultados,
                   "conciliacao_final": conciliacao}, f, ensure_ascii=False, indent=2)

    linhas = [
        "| Cenário | Clientes simultâneos | Requisições | Throughput (req/s) | Latência média (ms) | p95 (ms) | p99 (ms) | Erros 5xx (%) | Status |",
        "|---|---:|---:|---:|---:|---:|---:|---:|---|",
    ]
    for r in resultados:
        status_txt = ", ".join(f"{k}: {v}" for k, v in r["status"].items())
        linhas.append(f"| {r['cenario']} | {r['concorrencia']} | {r['requisicoes']} | {r['throughput_req_s']} | "
                      f"{r['latencia_media_ms']} | {r['latencia_p95_ms']} | {r['latencia_p99_ms']} | "
                      f"{r['taxa_erro_servidor_pct']} | {status_txt} |")
    linhas += ["", f"Disputa pelo mesmo produto: saldo final {disputa['saldo_final']}, "
                   f"resultado {'CORRETO' if disputa['correto'] else 'INCORRETO'}.",
               "", "Conciliação final: " + json.dumps(conciliacao, ensure_ascii=False)]
    with open(os.path.join(args.saida, "teste-carga.md"), "w", encoding="utf-8") as f:
        f.write("\n".join(linhas) + "\n")
    print("\n".join(linhas))


if __name__ == "__main__":
    main()
