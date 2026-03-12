import axiosInstance from "./axiosInstance";
import type { AuthResponse, User, UserRole } from "../types";
import { decodeJwt } from "../utils";
import {
  mapUser,
  type BackendAuthResponse,
  type BackendUserResponse,
} from "./communityApi";

interface WrappedBackendAuthResponse {
  status?: string;
  message?: string;
  data?: BackendAuthResponse;
}

function mapBackendRole(role?: string): UserRole {
  return role === "ADMIN" ? "ADMIN" : "USER";
}

function unwrapBackendAuthResponse(
  raw: BackendAuthResponse | WrappedBackendAuthResponse,
): BackendAuthResponse {
  if (
    raw &&
    typeof raw === "object" &&
    "data" in raw &&
    raw.data &&
    typeof raw.data === "object"
  ) {
    return raw.data;
  }

  return raw as BackendAuthResponse;
}

function normaliseAuthResponse(raw: AuthResponse): {
  token: string;
  user: User;
} {
  const token = raw.token || raw.accessToken || raw.jwt || "";

  if (!token) {
    throw new Error("Server returned a response with no token.");
  }

  if (raw.user) {
    return { token, user: raw.user };
  }

  const payload = decodeJwt(token);
  if (!payload) {
    throw new Error("Server returned a token that could not be decoded.");
  }

  let role: UserRole = "USER";
  if (typeof payload.role === "string") {
    role = mapBackendRole(payload.role.replace(/^ROLE_/, ""));
  } else if (Array.isArray(payload.roles) && payload.roles.length > 0) {
    role = mapBackendRole(String(payload.roles[0]).replace(/^ROLE_/, ""));
  } else if (
    Array.isArray(payload.authorities) &&
    payload.authorities.length > 0
  ) {
    role = mapBackendRole(String(payload.authorities[0]).replace(/^ROLE_/, ""));
  }

  const user: User = {
    id: typeof payload.id === "number" ? payload.id : Number(payload.sub) || 0,
    email:
      typeof payload.email === "string" ? payload.email : (payload.sub ?? ""),
    name:
      typeof payload.name === "string"
        ? payload.name
        : ((payload.email as string) ?? payload.sub ?? ""),
    role,
  };

  return { token, user };
}

async function fetchCurrentUser(): Promise<User | null> {
  try {
    const response = await axiosInstance.get<BackendUserResponse>("/users/me");
    return mapUser(response.data);
  } catch {
    return null;
  }
}

async function normaliseBackendAuthResponse(
  raw: BackendAuthResponse | WrappedBackendAuthResponse,
): Promise<{ token: string; user: User }> {
  const payload = unwrapBackendAuthResponse(raw);
  const base = normaliseAuthResponse(payload as AuthResponse);

  if (payload.user) {
    return { token: base.token, user: mapUser(payload.user) };
  }

  const currentUser = await fetchCurrentUser();
  if (currentUser) {
    return { token: base.token, user: currentUser };
  }

  return base;
}

export const authApi = {
  login: async (email: string, password: string) => {
    const res = await axiosInstance.post<
      BackendAuthResponse | WrappedBackendAuthResponse
    >("/users/login", { email, password });
    return normaliseBackendAuthResponse(res.data);
  },

  updatePassword: async (
    userId: number,
    password: string,
    bearerToken?: string,
  ): Promise<void> => {
    await axiosInstance.put(
      `/users/${userId}`,
      { password },
      bearerToken
        ? {
            headers: {
              Authorization: `Bearer ${bearerToken}`,
            },
          }
        : undefined,
    );
  },

  forgotPassword: async (email: string): Promise<void> => {
    await axiosInstance.post("/users/forgot-password", { email });
  },

  register: async (fullName: string, email: string, password: string) => {
    // Send multiple name fields to accommodate backend variants (username/name/fullName).
    // This is a defensive change to improve compatibility while we verify the backend's
    // expected payload shape. If backend expects a different field, remove the extra
    // keys once confirmed.
    await axiosInstance.post("/users", {
      username: fullName,
      name: fullName,
      fullName,
      email,
      password,
      role: "MEMBER",
    });

    return authApi.login(email, password);
  },
};
