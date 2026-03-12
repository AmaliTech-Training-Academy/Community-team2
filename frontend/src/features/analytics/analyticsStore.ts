import { create } from "zustand";
import type { Analytics } from "../../types";
import { api } from "../../api/index";

let analyticsRequest: Promise<void> | null = null;

interface AnalyticsState {
  data: Analytics | null;
  loading: boolean;
  fetch: () => Promise<void>;
}

export const useAnalyticsStore = create<AnalyticsState>((set, get) => ({
  data: null,
  loading: false,
  fetch: async () => {
    if (get().data) return;
    if (analyticsRequest) return analyticsRequest;

    set({ loading: true });

    analyticsRequest = (async () => {
      try {
        const data = await api.analytics.get();
        set({ data });
      } finally {
        analyticsRequest = null;
        set({ loading: false });
      }
    })();

    return analyticsRequest;
  },
}));
