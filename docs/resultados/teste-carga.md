| Cenário | Clientes simultâneos | Requisições | Throughput (req/s) | Latência média (ms) | p95 (ms) | p99 (ms) | Erros 5xx (%) | Status |
|---|---:|---:|---:|---:|---:|---:|---:|---|
| Leitura (produtos, estoques, alertas, curva ABC) | 1 | 1000 | 36.0 | 27.6 | 57.0 | 68.8 | 0.0 | 200: 1000 |
| Leitura (produtos, estoques, alertas, curva ABC) | 10 | 1000 | 384.1 | 25.5 | 58.2 | 66.8 | 0.0 | 200: 1000 |
| Leitura (produtos, estoques, alertas, curva ABC) | 50 | 1000 | 513.9 | 93.1 | 132.2 | 147.7 | 0.0 | 200: 1000 |
| Escrita (movimentações de entrada e saída) | 1 | 1000 | 46.9 | 21.2 | 33.6 | 35.6 | 0.0 | 201: 988, 422: 12 |
| Escrita (movimentações de entrada e saída) | 10 | 1000 | 847.7 | 11.6 | 29.4 | 32.2 | 0.0 | 201: 990, 422: 10 |
| Escrita (movimentações de entrada e saída) | 50 | 1000 | 1233.1 | 38.7 | 60.4 | 70.2 | 0.0 | 201: 1000 |
| Disputa: 50 saídas simultâneas de 5 un. (saldo 100) | 50 | 50 | 382.6 | 83.2 | 111.3 | 113.1 | 0.0 | 201: 20, 422: 30 |

Disputa pelo mesmo produto: saldo final 0, resultado CORRETO.

Conciliação final: {"produtos_verificados": 61, "produtos_consistentes": 61, "produtos_divergentes": 0, "movimentacoes_verificadas": 21640, "quebras_de_encadeamento": 0, "produtos_com_saldo_negativo": 0, "consistente": true, "tempo_ms": 160.4}
