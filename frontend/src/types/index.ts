export type UserRole = "ADMIN" | "USER";

export interface User {
  id: number;
  email: string;
  name: string;
  role: UserRole;
}

export type Category =
  | "Events"
  | "Lost & Found"
  | "Recommendations"
  | "Help Requests"
  | "News";
export const CATEGORIES: Category[] = [
  "Events",
  "Lost & Found",
  "Recommendations",
  "Help Requests",
  "News",
];

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
  /** Optional attached image — base64 data-URI in mock, signed URL or path in production */
  imageUrl?: string;
}

export interface PostsResponse {
  posts: Post[];
  total: number;
}

export interface PostFilters {
  category?: Category | "All";
  search?: string;
}

export interface Analytics {
  totalPosts: number;
  /** Optional — if backend doesn't provide, UI can derive from posts/comments */
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

// ── Subscriptions ─────────────────────────────────────────────────────────────

/** One subscription record per user — stored in Zustand persist */
export interface Subscription {
  email: string;
  /** Categories the user wants email alerts for */
  categories: Category[];
  /** Whether email notifications are globally enabled */
  enabled: boolean;
}
