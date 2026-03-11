import { api } from "../api/index";
import { usePostsStore } from "../features/posts/postsStore";
import type { Post } from "../types";

vi.mock("../api/index", () => ({
  api: {
    posts: {
      getAll: vi.fn(),
      getById: vi.fn(),
      create: vi.fn(),
      update: vi.fn(),
      delete: vi.fn(),
    },
  },
}));

const mockPost: Post = {
  id: 42,
  title: "Test Post Title",
  body: "This is the body of the post for testing purposes.",
  category: "Events",
  author: "Jane Doe",
  authorId: 1,
  createdAt: new Date(Date.now() - 60_000).toISOString(),
  comments: [
    {
      id: 1,
      text: "hi",
      author: "Bob",
      authorId: 2,
      createdAt: new Date().toISOString(),
    },
  ],
};

function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((res) => {
    resolve = res;
  });

  return { promise, resolve };
}

describe("usePostsStore", () => {
  beforeEach(() => {
    usePostsStore.setState({
      posts: [],
      currentPost: null,
      filters: { category: "All", title: "" },
      listLoading: false,
      detailLoading: false,
    });
    vi.clearAllMocks();
  });

  it("fetchPosts stores returned posts", async () => {
    vi.mocked(api.posts.getAll).mockResolvedValue({
      posts: [mockPost],
      total: 1,
    });

    await usePostsStore.getState().fetchPosts();

    expect(usePostsStore.getState().posts[0]?.id).toBe(42);
  });

  it("fetchPosts toggles listLoading during the request lifecycle", async () => {
    const pending = deferred<{ posts: Post[]; total: number }>();
    vi.mocked(api.posts.getAll).mockReturnValue(pending.promise);

    const fetchPromise = usePostsStore.getState().fetchPosts();
    expect(usePostsStore.getState().listLoading).toBe(true);

    pending.resolve({ posts: [mockPost], total: 1 });
    await fetchPromise;

    expect(usePostsStore.getState().listLoading).toBe(false);
  });

  it("fetchPost stores the returned currentPost", async () => {
    vi.mocked(api.posts.getById).mockResolvedValue(mockPost);

    await usePostsStore.getState().fetchPost(42);

    expect(usePostsStore.getState().currentPost?.id).toBe(42);
  });

  it("createPost prepends the created post", async () => {
    const createdPost = { ...mockPost, id: 43, title: "Created Post" };
    vi.mocked(api.posts.create).mockResolvedValue(createdPost);

    await usePostsStore.getState().createPost({
      title: createdPost.title,
      body: createdPost.body,
      category: createdPost.category,
      author: createdPost.author,
      authorId: createdPost.authorId,
      imageUrl: createdPost.imageUrl,
    });

    expect(usePostsStore.getState().posts[0]?.id).toBe(43);
  });

  it("updatePost replaces the matching post", async () => {
    usePostsStore.setState({ posts: [mockPost] });
    vi.mocked(api.posts.update).mockResolvedValue({
      ...mockPost,
      title: "New Title",
    });

    await usePostsStore.getState().updatePost(42, { title: "New Title" });

    expect(usePostsStore.getState().posts[0]?.title).toBe("New Title");
  });

  it("deletePost removes the matching post", async () => {
    usePostsStore.setState({ posts: [mockPost] });
    vi.mocked(api.posts.delete).mockResolvedValue(undefined);

    await usePostsStore.getState().deletePost(42);

    expect(usePostsStore.getState().posts).toHaveLength(0);
  });

  it("setFilters merges the provided filters", () => {
    usePostsStore.getState().setFilters({ category: "Events" });

    expect(usePostsStore.getState().filters.category).toBe("Events");
  });
});
