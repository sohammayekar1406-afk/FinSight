import path from "path"
import tailwindcss from "@tailwindcss/vite"
import react from "@vitejs/plugin-react"
import { defineConfig, type Plugin } from "vite"

// ──────────────────────────────────────────────────────────────────────────────
// SINGLE-SERVER ARCHITECTURE
// ──────────────────────────────────────────────────────────────────────────────
// Spring Boot is the single server for this application.
// Production builds output to dist/ and Maven packages them into Spring Boot
// static resources so both the API and the React SPA are served from port 8080.
//
// The ONE correct way to run FinSight locally:
//   Windows:       .\mvnw.cmd spring-boot:run
//   Linux / macOS: ./mvnw spring-boot:run
//   URL:           http://localhost:8080
//
// This Vite dev server (port 5173) exists ONLY for frontend hot-reload during
// development. It proxies /api → http://localhost:8080, so the Spring Boot
// backend MUST be running separately or every API call returns 502 Bad Gateway.
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Vite plugin that prints a loud warning whenever the dev server is started
 * directly (e.g. `npm run dev` inside next-app/).  This prevents the "Unable
 * to load dashboard" confusion when the backend is not running at :8080.
 */
function devServerWarningPlugin(): Plugin {
  return {
    name: "finsight-dev-warning",
    configureServer(server) {
      server.httpServer?.once("listening", () => {
        const reset = "\x1b[0m"
        const bold = "\x1b[1m"
        const red = "\x1b[31m"
        const yellow = "\x1b[33m"
        const cyan = "\x1b[36m"

        // Blank line then a full-width banner so it is impossible to miss.
        console.log("")
        console.log(`${bold}${red}╔══════════════════════════════════════════════════════════════╗${reset}`)
        console.log(`${bold}${red}║           ⚠  FRONTEND-ONLY DEV SERVER (port 5173)  ⚠        ║${reset}`)
        console.log(`${bold}${red}╠══════════════════════════════════════════════════════════════╣${reset}`)
        console.log(`${bold}${red}║  This is the Vite HMR server — it has NO backend behind it.  ║${reset}`)
        console.log(`${bold}${red}║  Every API call will return ${yellow}502 Bad Gateway${red} without Spring Boot.  ║${reset}`)
        console.log(`${bold}${red}╠══════════════════════════════════════════════════════════════╣${reset}`)
        console.log(`${bold}${red}║  ${cyan}Run Spring Boot separately first:${red}                           ║${reset}`)
        console.log(`${bold}${red}║    ${yellow}.\\.mvnw.cmd spring-boot:run${red}   (Windows)                   ║${reset}`)
        console.log(`${bold}${red}║    ${yellow}./mvnw spring-boot:run${red}         (Linux / macOS)             ║${reset}`)
        console.log(`${bold}${red}╠══════════════════════════════════════════════════════════════╣${reset}`)
        console.log(`${bold}${red}║  ${cyan}Or use the SINGLE-SERVER URL (no separate Vite needed):${red}      ║${reset}`)
        console.log(`${bold}${red}║    ${yellow}http://localhost:8080${red}                                        ║${reset}`)
        console.log(`${bold}${red}╚══════════════════════════════════════════════════════════════╝${reset}`)
        console.log("")
      })
    },
  }
}

export default defineConfig({
  plugins: [react(), tailwindcss(), devServerWarningPlugin()],
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
