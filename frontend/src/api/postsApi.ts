import axiosInstance from "./axiosInstance";
import type { Post, PostsResponse, Comment, PostFilters } from "../types";
import {
  hydratePost,
  hydratePosts,
  mapCreatedOrUpdatedComment,
  type BackendCommentCreateRequest,
  fetchCategories,
  type BackendPage,
  type ResponseDto,
  resolveCategoryId,
  type BackendCommentResponse,
  type BackendPostCreateRequest,
  type BackendPostResponse,
} from "./communityApi";

type BackendPostCreateResponse =
  | BackendPostResponse
  | { data: BackendPostResponse };
type BackendCommentMutationResponse =
  | BackendCommentResponse
  | { data: BackendCommentResponse };
type BackendPostMutationResponse =
  | BackendPostResponse
  | { data: BackendPostResponse };
type BackendPostsResponse =
  | BackendPostResponse[]
  | BackendPage<BackendPostResponse>
  | ResponseDto<BackendPage<BackendPostResponse>>;

function unwrapPage<T>(
  payload: T[] | BackendPage<T> | ResponseDto<BackendPage<T>>,
): T[] {
  if (Array.isArray(payload)) {
    return payload;
  }

  if (
    payload &&
    typeof payload === "object" &&
    "data" in payload &&
    payload.data &&
    typeof payload.data === "object"
  ) {
    return payload.data.content ?? [];
  }

  return (payload as BackendPage<T>).content ?? [];
}

async function resolveFilterCategoryId(
  filters?: PostFilters,
): Promise<number | undefined> {
  if (typeof filters?.categoryId === "number") {
    return filters.categoryId;
  }

  if (!filters?.category || filters.category === "All") {
    return undefined;
  }

  const categories = await fetchCategories();
  const match = categories.find(
    (item) =>
      item.name.trim().toLowerCase() === filters.category?.trim().toLowerCase(),
  );

  if (match) {
    return match.id;
  }

  return resolveCategoryId(filters.category);
}

async function buildPostFilterParams(
  filters?: PostFilters,
): Promise<Record<string, string | number>> {
  const categoryId = await resolveFilterCategoryId(filters);
  const params: Record<string, string | number> = {};

  if (filters?.title?.trim()) params.title = filters.title.trim();
  if (filters?.content?.trim()) params.content = filters.content.trim();
  if (typeof categoryId === "number") params.categoryId = categoryId;
  if (typeof filters?.authorId === "number") params.authorId = filters.authorId;
  if (filters?.createdAfter?.trim())
    params.createdAfter = filters.createdAfter.trim();
  if (filters?.createdBefore?.trim())
    params.createdBefore = filters.createdBefore.trim();
  if (typeof filters?.minViews === "number") params.minViews = filters.minViews;
  if (typeof filters?.maxViews === "number") params.maxViews = filters.maxViews;

  return params;
}

function unwrapCreatedPost(
  payload: BackendPostCreateResponse,
): BackendPostResponse {
  if (
    payload &&
    typeof payload === "object" &&
    "data" in payload &&
    payload.data
  ) {
    return payload.data;
  }

  return payload as BackendPostResponse;
}

function unwrapPost(payload: BackendPostMutationResponse): BackendPostResponse {
  if (
    payload &&
    typeof payload === "object" &&
    "data" in payload &&
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
    typeof payload === "object" &&
    "data" in payload &&
    payload.data
  ) {
    return payload.data;
  }

  return payload as BackendCommentResponse;
}

export const postsApi = {
  getAll: async (filters?: PostFilters): Promise<PostsResponse> => {
    const response = await axiosInstance.get<BackendPostsResponse>("/posts", {
      params: await buildPostFilterParams(filters),
    });
    const posts = await hydratePosts(unwrapPage(response.data));

    return { posts, total: posts.length };
  },

  getById: async (id: number): Promise<Post> => {
    const response = await axiosInstance.get<BackendPostMutationResponse>(
      `/posts/${id}`,
    );
    return hydratePost(unwrapPost(response.data));
  },

  create: async (payload: {
    title: string;
    body: string;
    category: string;
    author: string;
    authorId: number;
    imageFile?: File;
    imageUrl?: string;
  }): Promise<Post> => {
    const categoryId = await resolveCategoryId(
      payload.category as Post["category"],
    );
    const requestBody: BackendPostCreateRequest = {
      title: payload.title,
      content: payload.body,
      categoryId,
    };
    const formData = new FormData();

    formData.append(
      "post",
      new Blob([JSON.stringify(requestBody)], { type: "application/json" }),
    );

    if (payload.imageFile) {
      formData.append("image", payload.imageFile);
    }

    const response = await axiosInstance.post<BackendPostCreateResponse>(
      "/posts",
      formData,
    );

    const post = await hydratePost(unwrapCreatedPost(response.data));

    return {
      ...post,
      imageUrl: post.imageUrl ?? payload.imageUrl,
    };
  },

  update: async (
    id: number,
    payload: Partial<Post> & { imageFile?: File },
  ): Promise<Post> => {
    const requestBody: Record<string, unknown> = {};
    if (typeof payload.title === "string") requestBody.title = payload.title;
    if (typeof payload.body === "string") requestBody.content = payload.body;
    if (payload.category) {
      requestBody.categoryId = await resolveCategoryId(payload.category);
    }

    let response;
    if (payload.imageFile) {
      const formData = new FormData();
      formData.append(
        "post",
        new Blob([JSON.stringify(requestBody)], { type: "application/json" }),
      );
      formData.append("image", payload.imageFile);
      response = await axiosInstance.put<BackendPostMutationResponse>(
        `/posts/${id}`,
        formData,
      );
    } else {
      response = await axiosInstance.put<BackendPostMutationResponse>(
        `/posts/${id}`,
        requestBody,
      );
    }

    const post = await hydratePost(unwrapPost(response.data));
    return {
      ...post,
      imageUrl: post.imageUrl ?? payload.imageUrl,
    };
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
    const response = await axiosInstance.post<BackendCommentMutationResponse>(
      "/comments",
      requestBody,
    );
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
