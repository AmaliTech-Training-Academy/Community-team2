package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.ResponseDto;
import com.amalitech.communityboard.dto.request.PostFilter;
import com.amalitech.communityboard.dto.request.PostRequest;
import com.amalitech.communityboard.dto.request.PostUpdateRequest;
import com.amalitech.communityboard.dto.response.PostResponse;
import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.security.CustomUserDetails;
import com.amalitech.communityboard.service.interfaces.PostInterface;
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
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostController Unit Tests")
class PostControllerTest {

    @Mock
    private PostInterface postService;

    @Mock
    private CustomUserDetails principal;

    @InjectMocks
    private PostController postController;

    private PostResponse samplePostResponse;
    private PostRequest samplePostRequest;

    @BeforeEach
    void setUp() {
        samplePostResponse = PostResponse.builder()
                .id(1L)
                .title("Community Event")
                .content("Join us this Saturday for the neighbourhood cleanup.")
                .userId(10L)
                .categoryId(2L)
                .createdAt(LocalDateTime.now())
                .viewCount(0)
                .build();

        samplePostRequest = PostRequest.builder()
                .title("Community Event")
                .content("Join us this Saturday for the neighbourhood cleanup.")
                .categoryId(2L)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE POST
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST / – createPost")
    class CreatePost {

        @Test
        @DisplayName("returns 201 with post data when request is valid and no image")
        void createPost_noImage_returns201() {
            when(principal.getId()).thenReturn(10L);
            when(postService.createPost(any(PostRequest.class), eq(10L), isNull()))
                    .thenReturn(samplePostResponse);

            ResponseDto<PostResponse> response = postController.createPost(samplePostRequest, principal, null);

            assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.message()).isEqualTo("post created");
            assertThat(response.data().getTitle()).isEqualTo("Community Event");
            assertThat(response.data().getUserId()).isEqualTo(10L);
            verify(postService).createPost(samplePostRequest, 10L, null);
        }

        @Test
        @DisplayName("returns 201 with imageUrl when image is uploaded")
        void createPost_withImage_returns201() {
            MultipartFile image = new MockMultipartFile("image", "event.jpg",
                    "image/jpeg", "fake-image-bytes".getBytes());

            PostResponse withImage = PostResponse.builder()
                    .id(1L).title("Community Event").userId(10L).categoryId(2L)
                    .imageUrl("https://cdn.cloudinary.com/event.jpg")
                    .build();

            when(principal.getId()).thenReturn(10L);
            when(postService.createPost(any(PostRequest.class), eq(10L), eq(image)))
                    .thenReturn(withImage);

            ResponseDto<PostResponse> response = postController.createPost(samplePostRequest, principal, image);

            assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.data().getImageUrl()).isEqualTo("https://cdn.cloudinary.com/event.jpg");
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when category not found")
        void createPost_categoryNotFound_throwsEntityNotFoundException() {
            when(principal.getId()).thenReturn(10L);
            when(postService.createPost(any(), anyLong(), any()))
                    .thenThrow(new EntityNotFoundException("category not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> postController.createPost(samplePostRequest, principal, null));
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when user not found")
        void createPost_userNotFound_throwsEntityNotFoundException() {
            when(principal.getId()).thenReturn(99L);
            when(postService.createPost(any(), eq(99L), any()))
                    .thenThrow(new EntityNotFoundException("user not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> postController.createPost(samplePostRequest, principal, null));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET ALL POSTS
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET / – getAllPosts")
    class GetAllPosts {

        @Test
        @DisplayName("returns 200 with paginated posts")
        void getAllPosts_returnsPaginatedList() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<PostResponse> page = new PageImpl<>(List.of(samplePostResponse), pageable, 1);
            when(postService.getAllPosts(any(PostFilter.class), any(Pageable.class))).thenReturn(page);

            ResponseDto<Page<PostResponse>> response = postController.getAllPosts(new PostFilter(), pageable);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("posts retrieved");
            assertThat(response.data().getContent()).hasSize(1);
            assertThat(response.data().getContent().get(0).getTitle()).isEqualTo("Community Event");
        }

        @Test
        @DisplayName("returns empty page when no posts match filter")
        void getAllPosts_noMatches_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<PostResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(postService.getAllPosts(any(PostFilter.class), any(Pageable.class))).thenReturn(emptyPage);

            ResponseDto<Page<PostResponse>> response = postController.getAllPosts(new PostFilter(), pageable);

            assertThat(response.data().getContent()).isEmpty();
            assertThat(response.data().getTotalElements()).isZero();
        }

        @Test
        @DisplayName("passes filter fields through to service correctly")
        void getAllPosts_withFilter_passesFilterToService() {
            PostFilter filter = new PostFilter();
            filter.setTitle("cleanup");
            filter.setCategoryId(2L);
            Pageable pageable = PageRequest.of(0, 10);
            Page<PostResponse> page = new PageImpl<>(List.of(samplePostResponse), pageable, 1);

            when(postService.getAllPosts(eq(filter), any(Pageable.class))).thenReturn(page);

            ResponseDto<Page<PostResponse>> response = postController.getAllPosts(filter, pageable);

            assertThat(response.data().getContent()).hasSize(1);
            verify(postService).getAllPosts(filter, pageable);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET POST BY ID
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /{id} – getPostById")
    class GetPostById {

        @Test
        @DisplayName("returns 200 and post when found")
        void getPostById_found_returnsPost() {
            when(postService.getPostById(1L)).thenReturn(samplePostResponse);

            ResponseDto<PostResponse> response = postController.getPostById(1L);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("post retrieved");
            assertThat(response.data().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when post not found")
        void getPostById_notFound_throwsEntityNotFoundException() {
            when(postService.getPostById(99L))
                    .thenThrow(new EntityNotFoundException("post not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> postController.getPostById(99L));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET POSTS BY USER
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /by-user/{userId} – getPostsByUser")
    class GetPostsByUser {

        @Test
        @DisplayName("returns 200 with posts for given userId")
        void getPostsByUser_validUser_returnsPosts() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<PostResponse> page = new PageImpl<>(List.of(samplePostResponse), pageable, 1);
            when(postService.getPostByUserId(eq(10L), any(PostFilter.class), any(Pageable.class)))
                    .thenReturn(page);

            ResponseDto<Page<PostResponse>> response =
                    postController.getPostsByUser(10L, new PostFilter(), pageable);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("posts retrieved");
            assertThat(response.data().getContent()).hasSize(1);
            verify(postService).getPostByUserId(eq(10L), any(), eq(pageable));
        }

        @Test
        @DisplayName("returns empty page when user has no posts")
        void getPostsByUser_noPosts_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<PostResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(postService.getPostByUserId(anyLong(), any(), any())).thenReturn(emptyPage);

            ResponseDto<Page<PostResponse>> response =
                    postController.getPostsByUser(10L, new PostFilter(), pageable);

            assertThat(response.data().getContent()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE POST
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /{id} – updatePost")
    class UpdatePost {

        @Test
        @DisplayName("returns 200 with updated post data")
        void updatePost_validRequest_returnsUpdated() {
            PostUpdateRequest updateRequest = new PostUpdateRequest("Updated Title", null, null);
            PostResponse updated = PostResponse.builder()
                    .id(1L).title("Updated Title").content("Join us this Saturday for the neighbourhood cleanup.")
                    .userId(10L).categoryId(2L).build();

            when(postService.updatePost(eq(1L), any(PostUpdateRequest.class))).thenReturn(updated);

            ResponseDto<PostResponse> response = postController.updatePost(1L, updateRequest);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("post updated");
            assertThat(response.data().getTitle()).isEqualTo("Updated Title");
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when post not found")
        void updatePost_notFound_throwsEntityNotFoundException() {
            PostUpdateRequest updateRequest = new PostUpdateRequest("Title", null, null);
            when(postService.updatePost(anyLong(), any()))
                    .thenThrow(new EntityNotFoundException("post not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> postController.updatePost(99L, updateRequest));
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when new category not found")
        void updatePost_categoryNotFound_throwsEntityNotFoundException() {
            PostUpdateRequest updateRequest = new PostUpdateRequest(null, null, 999L);
            when(postService.updatePost(anyLong(), any()))
                    .thenThrow(new EntityNotFoundException("category not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> postController.updatePost(1L, updateRequest));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE POST
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /{id} – deletePost")
    class DeletePost {

        @Test
        @DisplayName("returns 204 No Content when post is deleted")
        void deletePost_exists_returns204() {
            doNothing().when(postService).deletePost(1L);

            ResponseEntity<Void> response = postController.deletePost(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(postService).deletePost(1L);
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when post not found")
        void deletePost_notFound_throwsEntityNotFoundException() {
            doThrow(new EntityNotFoundException("post not found")).when(postService).deletePost(99L);

            assertThrows(EntityNotFoundException.class,
                    () -> postController.deletePost(99L));
        }
    }
}