import { useEffect, useState, useCallback, memo } from "react";
import { useNavigate } from "react-router-dom";
import { usePostsStore } from "../features/posts/postsStore";
import { PostCard } from "../components/molecules/PostCard";
import { SearchBar } from "../components/molecules/SearchBar";
import { CategoryFilter } from "../components/molecules/CategoryFilter";
import { PostModal } from "../components/organisms/PostModal";
import { Spinner } from "../components/atoms/Spinner";
import { useDebounce } from "../hooks/useDebounce";
import type { Post } from "../types";
import { useCategoriesStore } from "../features/categories/categoriesStore";
import Comments from "../assets/images/comments.svg?react";
import PlusIcon from "../assets/images/plus.svg?react";

const POSTS_PER_PAGE = 5;

const PostList = memo(function PostList({
  posts,
  onNavigate,
}: {
  posts: Post[];
  onNavigate: (id: number) => void;
}) {
  return (
    <div data-testid="post-list" className="flex flex-col gap-3">
      {posts.map((post) => (
        <PostCard
          key={post.id}
          post={post}
          onClick={() => onNavigate(post.id)}
        />
      ))}
    </div>
  );
});

function Pagination({
  page,
  totalPages,
  onPage,
}: {
  page: number;
  totalPages: number;
  onPage: (p: number) => void;
}) {
  if (totalPages <= 1) return null;

  const pages: number[] = [];
  for (let i = 1; i <= totalPages; i++) pages.push(i);

  const btnBase =
    "min-w-[26px] h-6 px-2 rounded-md text-[11px] leading-none font-medium transition-colors border";

  return (
    <div className="flex items-center justify-end gap-1.5 mt-6">
      <button
        onClick={() => onPage(page - 1)}
        disabled={page === 1}
        className={`${btnBase} border-borderstroke text-blue-gray-dark disabled:opacity-40`}
      >
        Previous
      </button>

      {pages.map((p) => (
        <button
          key={p}
          onClick={() => onPage(p)}
          className={`${btnBase} ${
            p === page
              ? "bg-blue-gray-light text-white border-blue-gray-dark"
              : "bg-white text-blue-gray-dark border-borderstroke hover:border-gray-400"
          }`}
        >
          {p}
        </button>
      ))}

      <button
        onClick={() => onPage(page + 1)}
        disabled={page === totalPages}
        className={`${btnBase} border-borderstroke text-blue-gray-dark disabled:opacity-40`}
      >
        Next
      </button>
    </div>
  );
}

export default function HomePage() {
  const navigate = useNavigate();

  const posts = usePostsStore((s) => s.posts);
  const listLoading = usePostsStore((s) => s.listLoading);
  const filters = usePostsStore((s) => s.filters);
  const fetchPosts = usePostsStore((s) => s.fetchPosts);
  const setFilters = usePostsStore((s) => s.setFilters);
  const categories = useCategoriesStore((s) => s.categories);
  const fetchCategories = useCategoriesStore((s) => s.fetch);

  const [showModal, setShowModal] = useState(false);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const debSearch = useDebounce(search, 400);

  useEffect(() => {
    setFilters({ title: debSearch });
    setPage(1);
  }, [debSearch, setFilters]);

  useEffect(() => {
    setPage(1);
  }, [filters.category]);

  useEffect(() => {
    fetchPosts();
  }, [filters, fetchPosts]);

  useEffect(() => {
    fetchCategories();
  }, [fetchCategories]);

  const handleNavigate = useCallback(
    (id: number) => {
      navigate(`/posts/${id}`);
    },
    [navigate],
  );

  const handleModalClose = useCallback(() => setShowModal(false), []);
  const handleModalSaved = useCallback(() => setShowModal(false), []);

  const totalPages = Math.ceil(posts.length / POSTS_PER_PAGE);
  const pagedPosts = posts.slice(
    (page - 1) * POSTS_PER_PAGE,
    page * POSTS_PER_PAGE,
  );

  return (
    <div data-testid="home-page" className="fade-in">
      {/* Search + Create row */}
      <div className="flex flex-col gap-3 md:flex-row md:items-center mb-5">
        <div className="flex-1">
          <SearchBar value={search} onChange={setSearch} />
        </div>
        <button
          data-testid="create-post-btn"
          onClick={() => setShowModal(true)}
          className="w-full md:w-auto bg-blue-gray-light text-white text-body-sm font-semibold h-10 px-4 rounded-lg hover:opacity-90 transition-opacity flex items-center justify-center gap-2"
        >
          <PlusIcon aria-hidden="true" className="h-5 w-5" />
          <span>Create post</span>
        </button>
      </div>

      <CategoryFilter
        active={filters.category || "All"}
        categories={categories}
        onSelect={useCallback(
          (c: string) => setFilters({ category: c as any }),
          [setFilters],
        )}
      />

      {listLoading ? (
        <Spinner />
      ) : posts.length === 0 ? (
        <div data-testid="posts-empty-state" className="text-center py-16">
          <div className="text-5xl mb-3 flex justify-center">
            <Comments />
          </div>

          <div className="text-sm text-gray-400">
            No posts have been made yet
          </div>
        </div>
      ) : (
        <>
          <PostList posts={pagedPosts} onNavigate={handleNavigate} />
          <Pagination page={page} totalPages={totalPages} onPage={setPage} />
        </>
      )}

      {showModal && (
        <PostModal onClose={handleModalClose} onSaved={handleModalSaved} />
      )}
    </div>
  );
}
