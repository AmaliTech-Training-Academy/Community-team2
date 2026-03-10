import { create } from 'zustand';
import type { Post, PostFilters } from '../../types';
import { api } from '../../api/index';
import { getMockNotificationMessages } from '../subscriptions/notificationService';

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true';

// Toast is accessed imperatively so we don't couple the store to React context.
// The store emits notification messages via this registry — PostModal picks
// them up after createPost resolves.
type NotificationListener = (messages: string[]) => void;
let notificationListener: NotificationListener | null = null;

export function registerNotificationListener(fn: NotificationListener) {
  notificationListener = fn;
  return () => { notificationListener = null; };
}

interface PostsState {
  posts: Post[];
  currentPost: Post | null;
  filters: PostFilters;
  listLoading: boolean;
  detailLoading: boolean;
  fetchPosts:  () => Promise<void>;
  fetchPost:   (id: number) => Promise<void>;
  createPost:  (payload: Omit<Post, 'id' | 'createdAt' | 'comments'>) => Promise<Post>;
  updatePost:  (id: number, payload: Partial<Post>) => Promise<void>;
  deletePost:  (id: number) => Promise<void>;
  setFilters:  (f: Partial<PostFilters>) => void;
}

export const usePostsStore = create<PostsState>((set, get) => ({
  posts: [],
  currentPost: null,
  filters: { category: 'All', search: '' },
  listLoading: false,
  detailLoading: false,

  fetchPosts: async () => {
    set({ listLoading: true });
    try {
      const { category, search } = get().filters;
      const params: Record<string, string> = {};
      if (category && category !== 'All') params.category = category;
      if (search) params.search = search;
      const data = await api.posts.getAll(params);
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
    set(s => ({ posts: [post, ...s.posts] }));

    // In mock mode, simulate email notifications as in-app toasts
    if (USE_MOCK && notificationListener) {
      // Derive the current user's email from the stored auth state
      const raw = localStorage.getItem('ping_auth');
      const currentUserEmail: string =
        raw ? (JSON.parse(raw)?.state?.user?.email ?? '') : '';
      const messages = getMockNotificationMessages(post, currentUserEmail);
      if (messages.length > 0) {
        notificationListener(messages);
      }
    }

    return post;
  },

  updatePost: async (id, payload) => {
    const updated = await api.posts.update(id, payload);
    set(s => ({
      posts: s.posts.map(p => p.id === id ? updated : p),
      currentPost: s.currentPost?.id === id ? updated : s.currentPost,
    }));
  },

  deletePost: async (id) => {
    await api.posts.delete(id);
    set(s => ({ posts: s.posts.filter(p => p.id !== id) }));
  },

  setFilters: (f) => set(s => ({ filters: { ...s.filters, ...f } })),
}));
