package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.ResponseDto;
import com.amalitech.communityboard.dto.response.CategoryResponse;
import com.amalitech.communityboard.service.interfaces.CategoryInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Categories", description = "Category browsing endpoints")
public class CategoryController {

    private final CategoryInterface categoryService;

    public CategoryController(CategoryInterface categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve a paginated list of all categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class))))
    })
    public ResponseDto<Page<CategoryResponse>> getAllCategories(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<CategoryResponse> categories = categoryService.getAllCategories(pageable);

        return new ResponseDto<>(HttpStatus.OK, "categories retrieved", categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by id", description = "Retrieve a single category by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category retrieved",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseDto<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);

        return new ResponseDto<>(HttpStatus.OK, "category retrieved", category);
    }
}
