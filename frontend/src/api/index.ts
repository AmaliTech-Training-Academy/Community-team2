

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
