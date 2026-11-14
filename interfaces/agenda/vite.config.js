import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    // Los dos microservicios solo aceptan peticiones desde los puertos de las interfaces (CORS)
    port: 5174,
    strictPort: true,
  },
})
