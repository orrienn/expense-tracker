import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // Żądania na /api/... serwer deweloperski Vite przekazuje do backendu.
    // Przeglądarka widzi jeden adres (localhost:5173), więc nie trzeba konfigurować CORS.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
