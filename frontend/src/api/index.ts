/**
 * ─────────────────────────────────────────────────────────────────────────────
 *  API Layer — Single toggle between real backend and mock service
 *
 *  Set VITE_USE_MOCK=true in .env (or .env.local) to use the in-memory mock.
 *  Leave unset or set to false to use the real Spring Boot backend at :8080.
 *
 *  Every page/store imports from HERE — never directly from authApi, postsApi,
 *  or analyticsApi.  That way swapping backends is a one-line .env change.
 * ─────────────────────────────────────────────────────────────────────────────
 */

import { authApi }      from './authApi';
import { postsApi }     from './postsApi';
import { analyticsApi } from './analyticsApi';
import { mockAuthApi, mockPostsApi, mockAnalyticsApi } from '../mocks/mockService';

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true';

export const api = {
  auth:      USE_MOCK ? mockAuthApi      : authApi,
  posts:     USE_MOCK ? mockPostsApi     : postsApi,
  analytics: USE_MOCK ? mockAnalyticsApi : analyticsApi,
};

export { USE_MOCK };
