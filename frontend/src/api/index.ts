import { authApi } from "./authApi";
import { postsApi } from "./postsApi";
import { analyticsApi } from "./analyticsApi";

export const api = {
  auth: authApi,
  posts: postsApi,
  analytics: analyticsApi,
};
