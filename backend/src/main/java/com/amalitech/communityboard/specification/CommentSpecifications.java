package com.amalitech.communityboard.specification;

import com.amalitech.communityboard.dto.request.CommentFilter;
import com.amalitech.communityboard.models.Comment;
import org.springframework.data.jpa.domain.Specification;

public class CommentSpecifications {

    public static Specification<Comment> fromFilter(CommentFilter filter) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (filter == null) {
                return predicate;
            }

            if (filter.getPostId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("post").get("id"), filter.getPostId()));
            }

            if (filter.getUserId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("user").get("id"), filter.getUserId()));
            }

            if (filter.getContent() != null && !filter.getContent().isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("content")), "%" + filter.getContent().toLowerCase() + "%"));
            }

            if (filter.getParentCommentId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("parentCommentId"), filter.getParentCommentId()));
            }

            if (filter.getRootOnly() != null) {
                if (filter.getRootOnly()) {
                    predicate = cb.and(predicate, cb.isNull(root.get("parentCommentId")));
                } else {
                    predicate = cb.and(predicate, cb.isNotNull(root.get("parentCommentId")));
                }
            }

            if (filter.getCreatedAfter() != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAfter()));
            }

            if (filter.getCreatedBefore() != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedBefore()));
            }

            return predicate;
        };
    }
}

