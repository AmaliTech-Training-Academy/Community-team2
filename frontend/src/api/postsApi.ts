import axiosInstance from './axiosInstance';
import type { Post, PostsResponse, Comment } from '../types';

export const postsApi = {
  getAll: (params?: { category?: string; search?: string }) =>
    axiosInstance.get<PostsResponse>('/posts', { params }).then(r => r.data),

  getById: (id: number) =>
    axiosInstance.get<Post>(`/posts/${id}`).then(r => r.data),

  create: (payload: { title: string; body: string; category: string; author: string; authorId: number }) =>
    axiosInstance.post<Post>('/posts', payload).then(r => r.data),

  update: (id: number, payload: Partial<Post>) =>
    axiosInstance.put<Post>(`/posts/${id}`, payload).then(r => r.data),

  delete: (id: number) =>
    axiosInstance.delete(`/posts/${id}`).then(r => r.data),

  addComment: (postId: number, payload: { text: string; author: string; authorId: number }) =>
    axiosInstance.post<Comment>(`/posts/${postId}/comments`, payload).then(r => r.data),

  updateComment: (postId: number, commentId: number, text: string) =>
    axiosInstance.put<Comment>(`/posts/${postId}/comments/${commentId}`, { text }).then(r => r.data),

  deleteComment: (postId: number, commentId: number) =>
    axiosInstance.delete(`/posts/${postId}/comments/${commentId}`).then(r => r.data),
};
