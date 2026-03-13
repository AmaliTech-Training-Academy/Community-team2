import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { User } from "../../types";
import { api } from "../../api/index";
import { isTokenValid } from "../../utils";
import { clearPersistedAuthSession, setAuthSession } from "./authSession";

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
        const nextState = { user, token, isAuthenticated: true };
        set(nextState);
        setAuthSession(nextState);
      },

      register: async (username, email, password) => {
        const { token, user } = await api.auth.register(
          username,
          email,
          password,
        );
        const nextState = { user, token, isAuthenticated: true };
        set(nextState);
        setAuthSession(nextState);
      },

      logout: () => {
        clearPersistedAuthSession();
        set(CLEARED);
      },

      rehydrateAndValidate: () => {
        const currentState = get();

        if (!isTokenValid(currentState.token)) {
          clearPersistedAuthSession();
          set(CLEARED);
          return;
        }

        setAuthSession(currentState);
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
