import { create } from "zustand";
import { fetchCategoryNames } from "../../api/communityApi";
import type { Category } from "../../types";

let categoriesRequest: Promise<void> | null = null;

interface CategoriesState {
  categories: Category[];
  loading: boolean;
  fetch: () => Promise<void>;
}

export const useCategoriesStore = create<CategoriesState>((set, get) => ({
  categories: [],
  loading: false,
  fetch: async () => {
    if (get().categories.length > 0) return;
    if (categoriesRequest) return categoriesRequest;

    set({ loading: true });

    categoriesRequest = (async () => {
      try {
        const categories = await fetchCategoryNames();
        set((state) => (state.categories.length > 0 ? state : { categories }));
      } finally {
        categoriesRequest = null;
        set({ loading: false });
      }
    })();

    return categoriesRequest;
  },
}));
