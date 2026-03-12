export type UserRole = "ADMIN" | "USER";

export interface User {
  id: number;
  email: string;
  name: string;
  role: UserRole;
}

export type Category = string;

export interface Comment {
  id: number;
  text: string;
  author: string;
  authorId: number;
  createdAt: string;
}

export interface Post {
  id: number;
  title: string;
  body: string;
  category: Category;
  author: string;
  authorId: number;
  createdAt: string;
  comments: Comment[];

  imageUrl?: string;
}

export interface PostsResponse {
  posts: Post[];
  total: number;
}

export interface PostFilters {
  title?: string;
  content?: string;
  categoryId?: number;
  authorId?: number;
  createdAfter?: string;
  createdBefore?: string;
  minViews?: number;
  maxViews?: number;
  category?: Category | "All";
}

export interface Analytics {
  totalPosts: number;

  totalComments?: number;
  totalUsers: number;
  categoryBreakdown: Record<string, number>;
  dayActivity: { day: string; count: number }[];
  postTrend?: { date: string; count: number }[];
  topContributors: { name: string; count: number }[];
}

export interface AuthResponse {
  token: string;
  user?: User;
  accessToken?: string;
  jwt?: string;
}

export interface Toast {
  id: number;
  message: string;
  type: "success" | "error" | "warning";
}

export interface Subscription {
  email: string;

  categories: Category[];

  enabled: boolean;
}
