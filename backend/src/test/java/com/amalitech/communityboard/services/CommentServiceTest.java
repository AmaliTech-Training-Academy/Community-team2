package com.amalitech.communityboard.services;

import com.amalitech.communityboard.dto.request.CommentRequest;
import com.amalitech.communityboard.dto.request.CommentUpdateRequest;
import com.amalitech.communityboard.dto.response.CommentResponse;
import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.mapping.CommentMapper;
import com.amalitech.communityboard.models.Comment;
import com.amalitech.communityboard.models.Post;
import com.amalitech.communityboard.models.User;
import com.amalitech.communityboard.repository.CommentRepository;
import com.amalitech.communityboard.repository.PostRepository;
import com.amalitech.communityboard.repository.UserRepository;
import com.amalitech.communityboard.service.implementations.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService Unit Tests")
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    private Post samplePost;
    private User sampleUser;
    private Comment sampleComment;
    private CommentResponse sampleCommentResponse;

    @BeforeEach
    void setUp() {
        samplePost = new Post();
        samplePost.setId(5L);
        samplePost.setTitle("Community Event");

        sampleUser = new User();
        sampleUser.setId(10L);
        sampleUser.setUsername("silas_dev");

        sampleComment = new Comment();
        sampleComment.setId(1L);
        sampleComment.setContent("Great initiative!");
        sampleComment.setPost(samplePost);
        sampleComment.setUser(sampleUser);
        sampleComment.setCreatedAt(LocalDateTime.now());

        sampleCommentResponse = new CommentResponse();
        sampleCommentResponse.setId(1L);
        sampleCommentResponse.setPostId(5L);
        sampleCommentResponse.setUserId(10L);
        sampleCommentResponse.setContent("Great initiative!");
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE COMMENT
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createComment")
    class CreateComment {

        private CommentRequest request;

        @BeforeEach
        void setUpRequest() {
            request = new CommentRequest();
            request.setPostId(5L);
            request.setContent("Great initiative!");
        }

        @Test
        @DisplayName("saves comment and returns response when no parent")
        void createComment_noParent_savesAndReturns() {
            when(postRepository.findById(5L)).thenReturn(Optional.of(samplePost));
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(commentMapper.toEntity(request)).thenReturn(sampleComment);
            when(commentRepository.save(sampleComment)).thenReturn(sampleComment);
            when(commentMapper.toResponse(sampleComment)).thenReturn(sampleCommentResponse);

            CommentResponse result = commentService.createComment(request, 10L);

            assertThat(result).isEqualTo(sampleCommentResponse);
            verify(commentRepository).save(sampleComment);
        }

        @Test
        @DisplayName("sets post and user on entity before saving")
        void createComment_setsPostAndUser() {
            when(postRepository.findById(5L)).thenReturn(Optional.of(samplePost));
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(commentMapper.toEntity(request)).thenReturn(sampleComment);
            when(commentRepository.save(any())).thenReturn(sampleComment);
            when(commentMapper.toResponse(sampleComment)).thenReturn(sampleCommentResponse);

            commentService.createComment(request, 10L);

            ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
            verify(commentRepository).save(captor.capture());
            assertThat(captor.getValue().getPost()).isEqualTo(samplePost);
            assertThat(captor.getValue().getUser()).isEqualTo(sampleUser);
        }

        @Test
        @DisplayName("sets parent comment when parentCommentId is provided")
        void createComment_withParent_setsParent() {
            request.setParentCommentId(3L);

            Comment parentComment = new Comment();
            parentComment.setId(3L);
            parentComment.setContent("Original comment");

            when(postRepository.findById(5L)).thenReturn(Optional.of(samplePost));
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(commentMapper.toEntity(request)).thenReturn(sampleComment);
            when(commentRepository.findById(3L)).thenReturn(Optional.of(parentComment));
            when(commentRepository.save(any())).thenReturn(sampleComment);
            when(commentMapper.toResponse(sampleComment)).thenReturn(sampleCommentResponse);

            commentService.createComment(request, 10L);

            ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
            verify(commentRepository).save(captor.capture());
            assertThat(captor.getValue().getParent()).isEqualTo(parentComment);
        }

        @Test
        @DisplayName("sets parent to null when parentCommentId is null")
        void createComment_nullParent_setsParentToNull() {
            request.setParentCommentId(null);

            when(postRepository.findById(5L)).thenReturn(Optional.of(samplePost));
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(commentMapper.toEntity(request)).thenReturn(sampleComment);
            when(commentRepository.save(any())).thenReturn(sampleComment);
            when(commentMapper.toResponse(sampleComment)).thenReturn(sampleCommentResponse);

            commentService.createComment(request, 10L);

            ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
            verify(commentRepository).save(captor.capture());
            assertThat(captor.getValue().getParent()).isNull();
            // parentCommentId is null so commentRepository.findById should never be called
            verify(commentRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("throws EntityNotFoundException when post not found")
        void createComment_postNotFound_throwsEntityNotFoundException() {
            when(postRepository.findById(5L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> commentService.createComment(request, 10L));

            verifyNoInteractions(userRepository, commentRepository);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when user not found")
        void createComment_userNotFound_throwsEntityNotFoundException() {
            when(postRepository.findById(5L)).thenReturn(Optional.of(samplePost));
            when(userRepository.findById(10L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> commentService.createComment(request, 10L));

            verifyNoInteractions(commentRepository);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when parent comment not found")
        void createComment_parentNotFound_throwsEntityNotFoundException() {
            request.setParentCommentId(999L);

            when(postRepository.findById(5L)).thenReturn(Optional.of(samplePost));
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(commentMapper.toEntity(request)).thenReturn(sampleComment);
            when(commentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> commentService.createComment(request, 10L));

            verify(commentRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET COMMENT BY ID
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getCommentById")
    class GetCommentById {

        @Test
        @DisplayName("returns mapped response when found")
        void getCommentById_found_returnsResponse() {
            when(commentRepository.findById(1L)).thenReturn(Optional.of(sampleComment));
            when(commentMapper.toResponse(sampleComment)).thenReturn(sampleCommentResponse);

            CommentResponse result = commentService.getCommentById(1L);

            assertThat(result).isEqualTo(sampleCommentResponse);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when comment not found")
        void getCommentById_notFound_throwsEntityNotFoundException() {
            when(commentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> commentService.getCommentById(99L));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET ALL COMMENTS
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllComments")
    class GetAllComments {

        @Test
        @DisplayName("returns mapped page of all comments")
        void getAllComments_returnsMappedPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Comment> commentPage = new PageImpl<>(List.of(sampleComment), pageable, 1);
            when(commentRepository.findAll(pageable)).thenReturn(commentPage);
            when(commentMapper.toResponse(sampleComment)).thenReturn(sampleCommentResponse);

            Page<CommentResponse> result = commentService.getAllComments(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getContent()).isEqualTo("Great initiative!");
        }

        @Test
        @DisplayName("returns empty page when no comments exist")
        void getAllComments_noComments_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(commentRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

            Page<CommentResponse> result = commentService.getAllComments(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET COMMENTS BY POST ID
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getCommentByPostId")
    class GetCommentByPostId {

        @Test
        @DisplayName("returns mapped page of comments for given postId")
        void getCommentByPostId_returnsMappedPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Comment> page = new PageImpl<>(List.of(sampleComment), pageable, 1);
            when(commentRepository.findByPostId(5L, pageable)).thenReturn(page);
            when(commentMapper.toResponse(sampleComment)).thenReturn(sampleCommentResponse);

            Page<CommentResponse> result = commentService.getCommentByPostId(5L, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(commentRepository).findByPostId(5L, pageable);
        }

        @Test
        @DisplayName("returns empty page when post has no comments")
        void getCommentByPostId_noComments_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(commentRepository.findByPostId(5L, pageable)).thenReturn(new PageImpl<>(List.of()));

            Page<CommentResponse> result = commentService.getCommentByPostId(5L, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET COMMENTS BY USER ID
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getCommentByUserId")
    class GetCommentByUserId {

        @Test
        @DisplayName("returns mapped page of comments for given userId")
        void getCommentByUserId_returnsMappedPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Comment> page = new PageImpl<>(List.of(sampleComment), pageable, 1);
            when(commentRepository.findByUserId(10L, pageable)).thenReturn(page);
            when(commentMapper.toResponse(sampleComment)).thenReturn(sampleCommentResponse);

            Page<CommentResponse> result = commentService.getCommentByUserId(10L, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(commentRepository).findByUserId(10L, pageable);
        }

        @Test
        @DisplayName("returns empty page when user has no comments")
        void getCommentByUserId_noComments_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(commentRepository.findByUserId(10L, pageable)).thenReturn(new PageImpl<>(List.of()));

            Page<CommentResponse> result = commentService.getCommentByUserId(10L, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE COMMENT
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateComment")
    class UpdateComment {

        @Test
        @DisplayName("updates content and returns mapped response")
        void updateComment_valid_updatesContentAndReturns() {
            CommentUpdateRequest updateRequest = new CommentUpdateRequest();
            updateRequest.setContent("Updated content.");

            CommentResponse updatedResponse = new CommentResponse();
            updatedResponse.setId(1L);
            updatedResponse.setContent("Updated content.");

            when(commentRepository.findById(1L)).thenReturn(Optional.of(sampleComment));
            when(commentMapper.toResponse(sampleComment)).thenReturn(updatedResponse);

            CommentResponse result = commentService.updateComment(1L, updateRequest);

            assertThat(sampleComment.getContent()).isEqualTo("Updated content.");
            assertThat(result.getContent()).isEqualTo("Updated content.");
            // No explicit save call needed — dirty checking handles it under @Transactional
            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws EntityNotFoundException when comment not found")
        void updateComment_notFound_throwsEntityNotFoundException() {
            CommentUpdateRequest updateRequest = new CommentUpdateRequest();
            updateRequest.setContent("Something.");
            when(commentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> commentService.updateComment(99L, updateRequest));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE COMMENT
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteComment")
    class DeleteComment {

        @Test
        @DisplayName("deletes comment when it exists")
        void deleteComment_exists_deletesSuccessfully() {
            when(commentRepository.existsById(1L)).thenReturn(true);
            doNothing().when(commentRepository).deleteById(1L);

            commentService.deleteComment(1L);

            verify(commentRepository).deleteById(1L);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when comment does not exist")
        void deleteComment_notFound_throwsEntityNotFoundException() {
            when(commentRepository.existsById(99L)).thenReturn(false);

            assertThrows(EntityNotFoundException.class,
                    () -> commentService.deleteComment(99L));

            verify(commentRepository, never()).deleteById(anyLong());
        }
    }
}