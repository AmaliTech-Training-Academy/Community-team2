import type { User } from "../../types";

export interface AuthSessionSnapshot {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
}

type PersistedAuthState = {
  state?: Partial<AuthSessionSnapshot>;
};

const EMPTY_AUTH_SESSION: AuthSessionSnapshot = {
  user: null,
  token: null,
  isAuthenticated: false,
};

function normalizeAuthSession(
  snapshot?: Partial<AuthSessionSnapshot> | null,
): AuthSessionSnapshot {
  return {
    user: snapshot?.user ?? null,
    token: snapshot?.token ?? null,
    isAuthenticated: Boolean(snapshot?.isAuthenticated && snapshot?.token),
  };
}

function readPersistedAuthSession(): AuthSessionSnapshot {
  if (typeof window === "undefined") {
    return EMPTY_AUTH_SESSION;
  }

  try {
    const raw = localStorage.getItem("ping_auth");
    if (!raw) {
      return EMPTY_AUTH_SESSION;
    }

    const parsed = JSON.parse(raw) as PersistedAuthState;
    return normalizeAuthSession(parsed.state);
  } catch {
    return EMPTY_AUTH_SESSION;
  }
}

let authSession = readPersistedAuthSession();

export function getAuthSession(): AuthSessionSnapshot {
  return authSession;
}

export function getAuthToken(): string | null {
  return authSession.token;
}

export function getAuthUser(): User | null {
  return authSession.user;
}

export function setAuthSession(
  snapshot?: Partial<AuthSessionSnapshot> | null,
): AuthSessionSnapshot {
  authSession = normalizeAuthSession(snapshot);
  return authSession;
}

export function loadAuthSessionFromStorage(): AuthSessionSnapshot {
  authSession = readPersistedAuthSession();
  return authSession;
}

export function clearPersistedAuthSession(): void {
  authSession = EMPTY_AUTH_SESSION;

  if (typeof window !== "undefined") {
    localStorage.removeItem("ping_auth");
  }
}
