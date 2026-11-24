import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  css: {
    preprocessorOptions: {
      scss: {
        // Silencia as deprecation warnings do Dart Sass vindas do
        // Bootstrap 5.3 (node_modules): ele ainda usa @import e
        // funções globais legadas (mix/red/unit/if), removidos no
        // Dart Sass 3.0. Migrate completo espera o Bootstrap v6.
        quietDeps: true,
        silenceDeprecations: ['import'],
      },
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
