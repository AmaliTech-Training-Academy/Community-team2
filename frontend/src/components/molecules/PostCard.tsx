import { memo } from "react";
import type { Post } from "../../types";
import { Badge } from "../atoms/Badge";
import { timeAgo } from "../../utils";

interface PostCardProps {
  post: Post;
  onClick: () => void;
}

function ClockIcon() {
  return (
    <svg
      width="14"
      height="14"
      viewBox="0 0 14 14"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <circle cx="7" cy="7" r="5.5" stroke="#5A6F7C" strokeWidth="1.2" />
      <path
        d="M7 4.5V7L8.5 8.5"
        stroke="#5A6F7C"
        strokeWidth="1.2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function CommentIcon() {
  return (
    <svg
      width="14"
      height="14"
      viewBox="0 0 14 14"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        d="M2 2.5C2 2.22386 2.22386 2 2.5 2H11.5C11.7761 2 12 2.22386 12 2.5V8.5C12 8.77614 11.7761 9 11.5 9H4.5L2 12V2.5Z"
        stroke="#5A6F7C"
        strokeWidth="1.2"
        strokeLinejoin="round"
      />
    </svg>
  );
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
          <Badge category={post.category} />
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
            <ClockIcon /> {timeAgo(post.createdAt)}
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
            <CommentIcon /> {post.comments?.length ?? 0}
          </span>
        </div>
      </div>
    </div>
  );
});
