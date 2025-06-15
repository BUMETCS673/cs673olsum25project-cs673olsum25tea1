import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: "0.0.0.0",
    port: 5173,
    strictPort: true
  },
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: ["./src/test/setup.js"],
    reporters: ["default", "junit"], 
    
    outputFile: {
      junit: "./test-results/junit.xml", 
      html: "./test-results/html/index.html" 
    },

    coverage: {
      enabled: true,
      reporter: ["text", "json"], 
      reportsDirectory: "./coverage", 
    }
  }
})
