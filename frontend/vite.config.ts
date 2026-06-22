import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  define: {
    global: 'globalThis',
  },
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/auth': 'http://localhost:8080',
      '/ws': { target: 'http://localhost:8080', ws: true },
    },
  },
})
