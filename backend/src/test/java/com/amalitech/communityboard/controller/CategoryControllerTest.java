// test/java/com/amalitech/communityboard/controller/CategoryControllerTest.java
package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.response.CategoryResponse;
import com.amalitech.communityboard.service.interfaces.CategoryInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    @Mock
    private CategoryInterface categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(categoryController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        categoryResponse = new CategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Technology");
        categoryResponse.setDescription("Tech related posts");
        categoryResponse.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllCategories_ShouldReturnPageOfCategories() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryResponse> categoryPage = new PageImpl<>(List.of(categoryResponse), pageable, 1);

        when(categoryService.getAllCategories(any(Pageable.class))).thenReturn(categoryPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("categories retrieved"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Technology"));
    }

    @Test
    void getAllCategories_WithDefaultPagination_ShouldUseDefaultPageable() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryResponse> categoryPage = new PageImpl<>(List.of(categoryResponse), pageable, 1);

        when(categoryService.getAllCategories(any(Pageable.class))).thenReturn(categoryPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("categories retrieved"));
    }

    @Test
    void getCategoryById_WhenCategoryExists_ShouldReturnCategory() throws Exception {
        // Arrange
        when(categoryService.getCategoryById(1L)).thenReturn(categoryResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("category retrieved"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Technology"));
    }
}