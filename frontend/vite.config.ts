import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import svgr from 'vite-plugin-svgr'

export default defineConfig({
  plugins: [
    tailwindcss(),
    react(),
    svgr(),
  ],
  server: {
    port: 3000,
    strictPort: true, // fail fast if 3000 is already occupied
    proxy: {
      // Every request starting with /api is forwarded to Spring Boot.
      // The frontend code calls e.g. /api/auth/login  →  http://localhost:8080/auth/login
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
