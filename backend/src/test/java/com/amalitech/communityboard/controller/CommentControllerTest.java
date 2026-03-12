// test/java/com/amalitech/communityboard/controller/CommentControllerTest.java
package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.request.CommentRequest;
import com.amalitech.communityboard.dto.request.CommentUpdateRequest;
import com.amalitech.communityboard.dto.response.CommentResponse;
import com.amalitech.communityboard.security.CustomUserDetails;
import com.amalitech.communityboard.service.interfaces.CommentInterface;
import com.amalitech.communityboard.util.TestUserFactory;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CommentControllerTest {

    @Mock
    private CommentInterface commentService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private CommentResponse commentResponse;
    private CommentRequest commentRequest;
    private CommentUpdateRequest commentUpdateRequest;
    private CustomUserDetails memberUserDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(commentController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        commentResponse = new CommentResponse();
        commentResponse.setId(1L);
        commentResponse.setPostId(1L);
        commentResponse.setUserId(2L);
        commentResponse.setContent("Test comment");
        commentResponse.setCreatedAt(LocalDateTime.now());
        commentResponse.setParentCommentId(null);

        commentRequest = new CommentRequest();
        commentRequest.setPostId(1L);
        commentRequest.setContent("Test comment");
        commentRequest.setParentCommentId(null);

        commentUpdateRequest = new CommentUpdateRequest();
        commentUpdateRequest.setContent("Updated comment");

        memberUserDetails = TestUserFactory.createMemberUser();
    }

    @Test
    void createComment_WithValidData_ShouldReturnCreatedComment() throws Exception {
        // Arrange
        when(commentService.createComment(any(CommentRequest.class), eq(2L))).thenReturn(commentResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest))
                        .principal(() -> memberUserDetails)) // Use lambda to provide principal
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("comment created"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.content").value("Test comment"));

        verify(commentService, times(1)).createComment(any(CommentRequest.class), eq(2L));
    }

    @Test
    void createComment_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        CommentRequest invalidRequest = new CommentRequest();
        invalidRequest.setPostId(null);
        invalidRequest.setContent("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .principal(() -> memberUserDetails))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).createComment(any(), any());
    }

    @Test
    void getAllComments_ShouldReturnPageOfComments() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentResponse> commentPage = new PageImpl<>(List.of(commentResponse), pageable, 1);

        when(commentService.getAllComments(any(Pageable.class))).thenReturn(commentPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("comments retrieved"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void getCommentById_WhenCommentExists_ShouldReturnComment() throws Exception {
        // Arrange
        when(commentService.getCommentById(1L)).thenReturn(commentResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("comment retrieved"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getCommentsByPost_ShouldReturnPageOfComments() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentResponse> commentPage = new PageImpl<>(List.of(commentResponse), pageable, 1);

        when(commentService.getCommentByPostId(eq(1L), any(Pageable.class))).thenReturn(commentPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments/by-post/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("comments retrieved"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void getCommentsByUser_ShouldReturnPageOfComments() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentResponse> commentPage = new PageImpl<>(List.of(commentResponse), pageable, 1);

        when(commentService.getCommentByUserId(eq(2L), any(Pageable.class))).thenReturn(commentPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments/by-user/2")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("comments retrieved"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void updateComment_WithValidData_ShouldReturnUpdatedComment() throws Exception {
        // Arrange
        CommentResponse updatedResponse = new CommentResponse();
        updatedResponse.setId(1L);
        updatedResponse.setContent("Updated comment");

        when(commentService.updateComment(eq(1L), any(CommentUpdateRequest.class))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("comment updated"))
                .andExpect(jsonPath("$.data.content").value("Updated comment"));
    }

    @Test
    void deleteComment_WhenAuthorized_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(commentService).deleteComment(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/comments/1"))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteComment(1L);
    }
}