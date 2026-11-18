import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// strictPort: if the port is busy, Vite stops instead of silently using another one.
// This matters because the API Gateway only accepts the exact origins listed in its CORS setting
// (http://localhost:5173 for "npm run dev", http://localhost:4173 for "npm run preview").
export default defineConfig({
  plugins: [react()],
  server: { port: 5173, strictPort: true },
  preview: { port: 4173, strictPort: true },
})
