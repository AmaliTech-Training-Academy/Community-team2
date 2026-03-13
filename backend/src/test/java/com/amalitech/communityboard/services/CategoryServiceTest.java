package com.amalitech.communityboard.services;

import com.amalitech.communityboard.dto.response.CategoryResponse;
import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.mapping.CategoryMapper;
import com.amalitech.communityboard.models.Category;
import com.amalitech.communityboard.repository.CategoryRepository;
import com.amalitech.communityboard.service.implementations.CategoryService;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category sampleCategory;
    private CategoryResponse sampleCategoryResponse;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category();
        sampleCategory.setId(1L);
        sampleCategory.setName("Events");

        sampleCategoryResponse = CategoryResponse.builder()
                .id(1L)
                .name("Events")
                .build();
    }

    @Nested
    @DisplayName("getCategoryById")
    class GetCategoryById {

        @Test
        @DisplayName("returns mapped response when category exists")
        void getCategoryById_found_returnsMappedResponse() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
            when(categoryMapper.toResponse(sampleCategory)).thenReturn(sampleCategoryResponse);

            CategoryResponse result = categoryService.getCategoryById(1L);

            assertThat(result).isEqualTo(sampleCategoryResponse);
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Events");
            verify(categoryRepository).findById(1L);
            verify(categoryMapper).toResponse(sampleCategory);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when category does not exist")
        void getCategoryById_notFound_throwsEntityNotFoundException() {
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> categoryService.getCategoryById(99L));

            assertThat(ex.getMessage()).isEqualTo("category not found");
            verify(categoryRepository).findById(99L);
            verifyNoInteractions(categoryMapper);
        }
    }

    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategories {

        @Test
        @DisplayName("returns mapped page of categories")
        void getAllCategories_returnsMappedPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> categoryPage = new PageImpl<>(List.of(sampleCategory), pageable, 1);
            when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
            when(categoryMapper.toResponse(sampleCategory)).thenReturn(sampleCategoryResponse);

            Page<CategoryResponse> result = categoryService.getAllCategories(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Events");
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(categoryRepository).findAll(pageable);
            verify(categoryMapper).toResponse(sampleCategory);
        }

        @Test
        @DisplayName("returns empty page when no categories exist")
        void getAllCategories_noCategories_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(categoryRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            Page<CategoryResponse> result = categoryService.getAllCategories(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verifyNoInteractions(categoryMapper);
        }

        @Test
        @DisplayName("maps each category in the page independently")
        void getAllCategories_multipleCategoriesMappedIndependently() {
            Category secondCategory = new Category();
            secondCategory.setId(2L);
            secondCategory.setName("Announcements");

            CategoryResponse secondResponse = CategoryResponse.builder()
                    .id(2L).name("Announcements").build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> categoryPage = new PageImpl<>(
                    List.of(sampleCategory, secondCategory), pageable, 2);

            when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
            when(categoryMapper.toResponse(sampleCategory)).thenReturn(sampleCategoryResponse);
            when(categoryMapper.toResponse(secondCategory)).thenReturn(secondResponse);

            Page<CategoryResponse> result = categoryService.getAllCategories(pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).extracting(CategoryResponse::getName)
                    .containsExactly("Events", "Announcements");
            verify(categoryMapper, times(2)).toResponse(any(Category.class));
        }

        @Test
        @DisplayName("passes pageable through to repository unchanged")
        void getAllCategories_passesPageableToRepository() {
            Pageable pageable = PageRequest.of(2, 5);
            Page<Category> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(categoryRepository.findAll(pageable)).thenReturn(emptyPage);

            categoryService.getAllCategories(pageable);

            verify(categoryRepository).findAll(pageable);
            verifyNoMoreInteractions(categoryRepository);
        }
    }
}