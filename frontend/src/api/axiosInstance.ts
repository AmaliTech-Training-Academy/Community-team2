import axios, { AxiosError } from "axios";

function normalizeApiBaseUrl(rawBaseUrl?: string): string {
  const base = (rawBaseUrl || "/api").replace(/\/+$/, "");

  if (base.endsWith("/api/v1")) return base;
  if (base.endsWith("/api")) return `${base}/v1`;
  return `${base}/api/v1`;
}

export const API_BASE_URL = normalizeApiBaseUrl(import.meta.env.VITE_API_URL);

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: { "Content-Type": "application/json" },
  timeout: 15000,
  withCredentials: true,
});

axiosInstance.interceptors.request.use((config) => {
  if (config.data instanceof FormData && config.headers) {
    delete config.headers["Content-Type"];
  }

  config.headers = config.headers ?? {};

  try {
    const raw = localStorage.getItem("ping_auth");
    if (raw) {
      const parsed = JSON.parse(raw) as { state?: { token?: string } };
      const token = parsed?.state?.token;
      if (token) {
        if (!config.headers.Authorization) {
          config.headers.Authorization = `Bearer ${token}`;
        }
      }
    }
  } catch {}
  return config;
});

const GENERIC_MESSAGES = new Set([
  "validation failed",
  "validation error",
  "bad request",
  "conflict",
  "request failed",
  "invalid request",
  "unprocessable entity",
]);

function humanizeFieldName(field: string): string {
  return field
    .replace(/([a-z])([A-Z])/g, "$1 $2")
    .replace(/[._-]+/g, " ")
    .replace(/^./, (char) => char.toUpperCase());
}

function normalizeMessage(message: string): string {
  return message.trim().replace(/\s+/g, " ");
}

function isGenericMessage(message: string | null | undefined): boolean {
  if (!message) {
    return true;
  }

  const normalized = normalizeMessage(message).toLowerCase();
  return (
    GENERIC_MESSAGES.has(normalized) ||
    normalized.startsWith("validation failed") ||
    normalized.startsWith("bad request")
  );
}

function extractFieldMessage(data: Record<string, unknown>): string | null {
  const containers = ["errors", "fieldErrors", "violations", "details"];

  for (const key of containers) {
    const value = data[key];

    if (Array.isArray(value)) {
      for (const item of value) {
        if (!item || typeof item !== "object") {
          continue;
        }

        const typedItem = item as Record<string, unknown>;
        const messageValue =
          typeof typedItem.message === "string"
            ? typedItem.message
            : typeof typedItem.error === "string"
              ? typedItem.error
              : typeof typedItem.defaultMessage === "string"
                ? typedItem.defaultMessage
                : null;

        if (!messageValue) {
          continue;
        }

        const fieldValue =
          typeof typedItem.field === "string"
            ? typedItem.field
            : typeof typedItem.path === "string"
              ? typedItem.path
              : null;

        return fieldValue
          ? `${humanizeFieldName(fieldValue)}: ${normalizeMessage(messageValue)}`
          : normalizeMessage(messageValue);
      }
    }

    if (value && typeof value === "object") {
      for (const [field, messageValue] of Object.entries(
        value as Record<string, unknown>,
      )) {
        if (typeof messageValue === "string" && messageValue.trim()) {
          return `${humanizeFieldName(field)}: ${normalizeMessage(messageValue)}`;
        }
      }
    }
  }

  return null;
}

function extractPrimaryServerMessage(data: unknown): string | null {
  if (!data) {
    return null;
  }

  if (typeof data === "string") {
    return normalizeMessage(data);
  }

  if (typeof data !== "object") {
    return null;
  }

  const typedData = data as Record<string, unknown>;
  const fieldMessage = extractFieldMessage(typedData);
  if (fieldMessage) {
    return fieldMessage;
  }

  const candidateKeys = [
    "message",
    "error",
    "errorMessage",
    "detail",
    "title",
    "description",
    "reason",
  ];

  for (const key of candidateKeys) {
    const value = typedData[key];
    if (typeof value === "string" && value.trim()) {
      return normalizeMessage(value);
    }
  }

  if (typedData.data) {
    return extractPrimaryServerMessage(typedData.data);
  }

  return null;
}

function classifyAuthRequestError(
  status: number,
  requestUrl: string,
  method: string,
  serverMessage: string | null,
): string | null {
  const normalizedUrl = requestUrl.toLowerCase();
  const normalizedMethod = method.toLowerCase();

  const isLoginRequest = /\/users\/login(?:\?|$)/.test(normalizedUrl);
  const isForgotPasswordRequest = /\/users\/forgot-password(?:\?|$)/.test(
    normalizedUrl,
  );
  const isRegisterRequest =
    normalizedMethod === "post" &&
    /\/users(?:\?|$)/.test(normalizedUrl) &&
    !isLoginRequest &&
    !isForgotPasswordRequest;
  const isPasswordResetRequest =
    normalizedMethod === "put" && /\/users\/[^/]+(?:\?|$)/.test(normalizedUrl);

  if (isLoginRequest && [400, 401, 422].includes(status)) {
    return isGenericMessage(serverMessage)
      ? "Invalid email or password. Please try again."
      : (serverMessage as string);
  }

  if (isRegisterRequest) {
    if (status === 409) {
      return isGenericMessage(serverMessage)
        ? "An account with this email already exists. Try logging in instead."
        : (serverMessage as string);
    }

    if ([400, 422].includes(status)) {
      return isGenericMessage(serverMessage)
        ? "We couldn't create your account. Check your details and try again."
        : (serverMessage as string);
    }
  }

  if (isForgotPasswordRequest && [400, 404, 422].includes(status)) {
    return isGenericMessage(serverMessage)
      ? "We couldn't send the reset link. Check the email address and try again."
      : (serverMessage as string);
  }

  if (isPasswordResetRequest) {
    if ([401, 403].includes(status)) {
      return isGenericMessage(serverMessage)
        ? "This reset link is invalid or has expired. Request a new one and try again."
        : (serverMessage as string);
    }

    if ([400, 404, 422].includes(status)) {
      return isGenericMessage(serverMessage)
        ? "We couldn't update your password. Check the password requirements and try again."
        : (serverMessage as string);
    }
  }

  return null;
}

export function classifyAxiosError(error: unknown): string {
  if (!axios.isAxiosError(error)) {
    return error instanceof Error
      ? error.message
      : "An unexpected error occurred.";
  }

  const axiosErr = error as AxiosError;

  if (!axiosErr.response) {
    if (axiosErr.code === "ECONNABORTED") {
      return "Request timed out. The server took too long to respond — it may be overloaded or down.";
    }

    return (
      `Cannot reach the server at ${API_BASE_URL}. ` +
      "Make sure the backend is running and that CORS is " +
      "configured to allow requests from this frontend. " +
      "Check the browser Console → Network tab for the exact blocked request."
    );
  }

  const status = axiosErr.response.status;
  const requestUrl = axiosErr.config?.url ?? "";
  const requestMethod = axiosErr.config?.method ?? "get";
  const serverMessage = extractPrimaryServerMessage(axiosErr.response.data);
  const authSpecificMessage = classifyAuthRequestError(
    status,
    requestUrl,
    requestMethod,
    serverMessage,
  );

  if (authSpecificMessage) {
    return authSpecificMessage;
  }

  switch (true) {
    case status === 400:
      return serverMessage || "Bad request. Please check your input.";
    case status === 401:
      return (
        serverMessage ||
        "Invalid credentials. Please check your email and password."
      );
    case status === 403:
      return (
        serverMessage || "You do not have permission to perform this action."
      );
    case status === 404:
      return serverMessage || "The requested resource was not found.";
    case status === 409:
      return (
        serverMessage ||
        "A conflict occurred — this resource may already exist."
      );
    case status === 422:
      return serverMessage || "Validation failed. Please check your input.";
    case status >= 500:
      return (
        serverMessage || `Server error (${status}). Please try again later.`
      );
    default:
      return serverMessage || `Request failed with status ${status}.`;
  }
}

axiosInstance.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("ping_auth");

      if (
        !window.location.pathname.startsWith("/login") &&
        !window.location.pathname.startsWith("/register") &&
        !window.location.pathname.startsWith("/reset-password")
      ) {
        window.location.href = "/login";
      }
    }
    return Promise.reject(classifyAxiosError(error));
  },
);

export default axiosInstance;
