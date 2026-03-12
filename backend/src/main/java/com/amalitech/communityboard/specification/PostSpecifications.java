package com.amalitech.communityboard.specification;

import com.amalitech.communityboard.dto.request.PostFilter;
import com.amalitech.communityboard.models.Post;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecifications {

    public static Specification<Post> fromFilter(PostFilter filter) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (filter == null) {
                return predicate;
            }

            if (filter.getTitle() != null && !filter.getTitle().isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("title")), "%" + filter.getTitle().toLowerCase() + "%"));
            }

            if (filter.getContent() != null && !filter.getContent().isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("content")), "%" + filter.getContent().toLowerCase() + "%"));
            }

            if (filter.getCategoryId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (filter.getAuthorId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("author").get("id"), filter.getAuthorId()));
            }

            if (filter.getCreatedAfter() != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAfter()));
            }

            if (filter.getCreatedBefore() != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedBefore()));
            }

            if (filter.getMinViews() != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("viewCount"), filter.getMinViews()));
            }

            if (filter.getMaxViews() != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("viewCount"), filter.getMaxViews()));
            }

            return predicate;
        };
    }
}

