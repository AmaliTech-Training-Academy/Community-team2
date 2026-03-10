import axios, { AxiosError } from 'axios';


export const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});


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
   
  }
  return config;
});


export function classifyAxiosError(error: unknown): string {
  if (!axios.isAxiosError(error)) {
    return error instanceof Error ? error.message : 'An unexpected error occurred.';
  }

  const axiosErr = error as AxiosError;

  if (!axiosErr.response) {
    if (axiosErr.code === 'ECONNABORTED') {
      return 'Request timed out. The server took too long to respond — it may be overloaded or down.';
    }

    return (
      'Cannot reach the server at http://localhost:8080. ' +
      'Make sure your Spring Boot container is running and that CORS is ' +
      'configured to allow requests from http://localhost:3000. ' +
      'Check the browser Console → Network tab for the exact blocked request.'
    );
  }

  const status = axiosErr.response.status;
  const data   = axiosErr.response.data as Record<string, unknown> | null;

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


axiosInstance.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
    
      localStorage.removeItem('ping_auth');
  
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
