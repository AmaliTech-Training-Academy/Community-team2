/**
 * ─────────────────────────────────────────────────────────────────────────────
 *  PING — In-Memory Mock Service
 *
 *  A complete drop-in replacement for the real axios-based API layer.
 *  All CRUD operations run against a live in-memory store seeded from mockData.ts
 *  so navigation, create, edit, delete, and comments all work end-to-end
 *  without any backend.
 *
 *  HOW TO ACTIVATE (toggle in one place — see api/index.ts):
 *    VITE_USE_MOCK=true   →  uses this service
 *    VITE_USE_MOCK=false  →  uses the real axiosInstance (default)
 * ─────────────────────────────────────────────────────────────────────────────
 */

import type { User, Post, PostsResponse, Comment, Analytics } from "../types";
import {
  MOCK_POSTS,
  MOCK_AUTH_RESPONSES,
  MOCK_USERS,
  makeFakeJwt,
} from "./mockData";

// ── Simulated network latency (ms) ───────────────────────────────────────────
const DELAY = 350;
const delay = () => new Promise<void>((res) => setTimeout(res, DELAY));

// ── In-memory state (mutated by CRUD ops, reset on page reload) ──────────────
let posts: Post[] = structuredClone(MOCK_POSTS);
let nextPostId = posts.reduce((m, p) => Math.max(m, p.id), 0) + 1;
let nextCommentId =
  posts.flatMap((p) => p.comments).reduce((m, c) => Math.max(m, c.id), 0) + 1;

// ── Helper — raise a mock HTTP error ─────────────────────────────────────────
function mockError(message: string, status = 400): never {
  const err = new Error(message) as Error & { status: number };
  err.status = status;
  throw err;
}

// ── Auth ──────────────────────────────────────────────────────────────────────
export const mockAuthApi = {
  /**
   * Accepts any email that exists in MOCK_AUTH_RESPONSES.
   * Password is not validated — any non-empty string is accepted (mock only).
   * To test a wrong-password scenario, use password = "wrong".
   */
  login: async (
    email: string,
    password: string,
  ): Promise<{ token: string; user: User }> => {
    await delay();
    if (!password) mockError("Password is required.", 400);
    if (password === "wrong")
      mockError(
        "Invalid credentials. Please check your email and password.",
        401,
      );
    const record = MOCK_AUTH_RESPONSES[email.toLowerCase()];
    if (!record) mockError("No account found with that email address.", 401);
    // Regenerate a fresh token each login so the exp is always 24 h from now
    const freshToken = makeFakeJwt(record.user);
    return { token: freshToken, user: record.user };
  },

  register: async (
    fullName: string,
    email: string,
    password: string,
  ): Promise<{ token: string; user: User }> => {
    await delay();
    if (!fullName.trim()) mockError("Full name is required.");
    if (!email) mockError("Email is required.");
    if (password.length < 8)
      mockError("Password must be at least 8 characters.");
    if (MOCK_AUTH_RESPONSES[email.toLowerCase()]) {
      mockError("An account with this email already exists.", 409);
    }
    const newUser: User = {
      id: MOCK_USERS.length + 1,
      email: email.toLowerCase(),
      name: fullName,
      role: "USER",
    };
    // Register stays in-memory only — survives until page reload
    MOCK_USERS.push(newUser);
    const token = makeFakeJwt(newUser);
    MOCK_AUTH_RESPONSES[email.toLowerCase()] = { token, user: newUser };
    return { token, user: newUser };
  },
};

// ── Posts ─────────────────────────────────────────────────────────────────────
export const mockPostsApi = {
  getAll: async (params?: {
    category?: string;
    search?: string;
  }): Promise<PostsResponse> => {
    await delay();
    let result = [...posts];
    if (params?.category && params.category !== "All") {
      result = result.filter((p) => p.category === params.category);
    }
    if (params?.search) {
      const q = params.search.toLowerCase();
      result = result.filter(
        (p) =>
          p.title.toLowerCase().includes(q) || p.body.toLowerCase().includes(q),
      );
    }
    // Most recent first
    result.sort(
      (a, b) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
    );
    return { posts: result, total: result.length };
  },

  getById: async (id: number): Promise<Post> => {
    await delay();
    const post = posts.find((p) => p.id === id);
    if (!post) mockError(`Post ${id} not found.`, 404);
    return structuredClone(post!);
  },

  create: async (payload: {
    title: string;
    body: string;
    category: string;
    author: string;
    authorId: number;
    imageUrl?: string;
  }): Promise<Post> => {
    await delay();
    const post: Post = {
      id: nextPostId++,
      title: payload.title,
      body: payload.body,
      category: payload.category as Post["category"],
      author: payload.author,
      authorId: payload.authorId,
      createdAt: new Date().toISOString(),
      comments: [],
      imageUrl: payload.imageUrl,
    };
    posts.unshift(post);
    return structuredClone(post);
  },

  update: async (id: number, payload: Partial<Post>): Promise<Post> => {
    await delay();
    const idx = posts.findIndex((p) => p.id === id);
    if (idx === -1) mockError(`Post ${id} not found.`, 404);
    posts[idx] = {
      ...posts[idx],
      ...payload,
      id,
      comments: posts[idx].comments,
    };
    return structuredClone(posts[idx]);
  },

  delete: async (id: number): Promise<void> => {
    await delay();
    const idx = posts.findIndex((p) => p.id === id);
    if (idx === -1) mockError(`Post ${id} not found.`, 404);
    posts.splice(idx, 1);
  },

  addComment: async (
    postId: number,
    payload: { text: string; author: string; authorId: number },
  ): Promise<Comment> => {
    await delay();
    const post = posts.find((p) => p.id === postId);
    if (!post) mockError(`Post ${postId} not found.`, 404);
    const comment: Comment = {
      id: nextCommentId++,
      text: payload.text,
      author: payload.author,
      authorId: payload.authorId,
      createdAt: new Date().toISOString(),
    };
    post!.comments.push(comment);
    return structuredClone(comment);
  },

  updateComment: async (
    postId: number,
    commentId: number,
    text: string,
  ): Promise<Comment> => {
    await delay();
    const post = posts.find((p) => p.id === postId);
    if (!post) mockError(`Post ${postId} not found.`, 404);
    const comment = post!.comments.find((c) => c.id === commentId);
    if (!comment) mockError(`Comment ${commentId} not found.`, 404);
    comment!.text = text;
    return structuredClone(comment!);
  },

  deleteComment: async (postId: number, commentId: number): Promise<void> => {
    await delay();
    const post = posts.find((p) => p.id === postId);
    if (!post) mockError(`Post ${postId} not found.`, 404);
    const idx = post!.comments.findIndex((c) => c.id === commentId);
    if (idx === -1) mockError(`Comment ${commentId} not found.`, 404);
    post!.comments.splice(idx, 1);
  },
};

// ── Analytics ─────────────────────────────────────────────────────────────────
export const mockAnalyticsApi = {
  /**
   * Derives live analytics from the current in-memory posts state so numbers
   * stay accurate even after create / delete operations in the same session.
   */
  get: async (): Promise<Analytics> => {
    await delay();

    const totalComments = posts.reduce(
      (sum, p) => sum + (p.comments?.length ?? 0),
      0,
    );

    const breakdown: Record<string, number> = {
      News: 0,
      Events: 0,
      Discussion: 0,
      Alert: 0,
    };
    posts.forEach((p) => {
      breakdown[p.category] = (breakdown[p.category] ?? 0) + 1;
    });

    const dayMap: Record<string, number> = {
      Mon: 0,
      Tue: 0,
      Wed: 0,
      Thu: 0,
      Fri: 0,
      Sat: 0,
      Sun: 0,
    };
    const dayNames = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
    posts.forEach((p) => {
      const d = dayNames[new Date(p.createdAt).getDay()];
      dayMap[d]++;
    });
    const dayActivity = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"].map(
      (day) => ({
        day,
        count: dayMap[day],
      }),
    );

    // Last-30-day trend derived from real post dates
    const trendMap: Record<string, number> = {};
    for (let i = 29; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      trendMap[d.toISOString().slice(0, 10)] = 0;
    }
    posts.forEach((p) => {
      const dateKey = p.createdAt.slice(0, 10);
      if (dateKey in trendMap) trendMap[dateKey]++;
    });
    const postTrend = Object.entries(trendMap).map(([date, count]) => ({
      date,
      count,
    }));

    // Top contributors sorted by post count
    const contribMap: Record<string, number> = {};
    posts.forEach((p) => {
      contribMap[p.author] = (contribMap[p.author] ?? 0) + 1;
    });
    const topContributors = Object.entries(contribMap)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5)
      .map(([name, count]) => ({ name, count }));

    return {
      totalPosts: posts.length,
      totalComments,
      totalUsers: MOCK_USERS.length,
      categoryBreakdown: breakdown,
      dayActivity,
      postTrend,
      topContributors,
    };
  },
};
