import { create } from "zustand";
import { fetchCategoryNames } from "../../api/communityApi";
import type { Category } from "../../types";

interface CategoriesState {
  categories: Category[];
  loading: boolean;
  fetch: () => Promise<void>;
}

export const useCategoriesStore = create<CategoriesState>((set, get) => ({
  categories: [],
  loading: false,
  fetch: async () => {
    if (get().loading) return;

    set({ loading: true });
    try {
      const categories = await fetchCategoryNames();
      set({ categories });
    } finally {
      set({ loading: false });
    }
  },
}));
