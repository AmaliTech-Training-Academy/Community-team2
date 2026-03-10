import axios, { AxiosError } from 'axios';

// ── Base URL ─────────────────────────────────────────────────────────────────
// In development: requests go to /api which Vite proxies to http://localhost:8080
// (see vite.config.ts).  No CORS issues — the browser only ever talks to :3000.
// In production: set VITE_API_URL to your deployed backend origin.
export const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

// ── Request interceptor — attach JWT Bearer token ────────────────────────────
// The token is stored by Zustand's persist middleware under the key 'ping_auth'.
// We read it from the persisted JSON rather than a separate localStorage key
// so there is one source of truth.
axiosInstance.interceptors.request.use((config) => {
  try {
    const raw = localStorage.getItem('ping_auth');
    if (raw) {
      const parsed = JSON.parse(raw) as { state?: { token?: string } };
      const token = parsed?.state?.token;
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
  } catch {
    // Corrupt storage — ignore, let the request go through unauthenticated
  }
  return config;
});

// ── Error classifier ─────────────────────────────────────────────────────────
// Converts any axios error into a human-readable string that distinguishes:
//   1. No response  — server is down OR CORS preflight was blocked
//   2. Timeout      — server is reachable but too slow
//   3. HTTP 4xx/5xx — server responded with an error status
export function classifyAxiosError(error: unknown): string {
  if (!axios.isAxiosError(error)) {
    return error instanceof Error ? error.message : 'An unexpected error occurred.';
  }

  const axiosErr = error as AxiosError;

  if (!axiosErr.response) {
    if (axiosErr.code === 'ECONNABORTED') {
      return 'Request timed out. The server took too long to respond — it may be overloaded or down.';
    }
    // "Network Error" is the browser's way of saying it got nothing back.
    // This happens both when the server is down AND when a CORS preflight is
    // rejected. Both are indistinguishable at the JS level.
    return (
      'Cannot reach the server at http://localhost:8080. ' +
      'Make sure your Spring Boot container is running and that CORS is ' +
      'configured to allow requests from http://localhost:3000. ' +
      'Check the browser Console → Network tab for the exact blocked request.'
    );
  }

  const status = axiosErr.response.status;
  const data   = axiosErr.response.data as Record<string, unknown> | null;

  // Try the most common Spring Boot error body shapes
  const serverMessage: string | null =
    (typeof data?.message  === 'string' && data.message)  ||
    (typeof data?.error    === 'string' && data.error)     ||
    (typeof data?.errorMessage === 'string' && data.errorMessage) ||
    null;

  switch (true) {
    case status === 400: return serverMessage || 'Bad request. Please check your input.';
    case status === 401: return serverMessage || 'Invalid credentials. Please check your email and password.';
    case status === 403: return serverMessage || 'You do not have permission to perform this action.';
    case status === 404: return serverMessage || 'The requested resource was not found.';
    case status === 409: return serverMessage || 'A conflict occurred — this resource may already exist.';
    case status === 422: return serverMessage || 'Validation failed. Please check your input.';
    case status >= 500:  return serverMessage || `Server error (${status}). Please try again later.`;
    default:             return serverMessage || `Request failed with status ${status}.`;
  }
}

// ── Response interceptor — handle 401 globally ───────────────────────────────
axiosInstance.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Wipe only the Zustand persist key — consistent with authStore
      localStorage.removeItem('ping_auth');
      // Avoid redirect loops when already on an auth route
      if (
        !window.location.pathname.startsWith('/login') &&
        !window.location.pathname.startsWith('/register')
      ) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(classifyAxiosError(error));
  }
);

export default axiosInstance;
