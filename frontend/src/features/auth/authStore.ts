import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { User } from "../../types";
import { api } from "../../api/index";
import { isTokenValid } from "../../utils";

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (
    username: string,
    email: string,
    password: string,
  ) => Promise<void>;
  logout: () => void;
  rehydrateAndValidate: () => void;
}

const CLEARED: Pick<AuthState, "user" | "token" | "isAuthenticated"> = {
  user: null,
  token: null,
  isAuthenticated: false,
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      ...CLEARED,

      login: async (email, password) => {
        const { token, user } = await api.auth.login(email, password);
        set({ user, token, isAuthenticated: true });
      },

      register: async (username, email, password) => {
        const { token, user } = await api.auth.register(
          username,
          email,
          password,
        );
        set({ user, token, isAuthenticated: true });
      },

      logout: () => set(CLEARED),

      rehydrateAndValidate: () => {
        const { token } = get();
        if (!isTokenValid(token)) set(CLEARED);
      },
    }),
    {
      name: "ping_auth",
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
);
