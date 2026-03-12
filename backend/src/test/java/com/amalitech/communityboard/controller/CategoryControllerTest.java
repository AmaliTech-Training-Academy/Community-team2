package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.ResponseDto;
import com.amalitech.communityboard.dto.response.CategoryResponse;
import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.service.interfaces.CategoryInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController Unit Tests")
class CategoryControllerTest {

    @Mock
    private CategoryInterface categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private CategoryResponse sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = CategoryResponse.builder()
                .id(1L)
                .name("Events")
                .build();
    }

    @Nested
    @DisplayName("GET / – getAllCategories")
    class GetAllCategories {

        @Test
        @DisplayName("returns 200 with paginated categories")
        void getAllCategories_returnsPaginatedList() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CategoryResponse> page = new PageImpl<>(List.of(sampleCategory), pageable, 1);
            when(categoryService.getAllCategories(any(Pageable.class))).thenReturn(page);

            ResponseDto<Page<CategoryResponse>> response = categoryController.getAllCategories(pageable);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("categories retrieved");
            assertThat(response.data().getContent()).hasSize(1);
            assertThat(response.data().getContent().get(0).getName()).isEqualTo("Events");
            verify(categoryService).getAllCategories(pageable);
        }

        @Test
        @DisplayName("returns empty page when no categories exist")
        void getAllCategories_noCategories_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CategoryResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(categoryService.getAllCategories(any(Pageable.class))).thenReturn(emptyPage);

            ResponseDto<Page<CategoryResponse>> response = categoryController.getAllCategories(pageable);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.data().getContent()).isEmpty();
            assertThat(response.data().getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("GET /{id} – getCategoryById")
    class GetCategoryById {

        @Test
        @DisplayName("returns 200 and category when found")
        void getCategoryById_found_returnsCategory() {
            when(categoryService.getCategoryById(1L)).thenReturn(sampleCategory);

            ResponseDto<CategoryResponse> response = categoryController.getCategoryById(1L);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("category retrieved");
            assertThat(response.data().getId()).isEqualTo(1L);
            assertThat(response.data().getName()).isEqualTo("Events");
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when category not found")
        void getCategoryById_notFound_throwsEntityNotFoundException() {
            when(categoryService.getCategoryById(99L))
                    .thenThrow(new EntityNotFoundException("category not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> categoryController.getCategoryById(99L));
        }
    }
}