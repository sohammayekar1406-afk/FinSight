import path from "path"
import tailwindcss from "@tailwindcss/vite"
import react from "@vitejs/plugin-react"
import { defineConfig } from "vite"

// https://vite.dev/config/
//
// SINGLE-SERVER ARCHITECTURE:
// Spring Boot is the single server for this application.
// Production builds output to `dist/` and Maven packages them into Spring Boot static resources.
// Run command: `.\mvnw.cmd spring-boot:run` (Windows) or `./mvnw spring-boot:run` (Linux/macOS)
// Application URL: http://localhost:8080
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(import.meta.dirname, "./src"),
    },
  },
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
  build: {
    // Output to dist/ (Maven copies this into classpath:/static/)
    outDir: "dist",
    // Emit a clean build every time
    emptyOutDir: true,
    // Reasonable chunk-size warning threshold
    chunkSizeWarningLimit: 2000,
  },
})
