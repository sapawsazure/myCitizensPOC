import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

// Dev mode (npm run dev): UI on http://localhost:5173, /api proxied to the Spring Boot backend.
// Production-like mode (npm run build): the UI is written into the backend's static folder,
// so Spring Boot serves UI + API from the one port, e.g. http://localhost:8180.
const backend = process.env.VITE_BACKEND_URL || 'http://localhost:8180';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { '/api': backend },
  },
  build: {
    outDir: '../backend/src/main/resources/static',
    emptyOutDir: true,
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.js',
  },
});
