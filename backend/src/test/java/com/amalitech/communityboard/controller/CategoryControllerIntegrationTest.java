// test/java/com/amalitech/communityboard/controller/CategoryControllerIntegrationTest.java
package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.config.TestSecurityConfig;
import com.amalitech.communityboard.dto.response.CategoryResponse;
import com.amalitech.communityboard.service.interfaces.CategoryInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(TestSecurityConfig.class)
public class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryInterface categoryService;

    @Test
    @WithMockUser(roles = "MEMBER")
    void getAllCategories_WithAuthenticatedUser_ShouldReturnCategories() throws Exception {
        // Arrange
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Technology");
        categoryResponse.setDescription("Tech posts");
        categoryResponse.setCreatedAt(LocalDateTime.now());

        Page<CategoryResponse> categoryPage = new PageImpl<>(
                List.of(categoryResponse),
                PageRequest.of(0, 10),
                1
        );

        when(categoryService.getAllCategories(any(Pageable.class))).thenReturn(categoryPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("categories retrieved"))
                .andExpect(jsonPath("$.data.content[0].name").value("Technology"));
    }
}