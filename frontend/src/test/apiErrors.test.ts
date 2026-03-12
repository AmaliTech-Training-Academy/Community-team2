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
  it("maps generic login validation errors to invalid credentials", () => {
    const error = createAxiosError({
      status: 400,
      data: { message: "Validation failed" },
      url: "/users/login",
    });

    expect(classifyAxiosError(error)).toBe(
      "Invalid email or password. Please try again.",
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
