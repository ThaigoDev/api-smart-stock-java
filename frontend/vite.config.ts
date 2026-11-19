import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// rotas atendidas pela API Spring Boot (proxy em desenvolvimento)
const rotasApi = ['/auth', '/produtos', '/categorias', '/fornecedores', '/estoques', '/movimentacoesestoque',
  '/relatorios', '/pedidos-compra', '/v3', '/swagger-ui'];

export default defineConfig({
  plugins: [react()],
  build: {
    // o build é servido pela própria API, como recurso estático
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
    chunkSizeWarningLimit: 900,
  },
  server: {
    port: 5173,
    proxy: Object.fromEntries(rotasApi.map((rota) => [rota, { target: 'http://localhost:8080', changeOrigin: true }])),
  },
});
