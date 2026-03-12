import { create } from "zustand";
import type { Post, PostFilters } from "../../types";
import { api } from "../../api/index";

type CreatePostInput = Omit<Post, "id" | "createdAt" | "comments"> & {
  imageFile?: File;
};

interface PostsState {
  posts: Post[];
  currentPost: Post | null;
  filters: PostFilters;
  listLoading: boolean;
  detailLoading: boolean;
  fetchPosts: () => Promise<void>;
  fetchPost: (id: number) => Promise<void>;
  createPost: (payload: CreatePostInput) => Promise<Post>;
  updatePost: (id: number, payload: Partial<Post>) => Promise<void>;
  deletePost: (id: number) => Promise<void>;
  setFilters: (f: Partial<PostFilters>) => void;
}

export const usePostsStore = create<PostsState>((set, get) => ({
  posts: [],
  currentPost: null,
  filters: { category: "All", title: "" },
  listLoading: false,
  detailLoading: false,

  fetchPosts: async () => {
    set({ listLoading: true });
    try {
      const data = await api.posts.getAll(get().filters);
      set({ posts: data.posts });
    } finally {
      set({ listLoading: false });
    }
  },

  fetchPost: async (id) => {
    set({ detailLoading: true });
    try {
      const post = await api.posts.getById(id);
      set({ currentPost: post });
    } finally {
      set({ detailLoading: false });
    }
  },

  createPost: async (payload) => {
    const post = await api.posts.create(payload);
    set((s) => ({ posts: [post, ...s.posts] }));

    return post;
  },

  updatePost: async (id, payload) => {
    const updated = await api.posts.update(id, payload);
    set((s) => ({
      posts: s.posts.map((p) => (p.id === id ? updated : p)),
      currentPost: s.currentPost?.id === id ? updated : s.currentPost,
    }));
  },

  deletePost: async (id) => {
    await api.posts.delete(id);
    set((s) => ({ posts: s.posts.filter((p) => p.id !== id) }));
  },

  setFilters: (f) => set((s) => ({ filters: { ...s.filters, ...f } })),
}));
