// test/java/com/amalitech/communityboard/controller/CommentControllerIntegrationTest.java
package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.config.TestSecurityConfig;
import com.amalitech.communityboard.dto.request.CommentRequest;
import com.amalitech.communityboard.dto.response.CommentResponse;
import com.amalitech.communityboard.security.UserSecurity;
import com.amalitech.communityboard.service.interfaces.CommentInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import(TestSecurityConfig.class)
public class CommentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentInterface commentService;

    @MockitoBean
    private UserSecurity userSecurity;

    @Test
    @WithMockUser(roles = "MEMBER")
    void createComment_WithValidData_ShouldReturn201() throws Exception {
        // Arrange
        CommentRequest request = new CommentRequest();
        request.setPostId(1L);
        request.setContent("Test comment");
        request.setParentCommentId(null);

        CommentResponse response = new CommentResponse();
        response.setId(1L);
        response.setPostId(1L);
        response.setUserId(2L);
        response.setContent("Test comment");
        response.setCreatedAt(LocalDateTime.now());

        when(commentService.createComment(any(CommentRequest.class), eq(2L))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("comment created"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createComment_AsAdmin_ShouldSucceed() throws Exception {
        // Similar test but with admin role
        CommentRequest request = new CommentRequest();
        request.setPostId(1L);
        request.setContent("Admin comment");

        CommentResponse response = new CommentResponse();
        response.setId(1L);
        response.setContent("Admin comment");

        when(commentService.createComment(any(CommentRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/v1/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void createComment_WithoutAuthentication_ShouldReturn401() throws Exception {
        CommentRequest request = new CommentRequest();
        request.setPostId(1L);
        request.setContent("Test comment");

        mockMvc.perform(post("/api/v1/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}