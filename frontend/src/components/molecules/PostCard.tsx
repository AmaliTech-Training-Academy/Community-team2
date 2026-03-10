import { memo } from "react";
import type { Post } from "../../types";
import { Badge } from "../atoms/Badge";
import { timeAgo } from "../../utils";
import ClockIcon from "../../assets/images/clock-small.svg?react";
import CommentIcon from "../../assets/images/comment-bubble.svg?react";

interface PostCardProps {
  post: Post;
  onClick: () => void;
}

export const PostCard = memo(function PostCard({
  post,
  onClick,
}: PostCardProps) {
  return (
    <div
      data-testid={`post-card-${post.id}`}
      data-post-id={post.id}
      data-category={post.category}
      className="bg-white border border-borderstroke rounded-lg cursor-pointer hover:shadow-sm transition-shadow overflow-hidden"
      onClick={onClick}
    >
      {post.imageUrl && (
        <div className="w-full h-40 overflow-hidden">
          <img
            data-testid="post-card-image"
            src={post.imageUrl}
            alt={post.title}
            className="w-full h-full object-cover"
            loading="lazy"
          />
        </div>
      )}

      <div className="px-6 py-5 flex flex-col gap-2">
        <div className="flex items-start justify-between gap-3">
          <h3
            data-testid="post-card-title"
            className="text-body-lg font-semibold text-blue-gray-dark leading-snug flex-1 min-w-0"
          >
            {post.title}
          </h3>
          <div className="max-w-[40vw] shrink-0 self-start sm:max-w-[45%]">
            <Badge category={post.category} />
          </div>
        </div>
        <p
          data-testid="post-card-excerpt"
          className="text-body-sm text-gray-500 leading-relaxed line-clamp-2"
        >
          {post.body}
        </p>
        <div className="flex items-center gap-3 text-body-sm">
          <span
            data-testid="post-card-author"
            className="font-semibold text-blue-gray"
          >
            {post.author}
          </span>
          <span
            data-testid="post-card-time"
            className="text-gray-400 flex items-center gap-1"
          >
            <ClockIcon aria-hidden="true" /> {timeAgo(post.createdAt)}
          </span>
          {post.imageUrl && (
            <span
              data-testid="post-card-has-image"
              className="text-gray-400"
              title="Has image"
            >
              🖼
            </span>
          )}
          <span
            data-testid="post-card-comment-count"
            className="text-gray-400 ml-auto flex items-center gap-1"
          >
            <CommentIcon aria-hidden="true" /> {post.comments?.length ?? 0}
          </span>
        </div>
      </div>
    </div>
  );
});
