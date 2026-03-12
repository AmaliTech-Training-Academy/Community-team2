package com.amalitech.communityboard.service.implementations;

import com.amalitech.communityboard.dto.response.CategoryResponse;
import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.mapping.CategoryMapper;
import com.amalitech.communityboard.models.Category;
import com.amalitech.communityboard.repository.CategoryRepository;
import com.amalitech.communityboard.service.interfaces.CategoryInterface;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryService implements CategoryInterface {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;



    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("category not found")
        );
        return categoryMapper.toResponse(category);
    }

    @Override
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        Page<Category> category =  categoryRepository.findAll(pageable);
        return category.map(categoryMapper::toResponse);
    }




}
