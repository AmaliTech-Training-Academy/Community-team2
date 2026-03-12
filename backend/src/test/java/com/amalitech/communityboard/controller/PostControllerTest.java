// test/java/com/amalitech/communityboard/controller/PostControllerTest.java
package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.request.PostFilter;
import com.amalitech.communityboard.dto.request.PostRequest;
import com.amalitech.communityboard.dto.request.PostUpdateRequest;
import com.amalitech.communityboard.dto.response.PostResponse;
import com.amalitech.communityboard.security.CustomUserDetails;
import com.amalitech.communityboard.service.interfaces.PostInterface;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PostControllerTest {

    @Mock
    private PostInterface postService;

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private PostResponse postResponse;
    private PostRequest postRequest;
    private PostUpdateRequest postUpdateRequest;
    private CustomUserDetails memberUserDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(postController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        postResponse = PostResponse.builder()
                .id(1L)
                .title("Test Post")
                .content("Test content")
                .createdAt(LocalDateTime.now())
                .userId(2L)
                .categoryId(1L)
                .imageUrl("http://test.com/image.jpg")
                .viewCount(0)
                .build();

        postRequest = new PostRequest();
        postRequest.setTitle("Test Post");
        postRequest.setContent("Test content");
        postRequest.setCategoryId(1L);

        postUpdateRequest = new PostUpdateRequest();
        postUpdateRequest.setTitle("Updated Post");
        postUpdateRequest.setContent("Updated content");
        postUpdateRequest.setCategoryId(2L);

        memberUserDetails = TestUserFactory.createMemberUser();

        authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(memberUserDetails);
    }

    @Test
    void createPost_WithValidDataAndImage_ShouldReturnCreatedPost() throws Exception {
        // Arrange
        when(postService.createPost(any(PostRequest.class), eq(2L), any())).thenReturn(postResponse);

        MockMultipartFile postPart = new MockMultipartFile(
                "post",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(postRequest)
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/posts")
                        .file(postPart)
                        .file(imagePart)
                        .principal(authentication)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("post created"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void createPost_WithValidDataAndNoImage_ShouldReturnCreatedPost() throws Exception {
        // Arrange
        when(postService.createPost(any(PostRequest.class), eq(2L), isNull())).thenReturn(postResponse);

        MockMultipartFile postPart = new MockMultipartFile(
                "post",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(postRequest)
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/posts")
                        .file(postPart)
                        .principal(authentication)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("post created"));
    }

    @Test
    void createPost_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        PostRequest invalidRequest = new PostRequest();
        invalidRequest.setTitle("");
        invalidRequest.setCategoryId(null);

        MockMultipartFile postPart = new MockMultipartFile(
                "post",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(invalidRequest)
        );

        // Act & Assert - No stubbing needed
        mockMvc.perform(multipart("/api/v1/posts")
                        .file(postPart)
                        .principal(authentication)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(postService, never()).createPost(any(), any(), any());
    }

    @Test
    void getAllPosts_WithFilter_ShouldReturnPageOfPosts() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<PostResponse> postPage = new PageImpl<>(List.of(postResponse), pageable, 1);

        when(postService.getAllPosts(any(PostFilter.class), any(Pageable.class))).thenReturn(postPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("title", "Test")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("posts retrieved"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void getPostById_WhenPostExists_ShouldReturnPost() throws Exception {
        // Arrange
        when(postService.getPostById(1L)).thenReturn(postResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("post retrieved"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getPostsByUser_WithFilter_ShouldReturnPageOfPosts() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<PostResponse> postPage = new PageImpl<>(List.of(postResponse), pageable, 1);

        when(postService.getPostByUserId(eq(2L), any(PostFilter.class), any(Pageable.class))).thenReturn(postPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/by-user/2")
                        .param("page", "0")
                        .param("size", "10")
                        .param("title", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("posts retrieved"));
    }

    @Test
    void updatePost_WithValidData_ShouldReturnUpdatedPost() throws Exception {
        // Arrange
        PostResponse updatedResponse = PostResponse.builder()
                .id(1L)
                .title("Updated Post")
                .content("Updated content")
                .build();

        when(postService.updatePost(eq(1L), any(PostUpdateRequest.class))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("post updated"))
                .andExpect(jsonPath("$.data.title").value("Updated Post"));
    }

    @Test
    void deletePost_WhenAuthorized_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(postService).deletePost(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/posts/1"))
                .andExpect(status().isNoContent());

        verify(postService, times(1)).deletePost(1L);
    }
}