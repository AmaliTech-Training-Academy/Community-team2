import { create } from "zustand";
import type { Post, PostFilters } from "../../types";
import { api } from "../../api/index";

const listRequests = new Map<string, Promise<void>>();
const detailRequests = new Map<number, Promise<void>>();

function createFiltersKey(filters: PostFilters): string {
  return JSON.stringify(filters);
}

function hasFilterChanges(
  current: PostFilters,
  next: Partial<PostFilters>,
): boolean {
  return (Object.keys(next) as Array<keyof PostFilters>).some(
    (key) => current[key] !== next[key],
  );
}

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
  updatePost: (
    id: number,
    payload: Partial<Post> & { imageFile?: File },
  ) => Promise<void>;
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
    const filters = get().filters;
    const requestKey = createFiltersKey(filters);
    const existingRequest = listRequests.get(requestKey);

    if (existingRequest) {
      return existingRequest;
    }

    set({ listLoading: true });

    const request = (async () => {
      try {
        const data = await api.posts.getAll(filters);

        if (createFiltersKey(get().filters) === requestKey) {
          set({ posts: data.posts });
        }
      } finally {
        listRequests.delete(requestKey);

        if (listRequests.size === 0) {
          set({ listLoading: false });
        }
      }
    })();

    listRequests.set(requestKey, request);

    return request;
  },

  fetchPost: async (id) => {
    const existingRequest = detailRequests.get(id);

    if (existingRequest) {
      return existingRequest;
    }

    set({ detailLoading: true });

    const request = (async () => {
      try {
        const post = await api.posts.getById(id);
        set({ currentPost: post });
      } finally {
        detailRequests.delete(id);

        if (detailRequests.size === 0) {
          set({ detailLoading: false });
        }
      }
    })();

    detailRequests.set(id, request);

    return request;
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

  setFilters: (f) =>
    set((s) => {
      if (!hasFilterChanges(s.filters, f)) {
        return s;
      }

      return { filters: { ...s.filters, ...f } };
    }),
}));
