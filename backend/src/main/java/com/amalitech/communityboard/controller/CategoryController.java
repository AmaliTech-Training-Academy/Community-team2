package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.ResponseDto;
import com.amalitech.communityboard.dto.response.CategoryResponse;
import com.amalitech.communityboard.service.interfaces.CategoryInterface;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryInterface categoryService;

    public CategoryController(CategoryInterface categoryService) {
        this.categoryService = categoryService;
    }


    @GetMapping
    public ResponseDto<Page<CategoryResponse>> getAllCategories(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<CategoryResponse> categories = categoryService.getAllCategories(pageable);

        return new ResponseDto<>(HttpStatus.OK, "categories retrieved", categories);
    }

    @GetMapping("/{id}")
    public ResponseDto<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);

        return new ResponseDto<>(HttpStatus.OK, "category retrieved", category);
    }




}
