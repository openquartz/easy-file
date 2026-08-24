import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  base: '/easyfile-ui/',
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/easyfile-ui/login': { target: 'http://localhost:8080', changeOrigin: true },
      '/easyfile-ui/logout': { target: 'http://localhost:8080', changeOrigin: true },
      '/easyfile-ui/download-task': { target: 'http://localhost:8080', changeOrigin: true }
    }
  },
  build: {
    outDir: '../src/main/resources/easyfile-admin-ui',
    assetsDir: 'static/assets',
    emptyOutDir: true
  },
  test: {
    environment: 'node'
  }
})
