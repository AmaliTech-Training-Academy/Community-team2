package com.amalitech.communityboard.specification;

import com.amalitech.communityboard.dto.request.UserFilter;
import com.amalitech.communityboard.models.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<User> fromFilter(UserFilter filter) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (filter == null) {
                return predicate;
            }

            if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("username")), "%" + filter.getUsername().toLowerCase() + "%"));
            }

            if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("email")), "%" + filter.getEmail().toLowerCase() + "%"));
            }

            if (filter.getRole() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("role"), filter.getRole()));
            }

            if (filter.getProvider() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("provider"), filter.getProvider()));
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

