import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import path from "node:path";
import { fileURLToPath } from "node:url";
import svgr from "vite-plugin-svgr";
import { loadEnv } from "vite";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

function readBackendProxyTarget(env: Record<string, string>): string {
    const rawTarget =
        env.VITE_BACKEND_PROXY_TARGET ||
        // If VITE_API_URL is absolute, use it as the basis for the proxy target.
        (env.VITE_API_URL?.match(/^https?:\/\//) ? env.VITE_API_URL : "") ||
        "http://localhost:8080";

    return rawTarget.replace(/\/+$/, "").replace(/\/api(?:\/v1)?$/, "");
}

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, __dirname, "");
    const backendProxyTarget = readBackendProxyTarget(env);

    return {
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
    };
});
