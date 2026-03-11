package com.amalitech.communityboard.specification;

import com.amalitech.communityboard.dto.request.CategoryFilter;
import com.amalitech.communityboard.models.Category;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecifications {

    public static Specification<Category> fromFilter(CategoryFilter filter) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (filter == null) {
                return predicate;
            }

            if (filter.getName() != null && !filter.getName().isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
            }

            if (filter.getDescription() != null && !filter.getDescription().isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("description")), "%" + filter.getDescription().toLowerCase() + "%"));
            }

            return predicate;
        };
    }
}

