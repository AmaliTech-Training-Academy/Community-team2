package com.amalitech.communityboard.service.interfaces;

import com.amalitech.communityboard.dto.request.CategoryRequest;
import com.amalitech.communityboard.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryInterface {
    CategoryResponse getCategoryById(Long id);
    Page<CategoryResponse> getAllCategories(Pageable pageable);

}
