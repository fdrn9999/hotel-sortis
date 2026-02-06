import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          // Core Vue ecosystem
          'vendor-vue': ['vue', 'vue-router', 'pinia'],
          // Internationalization
          'vendor-i18n': ['vue-i18n'],
          // Audio library (lazy-loaded with BGM)
          'vendor-howler': ['howler'],
          // WebSocket libraries (lazy-loaded with PvP/Draft)
          'vendor-websocket': ['@stomp/stompjs', 'sockjs-client']
        }
      }
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws': {
        target: 'http://localhost:8080',
        ws: true
      }
    }
  }
})
