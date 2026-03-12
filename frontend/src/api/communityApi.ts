import axiosInstance from "./axiosInstance";
import type {
  Analytics,
  Category,
  Comment,
  Post,
  User,
  UserRole,
} from "../types";

type BackendUserRole = "ADMIN" | "MEMBER";

export interface ResponseDto<T> {
  status?: string;
  message?: string;
  data?: T;
}

export interface BackendPage<T> {
  content: T[];
  totalElements: number;
}

export interface BackendUserResponse {
  id: number;
  username: string;
  email: string;
  role: BackendUserRole;
  provider?: "GOOGLE" | "LOCAL";
  createdAt: string;
}

export interface BackendAuthResponse {
  accessToken?: string;
  tokenType?: string;
  user?: BackendUserResponse;
  token?: string;
  jwt?: string;
}

export interface BackendPostResponse {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  userId: number;
  categoryId: number;
  imageUrl?: string;
  imagePath?: string;
}

export interface BackendPostCreateRequest {
  title: string;
  content: string;
  categoryId: number;
}

function resolvePostImageUrl(raw: BackendPostResponse): string | undefined {
  return raw.imageUrl || raw.imagePath;
}

export interface BackendCommentResponse {
  id: number;
  postId: number;
  userId: number;
  content: string;
  createdAt: string;
  parentCommentId?: number | null;
}

export interface BackendCommentCreateRequest {
  postId: number;
  content: string;
  parentCommentId?: number | null;
}

export interface BackendCategoryResponse {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
}

const DEFAULT_PAGE_PARAMS = { page: 0, size: 100 };

function unwrapDto<T>(payload: T | ResponseDto<T>): T {
  if (
    payload &&
    typeof payload === "object" &&
    "data" in payload &&
    payload.data !== undefined
  ) {
    return payload.data;
  }

  return payload as T;
}

function unwrapPage<T>(
  payload: T[] | BackendPage<T> | ResponseDto<BackendPage<T>>,
): {
  items: T[];
  total: number;
} {
  const data = unwrapDto(
    payload as BackendPage<T> | ResponseDto<BackendPage<T>> | T[],
  );

  if (Array.isArray(data)) {
    return { items: data, total: data.length };
  }

  return {
    items: data.content ?? [],
    total: data.totalElements ?? data.content?.length ?? 0,
  };
}

function normalizeRole(role?: string): UserRole {
  return role === "ADMIN" ? "ADMIN" : "USER";
}

function normalizeCategory(name?: string): Category {
  if (name?.trim()) {
    const normalized = name.trim().toUpperCase();

    if (normalized === "DISCUSSIONS" || normalized === "DISCUSSION") {
      return "Discussion";
    }
    if (normalized === "NEWS") {
      return "News";
    }
    if (normalized === "EVENTS" || normalized === "EVENT") {
      return "Events";
    }
    if (normalized === "ALERTS" || normalized === "ALERT") {
      return "Alert";
    }

    return name.trim();
  }

  return "Discussion";
}

function fallbackCategory(categoryId: number): Category {
  return `Category ${categoryId}`;
}

export async function fetchCategoryNames(): Promise<Category[]> {
  const categories = await fetchCategories();
  return [
    ...new Set(categories.map((category) => normalizeCategory(category.name))),
  ];
}

export function mapUser(raw: BackendUserResponse): User {
  return {
    id: raw.id,
    email: raw.email,
    name: raw.username,
    role: normalizeRole(raw.role),
  };
}

function mapComment(
  raw: BackendCommentResponse,
  usersById: Map<number, User>,
  authorOverride?: string,
): Comment {
  return {
    id: raw.id,
    text: raw.content,
    author:
      authorOverride ?? usersById.get(raw.userId)?.name ?? `User ${raw.userId}`,
    authorId: raw.userId,
    createdAt: raw.createdAt,
  };
}

function buildLookup<T extends { id: number }>(items: T[]): Map<number, T> {
  return new Map(items.map((item) => [item.id, item]));
}

function getStoredAuthUser(): User | null {
  try {
    const raw = localStorage.getItem("ping_auth");
    if (!raw) return null;

    const parsed = JSON.parse(raw) as { state?: { user?: User | null } };
    return parsed.state?.user ?? null;
  } catch {
    return null;
  }
}

function buildStoredUserLookup(): Map<number, User> {
  const currentUser = getStoredAuthUser();
  return currentUser
    ? new Map([[currentUser.id, currentUser]])
    : new Map<number, User>();
}

export async function fetchUserById(userId: number): Promise<User> {
  const response = await axiosInstance.get<
    BackendUserResponse | ResponseDto<BackendUserResponse>
  >(`/users/${userId}`);
  return mapUser(unwrapDto(response.data));
}

export async function fetchUsers(): Promise<User[]> {
  const response = await axiosInstance.get<
    BackendUserResponse[] | ResponseDto<BackendPage<BackendUserResponse>>
  >("/users", { params: DEFAULT_PAGE_PARAMS });
  const { items } = unwrapPage(
    response.data as
      | BackendUserResponse[]
      | BackendPage<BackendUserResponse>
      | ResponseDto<BackendPage<BackendUserResponse>>,
  );
  return items.map(mapUser);
}

export async function fetchCategories(): Promise<BackendCategoryResponse[]> {
  const response = await axiosInstance.get<
    | BackendCategoryResponse[]
    | ResponseDto<BackendPage<BackendCategoryResponse>>
  >("/categories", { params: DEFAULT_PAGE_PARAMS });
  const { items } = unwrapPage(
    response.data as
      | BackendCategoryResponse[]
      | BackendPage<BackendCategoryResponse>
      | ResponseDto<BackendPage<BackendCategoryResponse>>,
  );
  return items;
}

export async function fetchPostsRaw(): Promise<BackendPostResponse[]> {
  const response = await axiosInstance.get<BackendPostResponse[]>("/posts", {
    params: DEFAULT_PAGE_PARAMS,
  });
  const { items } = unwrapPage(response.data);
  return items;
}

export async function fetchCommentsRaw(params?: {
  postId?: number;
  userId?: number;
}): Promise<BackendCommentResponse[]> {
  let path = "/comments";
  if (typeof params?.postId === "number")
    path = `/comments/by-post/${params.postId}`;
  if (typeof params?.userId === "number")
    path = `/comments/by-user/${params.userId}`;

  const response = await axiosInstance.get<
    | BackendPage<BackendCommentResponse>
    | ResponseDto<BackendPage<BackendCommentResponse>>
  >(path, { params: DEFAULT_PAGE_PARAMS });
  const { items } = unwrapPage(response.data);
  return items;
}

async function loadPostDependencies(
  postIds?: number[],
  postAuthorIds: number[] = [],
) {
  const [categoriesResult, commentsResult] = await Promise.allSettled([
    fetchCategories(),
    postIds && postIds.length === 1
      ? fetchCommentsRaw({ postId: postIds[0] })
      : fetchCommentsRaw(),
  ]);

  const categories =
    categoriesResult.status === "fulfilled" ? categoriesResult.value : [];
  const comments =
    commentsResult.status === "fulfilled" ? commentsResult.value : [];

  const userIds = [
    ...new Set(
      [...postAuthorIds, ...comments.map((comment) => comment.userId)].filter(
        Number.isFinite,
      ),
    ),
  ];
  const userResults = await Promise.allSettled(
    userIds.map((userId) => fetchUserById(userId)),
  );
  const usersById = buildStoredUserLookup();

  userResults.forEach((result) => {
    if (result.status === "fulfilled") {
      usersById.set(result.value.id, result.value);
    }
  });

  const categoriesById = buildLookup(categories);
  const commentsByPostId = new Map<number, Comment[]>();

  comments.forEach((comment) => {
    const mapped = mapComment(comment, usersById);
    const existing = commentsByPostId.get(comment.postId) ?? [];
    existing.push(mapped);
    commentsByPostId.set(comment.postId, existing);
  });

  commentsByPostId.forEach((postComments) => {
    postComments.sort(
      (left, right) =>
        new Date(left.createdAt).getTime() -
        new Date(right.createdAt).getTime(),
    );
  });

  return { usersById, categoriesById, commentsByPostId };
}

export async function hydratePosts(
  rawPosts: BackendPostResponse[],
): Promise<Post[]> {
  const dependencies = await loadPostDependencies(
    rawPosts.map((post) => post.id),
    rawPosts.map((post) => post.userId),
  );

  return rawPosts
    .map((post) => mapPost(post, dependencies))
    .sort(
      (left, right) =>
        new Date(right.createdAt).getTime() -
        new Date(left.createdAt).getTime(),
    );
}

export async function hydratePost(rawPost: BackendPostResponse): Promise<Post> {
  const dependencies = await loadPostDependencies(
    [rawPost.id],
    [rawPost.userId],
  );
  return mapPost(rawPost, dependencies);
}

function mapPost(
  raw: BackendPostResponse,
  dependencies: {
    usersById: Map<number, User>;
    categoriesById: Map<number, BackendCategoryResponse>;
    commentsByPostId: Map<number, Comment[]>;
  },
): Post {
  const categoryName = dependencies.categoriesById.get(raw.categoryId)?.name;

  return {
    id: raw.id,
    title: raw.title,
    body: raw.content,
    category: categoryName
      ? normalizeCategory(categoryName)
      : fallbackCategory(raw.categoryId),
    author:
      dependencies.usersById.get(raw.userId)?.name ?? `User ${raw.userId}`,
    authorId: raw.userId,
    createdAt: raw.createdAt,
    comments: dependencies.commentsByPostId.get(raw.id) ?? [],
    imageUrl: resolvePostImageUrl(raw),
  };
}

export async function resolveCategoryId(category: Category): Promise<number> {
  const categories = await fetchCategories();
  const match = categories.find(
    (item) =>
      normalizeCategory(item.name).toLowerCase() ===
      normalizeCategory(category).toLowerCase(),
  );

  if (!match) {
    throw new Error(`Category "${category}" does not exist on the server.`);
  }

  return match.id;
}

export async function mapCreatedOrUpdatedComment(
  raw: BackendCommentResponse,
  authorOverride?: string,
): Promise<Comment> {
  return mapComment(raw, buildStoredUserLookup(), authorOverride);
}

export async function buildAnalytics(): Promise<Analytics> {
  const [posts, comments, categories] = await Promise.all([
    fetchPostsRaw(),
    fetchCommentsRaw().catch(() => []),
    fetchCategories().catch(() => []),
  ]);

  const categoriesById = buildLookup(categories);
  const categoryBreakdown = Object.fromEntries(
    categories.map((category) => [category.name, 0]),
  ) as Record<string, number>;

  posts.forEach((post) => {
    const categoryName = categoriesById.get(post.categoryId)?.name;
    const key = categoryName
      ? normalizeCategory(categoryName)
      : fallbackCategory(post.categoryId);
    categoryBreakdown[key] = (categoryBreakdown[key] ?? 0) + 1;
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
  posts.forEach((post) => {
    const dayName = dayNames[new Date(post.createdAt).getDay()];
    if (dayName !== "Sun") {
      dayMap[dayName] += 1;
      return;
    }

    dayMap.Sun += 1;
  });

  const contributorCounts = new Map<number, number>();
  posts.forEach((post) => {
    contributorCounts.set(
      post.userId,
      (contributorCounts.get(post.userId) ?? 0) + 1,
    );
  });

  const usersById = buildStoredUserLookup();
  const contributorUserIds = [...new Set(posts.map((post) => post.userId))];
  const contributorUsers = await Promise.allSettled(
    contributorUserIds.map((userId) => fetchUserById(userId)),
  );

  contributorUsers.forEach((result) => {
    if (result.status === "fulfilled") {
      usersById.set(result.value.id, result.value);
    }
  });

  const topContributors = [...contributorCounts.entries()]
    .sort((left, right) => right[1] - left[1])
    .slice(0, 10)
    .map(([userId, count]) => ({
      name: usersById.get(userId)?.name ?? `User ${userId}`,
      count,
    }));

  return {
    totalPosts: posts.length,
    totalComments: comments.length,
    totalUsers: new Set(posts.map((post) => post.userId)).size,
    categoryBreakdown,
    dayActivity: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"].map(
      (day) => ({
        day,
        count: dayMap[day],
      }),
    ),
    topContributors,
  };
}
