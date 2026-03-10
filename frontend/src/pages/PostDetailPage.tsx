import { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { usePostsStore } from "../features/posts/postsStore";
import { useAuthStore } from "../features/auth/authStore";
import { useToast } from "../components/atoms/Toast";
import { api } from "../api/index";
import { Badge } from "../components/atoms/Badge";
import { PostModal } from "../components/organisms/PostModal";
import { Spinner } from "../components/atoms/Spinner";
import { timeAgo } from "../utils";
import type { Comment } from "../types";
import HomeIcon from "../assets/images/home.svg?react";
import ClockIcon from "../assets/images/clock.svg?react";
import PencilIcon from "../assets/images/pencil.svg?react";
import TrashIcon from "../assets/images/trash.svg?react";
import Comments from '../assets/images/comments.svg?react';

function getInitials(name: string) {
  return name
    .split(" ")
    .map((n) => n[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();
}

export default function PostDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const user = useAuthStore((s) => s.user);
  const currentPost = usePostsStore((s) => s.currentPost);
  const detailLoading = usePostsStore((s) => s.detailLoading);
  const fetchPost = usePostsStore((s) => s.fetchPost);
  const deletePost = usePostsStore((s) => s.deletePost);

  const toast = useToast();

  const [editModal, setEditModal] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState(false);
  const [commentText, setCommentText] = useState("");
  const [commentLoading, setCommentLoading] = useState(false);
  const [editingComment, setEditingComment] = useState<number | null>(null);
  const [editCommentText, setEditCommentText] = useState("");
  const [comments, setComments] = useState<Comment[]>([]);

  const numericId = Number(id);

  useEffect(() => {
    if (id) fetchPost(numericId);
  }, [id, fetchPost, numericId]);

  useEffect(() => {
    if (currentPost) setComments(currentPost.comments || []);
  }, [currentPost]);

  const handleDelete = useCallback(async () => {
    try {
      await deletePost(numericId);
      toast("Post deleted");
      navigate("/");
    } catch (err: unknown) {
      toast(String(err), "error");
    }
  }, [deletePost, numericId, toast, navigate]);

  const handleComment = useCallback(async () => {
    if (!commentText.trim()) return;
    setCommentLoading(true);
    try {
      const c = await api.posts.addComment(numericId, {
        text: commentText,
        author: user!.name,
        authorId: user!.id,
      });
      setComments((prev) => [...prev, c]);
      setCommentText("");
      toast("Comment added");
    } catch (err: unknown) {
      toast(String(err), "error");
    } finally {
      setCommentLoading(false);
    }
  }, [commentText, numericId, user, toast]);

  const handleDeleteComment = useCallback(
    async (cid: number) => {
      try {
        await api.posts.deleteComment(numericId, cid);
        setComments((prev) => prev.filter((c) => c.id !== cid));
        toast("Comment deleted");
      } catch (err: unknown) {
        toast(String(err), "error");
      }
    },
    [numericId, toast],
  );

  const handleEditComment = useCallback(
    async (comment: Comment) => {
      try {
        await api.posts.updateComment(numericId, comment.id, editCommentText);
        setComments((prev) =>
          prev.map((c) =>
            c.id === comment.id ? { ...c, text: editCommentText } : c,
          ),
        );
        setEditingComment(null);
        toast("Comment updated");
      } catch (err: unknown) {
        toast(String(err), "error");
      }
    },
    [numericId, editCommentText, toast],
  );

  const openEditComment = useCallback((comment: Comment) => {
    setEditingComment(comment.id);
    setEditCommentText(comment.text);
  }, []);

  const cancelEditComment = useCallback(() => setEditingComment(null), []);
  const closeEditModal = useCallback(() => setEditModal(false), []);
  const closeDeleteConfirm = useCallback(() => setDeleteConfirm(false), []);

  if (detailLoading || !currentPost) return <Spinner />;

  return (
    <div data-testid="post-detail-page" className="fade-in">
      {/* Breadcrumb pill */}
      <div className="mb-5">
        <div className="inline-flex items-center gap-3 bg-white border border-borderstroke rounded-lg px-4 py-2.5">
          <button
            data-testid="back-to-posts-btn"
            onClick={() => navigate("/")}
            className="flex items-center gap-2 text-body-sm font-semibold text-blue-gray-dark hover:opacity-70 transition-opacity bg-transparent border-none cursor-pointer p-0"
          >
            <HomeIcon width={18} height={18} />
            <span>Home</span>
          </button>
          <span className="text-body-sm font-semibold text-blue-gray-dark">
            &gt;
          </span>
          <span className="text-body-sm font-semibold text-blue-gray-dark">
            Post Details
          </span>
        </div>
      </div>

      <div data-testid="post-detail-card">
        <div className="flex items-start justify-between gap-3 mb-4">
          <h1
            data-testid="post-detail-title"
            className="text-h-lg font-bold text-blue-gray-dark leading-snug flex-1"
          >
            {currentPost.title}
          </h1>
          <div className="flex items-center gap-2 shrink-0">
            <Badge category={currentPost.category} />
          </div>
        </div>

        {/* Optional hero image */}
        {currentPost.imageUrl && (
          <div className="w-full max-h-72 overflow-hidden rounded-xl mb-4">
            <img
              data-testid="post-detail-image"
              src={currentPost.imageUrl}
              alt={currentPost.title}
              className="w-full h-full object-cover"
            />
          </div>
        )}

        {/* Body — before author/time */}
        <p
          data-testid="post-detail-body"
          className="text-body-lg text-blue-gray leading-relaxed mb-4"
        >
          {currentPost.body}
        </p>

        {/* Author + time */}
        <div className="flex items-center gap-3 text-body-sm mb-5">
          <span className="font-semibold text-blue-gray">
            {currentPost.author}
          </span>
          <span className="text-gray-400 flex items-center gap-1">
            <ClockIcon /> {timeAgo(currentPost.createdAt)}
          </span>
        </div>

        <hr className="border-borderstroke" />
      </div>

      {/* Comment form */}
      <div data-testid="comment-form" className="mt-6">
        <textarea
          data-testid="comment-input"
          className="w-full px-4 py-3 bg-primary border border-borderstroke rounded-xl text-body-lg text-blue-gray-dark placeholder:text-gray-400 focus:outline-none focus:border-blue-gray transition-colors resize-none min-h-44 md:min-h-28"
          placeholder="Share your thoughts..."
          value={commentText}
          onChange={(e) => setCommentText(e.target.value)}
        />
        <div className="mt-3 flex md:justify-end">
          <button
            data-testid="comment-submit-btn"
            onClick={handleComment}
            disabled={commentLoading || !commentText.trim()}
            className="w-full h-11 md:w-55 md:h-9 bg-blue-gray-dark text-white text-body-sm font-semibold rounded-lg hover:opacity-90 transition-opacity disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {commentLoading ? "Posting…" : "Add comment"}
          </button>
        </div>
      </div>

      {/* Comments section */}
      <div data-testid="comments-section" className="mt-6">
        <h2
          data-testid="comments-count-heading"
          className="text-h-md font-bold text-blue-gray-dark mb-4"
        >
          Comments ({comments.length})
        </h2>

        {comments.length === 0 && (
          <div data-testid="comment-empty-state" className="text-center py-16">
          <div className="text-5xl mb-3 flex justify-center"><Comments /></div>
          
          <div className="text-sm text-gray-400">
            No Comments yet
          </div>
        </div>

          
        )}

        {comments.map((comment) => {
          const canModifyComment =
            user!.id === comment.authorId || user!.role === "ADMIN";
          const isEditingThis = editingComment === comment.id;

          return (
            <div
              key={comment.id}
              data-testid={`comment-item-${comment.id}`}
              data-comment-id={comment.id}
              className="py-4 border-b border-borderstroke last:border-0 flex gap-3"
            >
              {/* Avatar */}
              <div className="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center text-body-sm font-semibold text-blue-gray-dark shrink-0">
                {getInitials(comment.author)}
              </div>

              {/* Content */}
              <div className="flex-1 min-w-0">
                {/* Name + time stacked */}
                <span
                  data-testid="comment-author"
                  className="text-body-sm font-semibold text-blue-gray-dark block"
                >
                  {comment.author}
                </span>
                <span className="text-body-sm text-gray-400 block mb-2">
                  {timeAgo(comment.createdAt)}
                </span>

                {/* Edit mode */}
                {isEditingThis ? (
                  <div>
                    <textarea
                      data-testid="comment-edit-input"
                      title="Edit comment"
                      className="w-full px-4 py-3 bg-primary border border-borderstroke rounded-xl text-body-lg text-blue-gray-dark focus:outline-none focus:border-blue-gray transition-colors resize-none min-h-20"
                      value={editCommentText}
                      onChange={(e) => setEditCommentText(e.target.value)}
                    />
                    <div className="flex gap-2 mt-2">
                      <button
                        data-testid="comment-edit-save-btn"
                        onClick={() => handleEditComment(comment)}
                        className="px-5 py-2.5 bg-blue-gray-dark text-white text-body-sm font-semibold rounded-lg hover:opacity-90 transition-opacity"
                      >
                        Save Changes
                      </button>
                      <button
                        data-testid="comment-edit-cancel-btn"
                        onClick={cancelEditComment}
                        className="px-4 py-2.5 text-body-sm font-semibold text-blue-gray bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                ) : (
                  <>
                    <p
                      data-testid="comment-text"
                      className="text-body-sm text-blue-gray leading-relaxed"
                    >
                      {comment.text}
                    </p>
                  </>
                )}
              </div>

              {/* Actions (icon-only, right side) */}
              {canModifyComment && !isEditingThis && (
                <div className="flex items-center gap-4 self-center">
                  <button
                    data-testid="comment-edit-btn"
                    onClick={() => openEditComment(comment)}
                    className="bg-transparent border-none p-0 hover:opacity-70 transition-opacity"
                    aria-label="Edit comment"
                    title="Edit"
                  >
                    <PencilIcon />
                  </button>
                  <button
                    data-testid="comment-delete-btn"
                    onClick={() => handleDeleteComment(comment.id)}
                    className="bg-transparent border-none p-0 hover:opacity-70 transition-opacity"
                    aria-label="Delete comment"
                    title="Delete"
                  >
                    <TrashIcon />
                  </button>
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Modals */}
      {editModal && (
        <PostModal
          post={currentPost}
          onClose={closeEditModal}
          onSaved={() => {
            fetchPost(numericId);
            closeEditModal();
          }}
        />
      )}

      {deleteConfirm && (
        <div
          data-testid="delete-modal-overlay"
          className="fixed inset-0 bg-black/45 z-500 flex items-center justify-center p-4"
          onClick={(e) => e.target === e.currentTarget && closeDeleteConfirm()}
        >
          <div
            data-testid="delete-modal"
            className="modal-in bg-white rounded-xl shadow-2xl w-full max-w-sm p-6"
          >
            <h3 className="text-body-lg font-bold text-blue-gray-dark mb-3">
              Delete Post
            </h3>
            <p className="text-body-sm text-blue-gray mb-5">
              Are you sure you want to delete{" "}
              <strong>"{currentPost.title}"</strong>? This cannot be undone.
            </p>
            <div className="flex justify-end gap-2">
              <button
                data-testid="delete-modal-cancel-btn"
                onClick={closeDeleteConfirm}
                className="px-4 py-2 text-body-sm font-semibold text-blue-gray bg-gray-100 border border-borderstroke rounded-xl hover:bg-gray-200 transition-colors"
              >
                Cancel
              </button>
              <button
                data-testid="delete-modal-confirm-btn"
                onClick={handleDelete}
                className="px-4 py-2 text-body-sm font-semibold text-white bg-red-500 rounded-xl hover:bg-red-600 transition-colors"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
