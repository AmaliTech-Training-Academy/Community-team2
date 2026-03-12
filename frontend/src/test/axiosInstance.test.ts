import axios, { AxiosError } from "axios";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

describe("axiosInstance request interceptor", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.resetModules();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("attaches the persisted bearer token without reading storage during the request", async () => {
    localStorage.setItem(
      "ping_auth",
      JSON.stringify({
        state: {
          token: "tok123",
          isAuthenticated: true,
          user: { id: 1, email: "a@b.com", name: "Alice", role: "USER" },
        },
      }),
    );

    const { default: axiosInstance } = await import("../api/axiosInstance");
    const getItemSpy = vi.spyOn(Storage.prototype, "getItem");

    const response = await axiosInstance.get("/posts", {
      adapter: async (config) => ({
        data: null,
        status: 200,
        statusText: "OK",
        headers: {},
        config,
      }),
    });

    expect(getItemSpy).not.toHaveBeenCalled();
    expect(response.config.headers.Authorization).toBe("Bearer tok123");
  });

  it("maps ngrok/html login failures to a friendly message", async () => {
    const { API_BASE_URL, classifyAxiosError } =
      await import("../api/axiosInstance");

    const error = new AxiosError(
      "Request failed with status code 404",
      "ERR_BAD_REQUEST",
      {
        url: "/users/login",
        method: "post",
        baseURL: API_BASE_URL,
        headers: axios.AxiosHeaders.from({}),
      },
      undefined,
      {
        status: 404,
        statusText: "Not Found",
        headers: {},
        config: {
          url: "/users/login",
          method: "post",
          baseURL: API_BASE_URL,
          headers: axios.AxiosHeaders.from({}),
        },
        data: "<!DOCTYPE html><html><body>The endpoint is offline. (ERR_NGROK_3200)</body></html>",
      },
    );

    expect(classifyAxiosError(error)).toBe(
      "Login is temporarily unavailable. Please try again in a moment.",
    );
  });
});
