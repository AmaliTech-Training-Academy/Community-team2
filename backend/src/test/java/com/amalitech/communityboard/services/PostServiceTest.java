package com.amalitech.communityboard.services;

import com.amalitech.communityboard.dto.request.PostFilter;
import com.amalitech.communityboard.dto.request.PostRequest;
import com.amalitech.communityboard.dto.request.PostUpdateRequest;
import com.amalitech.communityboard.dto.response.PostResponse;
import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.mapping.PostMapper;
import com.amalitech.communityboard.models.Category;
import com.amalitech.communityboard.models.Post;
import com.amalitech.communityboard.models.User;
import com.amalitech.communityboard.repository.CategoryRepository;
import com.amalitech.communityboard.repository.PostRepository;
import com.amalitech.communityboard.repository.UserRepository;
import com.amalitech.communityboard.service.implementations.CloudinaryService;
import com.amalitech.communityboard.service.implementations.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Unit Tests")
class PostServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private PostMapper postMapper;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PostService postService;

    private User sampleUser;
    private Category sampleCategory;
    private Post samplePost;
    private PostResponse samplePostResponse;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(10L);
        sampleUser.setUsername("silas_dev");
        sampleUser.setEmail("silas@amalitech.com");

        sampleCategory = new Category();
        sampleCategory.setId(2L);
        sampleCategory.setName("Events");

        samplePost = new Post();
        samplePost.setId(1L);
        samplePost.setTitle("Community Event");
        samplePost.setContent("Join us this Saturday.");
        samplePost.setAuthor(sampleUser);
        samplePost.setCategory(sampleCategory);
        samplePost.setViewCount(0);
        samplePost.setCreatedAt(LocalDateTime.now());

        samplePostResponse = PostResponse.builder()
                .id(1L)
                .title("Community Event")
                .content("Join us this Saturday.")
                .userId(10L)
                .categoryId(2L)
                .viewCount(0)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE POST
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createPost")
    class CreatePost {

        private PostRequest postRequest;

        @BeforeEach
        void setUpRequest() {
            postRequest = PostRequest.builder()
                    .title("Community Event")
                    .content("Join us this Saturday.")
                    .categoryId(2L)
                    .build();
        }

        @Test
        @DisplayName("saves post and returns response when no image provided")
        void createPost_noImage_savesAndReturns() {
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(sampleCategory));
            when(postMapper.toEntity(postRequest)).thenReturn(samplePost);
            when(postRepository.save(any(Post.class))).thenReturn(samplePost);
            when(postMapper.toResponse(samplePost)).thenReturn(samplePostResponse);

            PostResponse result = postService.createPost(postRequest, 10L, null);

            assertThat(result).isEqualTo(samplePostResponse);
            verify(postRepository).save(samplePost);
            verify(eventPublisher).publishEvent(any());

            // imageUrl should be null when no image provided
            ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getImageUrl()).isNull();
        }

        @Test
        @DisplayName("uploads image and sets imageUrl on post")
        void createPost_withImage_uploadsAndSetsImageUrl() {
            MultipartFile image = new MockMultipartFile("image", "event.jpg",
                    "image/jpeg", "fake-bytes".getBytes());

            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(sampleCategory));
            when(postMapper.toEntity(postRequest)).thenReturn(samplePost);
            when(cloudinaryService.uploadImage(image))
                    .thenReturn(CompletableFuture.completedFuture("https://cdn.cloudinary.com/event.jpg"));
            when(postRepository.save(any(Post.class))).thenReturn(samplePost);
            when(postMapper.toResponse(samplePost)).thenReturn(samplePostResponse);

            postService.createPost(postRequest, 10L, image);

            ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getImageUrl()).isEqualTo("https://cdn.cloudinary.com/event.jpg");
        }

        @Test
        @DisplayName("saves post without imageUrl when Cloudinary upload fails")
        void createPost_imageUploadFails_savesPostWithoutImage() {
            MultipartFile image = new MockMultipartFile("image", "event.jpg",
                    "image/jpeg", "fake-bytes".getBytes());

            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(sampleCategory));
            when(postMapper.toEntity(postRequest)).thenReturn(samplePost);

            CompletableFuture<String> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("Cloudinary unavailable"));
            when(cloudinaryService.uploadImage(image)).thenReturn(failedFuture);

            when(postRepository.save(any(Post.class))).thenReturn(samplePost);
            when(postMapper.toResponse(samplePost)).thenReturn(samplePostResponse);

            // Should not throw — graceful degradation
            PostResponse result = postService.createPost(postRequest, 10L, image);

            assertThat(result).isNotNull();
            ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getImageUrl()).isNull();
        }

        @Test
        @DisplayName("sets author and category on post entity before saving")
        void createPost_setsAuthorAndCategory() {
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(sampleCategory));
            when(postMapper.toEntity(postRequest)).thenReturn(samplePost);
            when(postRepository.save(any(Post.class))).thenReturn(samplePost);
            when(postMapper.toResponse(samplePost)).thenReturn(samplePostResponse);

            postService.createPost(postRequest, 10L, null);

            ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
            verify(postRepository).save(captor.capture());
            assertThat(captor.getValue().getAuthor()).isEqualTo(sampleUser);
            assertThat(captor.getValue().getCategory()).isEqualTo(sampleCategory);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when user not found")
        void createPost_userNotFound_throwsEntityNotFoundException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> postService.createPost(postRequest, 99L, null));

            verifyNoInteractions(postRepository, categoryRepository);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when category not found")
        void createPost_categoryNotFound_throwsEntityNotFoundException() {
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> postService.createPost(postRequest, 10L, null));

            verifyNoInteractions(postRepository);
        }

        @Test
        @DisplayName("publishes PostCreatedEvent after saving")
        void createPost_publishesEvent() {
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(sampleCategory));
            when(postMapper.toEntity(postRequest)).thenReturn(samplePost);
            when(postRepository.save(any(Post.class))).thenReturn(samplePost);
            when(postMapper.toResponse(samplePost)).thenReturn(samplePostResponse);

            postService.createPost(postRequest, 10L, null);

            verify(eventPublisher).publishEvent(any());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET POST BY ID
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getPostById")
    class GetPostById {

        @Test
        @DisplayName("returns mapped response and increments view count")
        void getPostById_found_returnsPostAndIncrementsViews() {
            samplePost.setViewCount(5);
            when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));
            when(postMapper.toResponse(samplePost)).thenReturn(samplePostResponse);

            postService.getPostById(1L);

            assertThat(samplePost.getViewCount()).isEqualTo(6);
            verify(postMapper).toResponse(samplePost);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when post not found")
        void getPostById_notFound_throwsEntityNotFoundException() {
            when(postRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> postService.getPostById(99L));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET ALL POSTS
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllPosts")
    class GetAllPosts {

        @Test
        @DisplayName("returns mapped page of posts")
        void getAllPosts_returnsMappedPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> postPage = new PageImpl<>(List.of(samplePost), pageable, 1);
            when(postRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(postPage);
            when(postMapper.toResponse(samplePost)).thenReturn(samplePostResponse);

            Page<PostResponse> result = postService.getAllPosts(new PostFilter(), pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Community Event");
        }

        @Test
        @DisplayName("returns empty page when no posts match filter")
        void getAllPosts_noMatches_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(postRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

            Page<PostResponse> result = postService.getAllPosts(new PostFilter(), pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET POSTS BY USER ID
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getPostByUserId")
    class GetPostByUserId {

        @Test
        @DisplayName("sets authorId on filter and delegates to getAllPosts")
        void getPostByUserId_setsAuthorIdOnFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> postPage = new PageImpl<>(List.of(samplePost), pageable, 1);
            when(postRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(postPage);
            when(postMapper.toResponse(samplePost)).thenReturn(samplePostResponse);

            PostFilter filter = new PostFilter();
            postService.getPostByUserId(10L, filter, pageable);

            // The service mutates the filter to set authorId
            assertThat(filter.getAuthorId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("creates a new filter when null is passed")
        void getPostByUserId_nullFilter_createsNewFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(postRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

            // Should not throw NPE
            Page<PostResponse> result = postService.getPostByUserId(10L, null, pageable);

            assertThat(result).isNotNull();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE POST
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updatePost")
    class UpdatePost {

        @Test
        @DisplayName("updates title only when content and categoryId are null")
        void updatePost_titleOnly_updatesTitleOnly() {
            PostUpdateRequest request = new PostUpdateRequest("New Title", null, null);
            when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));
            when(postMapper.toResponse(samplePost)).thenReturn(samplePostResponse);

            postService.updatePost(1L, request);

            assertThat(samplePost.getTitle()).isEqualTo("New Title");
            assertThat(samplePost.getContent()).isEqualTo("Join us this Saturday."); // unchanged
        }

        @Test
        @DisplayName("updates content only when title and categoryId are null")
        void updatePost_contentOnly_updatesContentOnly() {
            PostUpdateRequest request = new PostUpdateRequest(null, "Updated content here.", null);
            when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));
            when(postMapper.toResponse(samplePost)).thenReturn(samplePostResponse);

            postService.updatePost(1L, request);

            assertThat(samplePost.getContent()).isEqualTo("Updated content here.");
            assertThat(samplePost.getTitle()).isEqualTo("Community Event"); // unchanged
        }

        @Test
        @DisplayName("updates category when new categoryId is provided and exists")
        void updatePost_newCategory_updatesCategory() {
            Category newCategory = new Category();
            newCategory.setId(5L);
            newCategory.setName("Announcements");

            PostUpdateRequest request = new PostUpdateRequest(null, null, 5L);
            when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));
            when(categoryRepository.findById(5L)).thenReturn(Optional.of(newCategory));
            when(postMapper.toResponse(samplePost)).thenReturn(samplePostResponse);

            postService.updatePost(1L, request);

            assertThat(samplePost.getCategory()).isEqualTo(newCategory);
        }

        @Test
        @DisplayName("does not update title when blank string is passed")
        void updatePost_blankTitle_doesNotUpdateTitle() {
            PostUpdateRequest request = new PostUpdateRequest("   ", null, null);
            when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));
            when(postMapper.toResponse(samplePost)).thenReturn(samplePostResponse);

            postService.updatePost(1L, request);

            assertThat(samplePost.getTitle()).isEqualTo("Community Event"); // unchanged
        }

        @Test
        @DisplayName("throws EntityNotFoundException when post not found")
        void updatePost_postNotFound_throwsEntityNotFoundException() {
            PostUpdateRequest request = new PostUpdateRequest("Title", null, null);
            when(postRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> postService.updatePost(99L, request));
        }

        @Test
        @DisplayName("throws EntityNotFoundException when new category not found")
        void updatePost_categoryNotFound_throwsEntityNotFoundException() {
            PostUpdateRequest request = new PostUpdateRequest(null, null, 999L);
            when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> postService.updatePost(1L, request));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE POST
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deletePost")
    class DeletePost {

        @Test
        @DisplayName("deletes post when it exists")
        void deletePost_exists_deletesSuccessfully() {
            when(postRepository.existsById(1L)).thenReturn(true);
            doNothing().when(postRepository).deleteById(1L);

            postService.deletePost(1L);

            verify(postRepository).deleteById(1L);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when post does not exist")
        void deletePost_notFound_throwsEntityNotFoundException() {
            when(postRepository.existsById(99L)).thenReturn(false);

            assertThrows(EntityNotFoundException.class,
                    () -> postService.deletePost(99L));

            verify(postRepository, never()).deleteById(anyLong());
        }
    }
}