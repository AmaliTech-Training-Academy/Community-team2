package com.amalitech.communityboard.mapping;

import com.amalitech.communityboard.dto.request.CategoryRequest;
import com.amalitech.communityboard.dto.response.CategoryResponse;
import com.amalitech.communityboard.models.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
    Category toEntity(CategoryRequest category);
}
