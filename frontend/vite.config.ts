import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import svgr from "vite-plugin-svgr";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

function readBackendProxyTarget(): string {
  const envPath = path.resolve(__dirname, ".env");
  const envContent = fs.readFileSync(envPath, "utf8");
  const match = envContent.match(/^\s*VITE_API_URL\s*=\s*(.+)\s*$/m);
  const rawTarget = match?.[1]?.trim() || "http://localhost:8080";

  return rawTarget.replace(/\/+$/, "").replace(/\/api(?:\/v1)?$/, "");
}

const backendProxyTarget = readBackendProxyTarget();

export default defineConfig({
  plugins: [tailwindcss(), react(), svgr()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
      "\\.svg\\?react$": path.resolve(
        __dirname,
        "./src/test/__mocks__/svgMock.tsx",
      ),
    },
  },
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: "./src/test/setup.ts",
    css: false,
  },
  server: {
    port: 3000,
    strictPort: true,
    proxy: {
      "/api": {
        target: backendProxyTarget,
        changeOrigin: true,
        secure: true,
        headers: {
          "ngrok-skip-browser-warning": "true",
        },
      },
    },
  },
});
