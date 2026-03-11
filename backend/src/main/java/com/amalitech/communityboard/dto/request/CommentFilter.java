package com.amalitech.communityboard.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentFilter {
    private Long postId;
    private Long userId;
    private String content;
    private Long parentCommentId;
    /**
     * true  => only root comments (parentCommentId IS NULL)
     * false => only replies (parentCommentId IS NOT NULL)
     * null  => no constraint
     */
    private Boolean rootOnly;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
}
