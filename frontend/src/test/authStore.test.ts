import { api } from "../api/index";
import { useAuthStore } from "../features/auth/authStore";

vi.mock("../api/index", () => ({
  api: {
    auth: {
      login: vi.fn(),
      register: vi.fn(),
    },
  },
}));

function encodeBase64Url(value: string) {
  return btoa(value)
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/g, "");
}

function createJwt(payload: Record<string, unknown>) {
  return `${encodeBase64Url(JSON.stringify({ alg: "HS256", typ: "JWT" }))}.${encodeBase64Url(JSON.stringify(payload))}.sig`;
}

describe("useAuthStore", () => {
  beforeEach(() => {
    localStorage.clear();
    useAuthStore.setState({ user: null, token: null, isAuthenticated: false });
    vi.clearAllMocks();
  });

  it("starts with a cleared auth state", () => {
    expect(useAuthStore.getState()).toMatchObject({
      user: null,
      token: null,
      isAuthenticated: false,
    });
  });

  it("sets auth state on login success", async () => {
    vi.mocked(api.auth.login).mockResolvedValue({
      token: "tok123",
      user: { id: 1, email: "a@b.com", name: "Alice", role: "USER" },
    });

    await useAuthStore.getState().login("a@b.com", "pass");

    expect(useAuthStore.getState()).toMatchObject({
      isAuthenticated: true,
      token: "tok123",
      user: { email: "a@b.com" },
    });
  });

  it("keeps auth state cleared on login failure", async () => {
    vi.mocked(api.auth.login).mockRejectedValue(
      new Error("Invalid credentials"),
    );

    await expect(
      useAuthStore.getState().login("a@b.com", "pass"),
    ).rejects.toThrow("Invalid credentials");

    expect(useAuthStore.getState().isAuthenticated).toBe(false);
  });

  it("clears auth state on logout", () => {
    useAuthStore.setState({
      user: { id: 1, email: "a@b.com", name: "Alice", role: "USER" },
      token: "tok123",
      isAuthenticated: true,
    });

    useAuthStore.getState().logout();

    expect(useAuthStore.getState()).toMatchObject({
      user: null,
      token: null,
      isAuthenticated: false,
    });
  });

  it("clears auth state when rehydrateAndValidate sees an expired token", () => {
    useAuthStore.setState({
      user: { id: 1, email: "a@b.com", name: "Alice", role: "USER" },
      token: createJwt({ exp: Math.floor(Date.now() / 1000) - 3600 }),
      isAuthenticated: true,
    });

    useAuthStore.getState().rehydrateAndValidate();

    expect(useAuthStore.getState().isAuthenticated).toBe(false);
  });
});
