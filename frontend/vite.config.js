import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
// CSS Modules (*.module.css) are built into Vite, no plugin needed.
export default defineConfig({
  plugins: [react()],
  server: {
    // The Vite dev server forwards /api/... requests to the backend.
    // The browser sees a single origin (localhost:5173), so no CORS config is needed.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
