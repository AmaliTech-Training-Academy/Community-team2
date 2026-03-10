import { create } from 'zustand';
import type { Analytics } from '../../types';
import { api } from '../../api/index';

interface AnalyticsState {
  data: Analytics | null;
  loading: boolean;
  fetch: () => Promise<void>;
}

export const useAnalyticsStore = create<AnalyticsState>((set) => ({
  data: null,
  loading: false,
  fetch: async () => {
    set({ loading: true });
    try {
      const data = await api.analytics.get();
      set({ data });
    } finally {
      set({ loading: false });
    }
  },
}));
