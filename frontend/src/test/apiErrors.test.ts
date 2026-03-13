import type { AxiosError } from "axios";
import { classifyAxiosError } from "../api/axiosInstance";

function createAxiosError({
  status,
  data,
  url,
  method = "post",
  code,
}: {
  status?: number;
  data?: unknown;
  url: string;
  method?: string;
  code?: string;
}): AxiosError {
  return {
    isAxiosError: true,
    code,
    config: {
      url,
      method,
      headers: {},
    },
    response:
      typeof status === "number"
        ? {
            status,
            statusText: "Request failed",
            headers: {},
            config: {
              url,
              method,
              headers: {},
            },
            data,
          }
        : undefined,
  } as AxiosError;
}

describe("classifyAxiosError", () => {
  it("maps login network failures to a user-friendly login service message", () => {
    const error = createAxiosError({
      code: "ERR_NETWORK",
      url: "/users/login",
    });

    expect(classifyAxiosError(error)).toBe(
      "Unable to connect to the login service. Please ensure you're online or try again shortly.",
    );
  });

  it("maps generic login validation errors to invalid credentials", () => {
    const error = createAxiosError({
      status: 400,
      data: { message: "Validation failed" },
      url: "/users/login",
    });

    expect(classifyAxiosError(error)).toBe(
      "We couldn't sign you in. Check your email and password, then try again.",
    );
  });

  it("maps generic registration conflicts to duplicate account guidance", () => {
    const error = createAxiosError({
      status: 409,
      data: { error: "Conflict" },
      url: "/users",
    });

    expect(classifyAxiosError(error)).toBe(
      "An account with this email already exists. Try logging in instead.",
    );
  });

  it("maps registration service outages to a friendly message", () => {
    const error = createAxiosError({
      status: 404,
      data: "<!DOCTYPE html><html><body>The endpoint is offline. (ERR_NGROK_3200)</body></html>",
      url: "/users",
    });

    expect(classifyAxiosError(error)).toBe(
      "Registration is temporarily unavailable. Please try again in a moment.",
    );
  });

  it("surfaces field-level auth validation messages when the backend provides them", () => {
    const error = createAxiosError({
      status: 422,
      data: {
        errors: [{ field: "email", message: "must be a valid email address" }],
      },
      url: "/users/forgot-password",
    });

    expect(classifyAxiosError(error)).toBe(
      "Email: must be a valid email address",
    );
  });

  it("maps forgot-password service outages to a friendly message", () => {
    const error = createAxiosError({
      status: 503,
      data: "<!DOCTYPE html><html><body>ngrok error</body></html>",
      url: "/users/forgot-password",
    });

    expect(classifyAxiosError(error)).toBe(
      "Password reset is temporarily unavailable. Please try again in a moment.",
    );
  });

  it("maps generic reset authorization failures to an expired link message", () => {
    const error = createAxiosError({
      status: 401,
      data: { message: "Validation failed" },
      url: "/users/42",
      method: "put",
    });

    expect(classifyAxiosError(error)).toBe(
      "This reset link is invalid or has expired. Request a new one and try again.",
    );
  });
});
