import axiosInstance from './axiosInstance';
import type { Post, PostsResponse, Comment } from '../types';
import {
  fetchPostsRaw,
  hydratePost,
  hydratePosts,
  mapCreatedOrUpdatedComment,
  type BackendCommentCreateRequest,
  resolveCategoryId,
  type BackendCommentResponse,
  type BackendPostCreateRequest,
  type BackendPostResponse,
} from './communityApi';

type BackendPostCreateResponse = BackendPostResponse | { data: BackendPostResponse };
type BackendCommentMutationResponse = BackendCommentResponse | { data: BackendCommentResponse };
type BackendPostMutationResponse = BackendPostResponse | { data: BackendPostResponse };

function unwrapCreatedPost(
  payload: BackendPostCreateResponse,
): BackendPostResponse {
  if (
    payload &&
    typeof payload === 'object' &&
    'data' in payload &&
    payload.data
  ) {
    return payload.data;
  }

  return payload as BackendPostResponse;
}

function unwrapPost(
  payload: BackendPostMutationResponse,
): BackendPostResponse {
  if (
    payload &&
    typeof payload === 'object' &&
    'data' in payload &&
    payload.data
  ) {
    return payload.data;
  }

  return payload as BackendPostResponse;
}

function unwrapComment(
  payload: BackendCommentMutationResponse,
): BackendCommentResponse {
  if (
    payload &&
    typeof payload === 'object' &&
    'data' in payload &&
    payload.data
  ) {
    return payload.data;
  }

  return payload as BackendCommentResponse;
}

export const postsApi = {
  getAll: async (params?: { category?: string; search?: string }): Promise<PostsResponse> => {
    let posts = await hydratePosts(await fetchPostsRaw());

    if (params?.category && params.category !== 'All') {
      posts = posts.filter((post) => post.category === params.category);
    }

    if (params?.search) {
      const query = params.search.toLowerCase();
      posts = posts.filter(
        (post) =>
          post.title.toLowerCase().includes(query) ||
          post.body.toLowerCase().includes(query),
      );
    }

    return { posts, total: posts.length };
  },

  getById: async (id: number): Promise<Post> => {
    const response = await axiosInstance.get<BackendPostMutationResponse>(`/posts/${id}`);
    return hydratePost(unwrapPost(response.data));
  },

  create: async (payload: {
    title: string;
    body: string;
    category: string;
    author: string;
    authorId: number;
  }): Promise<Post> => {
    const categoryId = await resolveCategoryId(payload.category as Post['category']);
    const requestBody: BackendPostCreateRequest = {
      title: payload.title,
      content: payload.body,
      categoryId,
    };

    const response = await axiosInstance.post<BackendPostCreateResponse>('/posts', requestBody);

    return hydratePost(unwrapCreatedPost(response.data));
  },

  update: async (id: number, payload: Partial<Post>): Promise<Post> => {
    const requestBody: Record<string, unknown> = {};
    if (typeof payload.title === 'string') requestBody.title = payload.title;
    if (typeof payload.body === 'string') requestBody.content = payload.body;
    if (payload.category) {
      requestBody.categoryId = await resolveCategoryId(payload.category);
    }

    const response = await axiosInstance.put<BackendPostMutationResponse>(`/posts/${id}`, requestBody);
    return hydratePost(unwrapPost(response.data));
  },

  delete: async (id: number) => {
    await axiosInstance.delete(`/posts/${id}`);
  },

  addComment: async (
    postId: number,
    payload: { text: string; author: string; authorId: number },
  ): Promise<Comment> => {
    const requestBody: BackendCommentCreateRequest = {
      postId,
      content: payload.text,
      parentCommentId: null,
    };
    const response = await axiosInstance.post<BackendCommentMutationResponse>('/comments', requestBody);
    const rawComment = unwrapComment(response.data);

    return mapCreatedOrUpdatedComment(rawComment, payload.author);
  },

  updateComment: async (_postId: number, commentId: number, text: string) => {
    const response = await axiosInstance.put<BackendCommentMutationResponse>(
      `/comments/${commentId}`,
      { content: text },
    );
    const rawComment = unwrapComment(response.data);

    return mapCreatedOrUpdatedComment(rawComment);
  },

  deleteComment: async (_postId: number, commentId: number) => {
    await axiosInstance.delete(`/comments/${commentId}`);
  },
};
