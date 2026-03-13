package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.ResponseDto;
import com.amalitech.communityboard.dto.request.CommentRequest;
import com.amalitech.communityboard.dto.request.CommentUpdateRequest;
import com.amalitech.communityboard.dto.response.CommentResponse;
import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.security.CustomUserDetails;
import com.amalitech.communityboard.service.interfaces.CommentInterface;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentController Unit Tests")
class CommentControllerTest {

    @Mock
    private CommentInterface commentService;

    @Mock
    private CustomUserDetails userDetails;

    @InjectMocks
    private CommentController commentController;

    private CommentResponse sampleComment;
    private CommentRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleComment = new CommentResponse();
        sampleComment.setId(1L);
        sampleComment.setPostId(5L);
        sampleComment.setUserId(10L);
        sampleComment.setContent("Great initiative!");
        sampleComment.setCreatedAt(LocalDateTime.now());

        sampleRequest = new CommentRequest();
        sampleRequest.setPostId(5L);
        sampleRequest.setContent("Great initiative!");
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE COMMENT
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST / – createComment")
    class CreateComment {

        @Test
        @DisplayName("returns 201 with comment data on valid request")
        void createComment_valid_returns201() {
            when(userDetails.getId()).thenReturn(10L);
            when(commentService.createComment(any(CommentRequest.class), eq(10L)))
                    .thenReturn(sampleComment);

            ResponseDto<CommentResponse> response = commentController.createComment(sampleRequest, userDetails);

            assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.message()).isEqualTo("comment created");
            assertThat(response.data().getContent()).isEqualTo("Great initiative!");
            assertThat(response.data().getUserId()).isEqualTo(10L);
            verify(commentService).createComment(sampleRequest, 10L);
        }

        @Test
        @DisplayName("returns 201 with parentCommentId set for a threaded reply")
        void createComment_threadedReply_returns201() {
            sampleRequest.setParentCommentId(3L);
            CommentResponse reply = new CommentResponse();
            reply.setId(2L);
            reply.setPostId(5L);
            reply.setUserId(10L);
            reply.setContent("Great initiative!");
            reply.setParentCommentId(3L);

            when(userDetails.getId()).thenReturn(10L);
            when(commentService.createComment(any(CommentRequest.class), eq(10L))).thenReturn(reply);

            ResponseDto<CommentResponse> response = commentController.createComment(sampleRequest, userDetails);

            assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.data().getParentCommentId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when post not found")
        void createComment_postNotFound_throwsEntityNotFoundException() {
            when(userDetails.getId()).thenReturn(10L);
            when(commentService.createComment(any(), anyLong()))
                    .thenThrow(new EntityNotFoundException("Post not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> commentController.createComment(sampleRequest, userDetails));
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when user not found")
        void createComment_userNotFound_throwsEntityNotFoundException() {
            when(userDetails.getId()).thenReturn(99L);
            when(commentService.createComment(any(), eq(99L)))
                    .thenThrow(new EntityNotFoundException("User not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> commentController.createComment(sampleRequest, userDetails));
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when parent comment not found")
        void createComment_parentNotFound_throwsEntityNotFoundException() {
            sampleRequest.setParentCommentId(999L);
            when(userDetails.getId()).thenReturn(10L);
            when(commentService.createComment(any(), anyLong()))
                    .thenThrow(new EntityNotFoundException("Parent comment not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> commentController.createComment(sampleRequest, userDetails));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET ALL COMMENTS
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET / – getAllComments")
    class GetAllComments {

        @Test
        @DisplayName("returns 200 with paginated comments")
        void getAllComments_returnsPaginatedList() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CommentResponse> page = new PageImpl<>(List.of(sampleComment), pageable, 1);
            when(commentService.getAllComments(any(Pageable.class))).thenReturn(page);

            ResponseDto<Page<CommentResponse>> response = commentController.getAllComments(pageable);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("comments retrieved");
            assertThat(response.data().getContent()).hasSize(1);
            assertThat(response.data().getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("returns empty page when no comments exist")
        void getAllComments_noComments_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(commentService.getAllComments(any())).thenReturn(new PageImpl<>(List.of()));

            ResponseDto<Page<CommentResponse>> response = commentController.getAllComments(pageable);

            assertThat(response.data().getContent()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET COMMENT BY ID
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /{id} – getCommentById")
    class GetCommentById {

        @Test
        @DisplayName("returns 200 and comment when found")
        void getCommentById_found_returnsComment() {
            when(commentService.getCommentById(1L)).thenReturn(sampleComment);

            ResponseDto<CommentResponse> response = commentController.getCommentById(1L);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("comment retrieved");
            assertThat(response.data().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when comment not found")
        void getCommentById_notFound_throwsEntityNotFoundException() {
            when(commentService.getCommentById(99L))
                    .thenThrow(new EntityNotFoundException("Comment not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> commentController.getCommentById(99L));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET COMMENTS BY POST
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /by-post/{postId} – getCommentsByPost")
    class GetCommentsByPost {

        @Test
        @DisplayName("returns 200 with comments for given postId")
        void getCommentsByPost_validPost_returnsComments() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CommentResponse> page = new PageImpl<>(List.of(sampleComment), pageable, 1);
            when(commentService.getCommentByPostId(eq(5L), any(Pageable.class))).thenReturn(page);

            ResponseDto<Page<CommentResponse>> response = commentController.getCommentsByPost(5L, pageable);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("comments retrieved");
            assertThat(response.data().getContent()).hasSize(1);
            verify(commentService).getCommentByPostId(5L, pageable);
        }

        @Test
        @DisplayName("returns empty page when post has no comments")
        void getCommentsByPost_noComments_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(commentService.getCommentByPostId(anyLong(), any())).thenReturn(new PageImpl<>(List.of()));

            ResponseDto<Page<CommentResponse>> response = commentController.getCommentsByPost(5L, pageable);

            assertThat(response.data().getContent()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET COMMENTS BY USER
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /by-user/{userId} – getCommentsByUser")
    class GetCommentsByUser {

        @Test
        @DisplayName("returns 200 with comments for given userId")
        void getCommentsByUser_validUser_returnsComments() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CommentResponse> page = new PageImpl<>(List.of(sampleComment), pageable, 1);
            when(commentService.getCommentByUserId(eq(10L), any(Pageable.class))).thenReturn(page);

            ResponseDto<Page<CommentResponse>> response = commentController.getCommentsByUser(10L, pageable);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("comments retrieved");
            assertThat(response.data().getContent()).hasSize(1);
            verify(commentService).getCommentByUserId(10L, pageable);
        }

        @Test
        @DisplayName("returns empty page when user has no comments")
        void getCommentsByUser_noComments_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(commentService.getCommentByUserId(anyLong(), any())).thenReturn(new PageImpl<>(List.of()));

            ResponseDto<Page<CommentResponse>> response = commentController.getCommentsByUser(10L, pageable);

            assertThat(response.data().getContent()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE COMMENT
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /{id} – updateComment")
    class UpdateComment {

        @Test
        @DisplayName("returns 200 with updated comment")
        void updateComment_valid_returnsUpdated() {
            CommentUpdateRequest updateRequest = new CommentUpdateRequest();
            updateRequest.setContent("Updated content.");

            CommentResponse updated = new CommentResponse();
            updated.setId(1L);
            updated.setContent("Updated content.");

            when(commentService.updateComment(eq(1L), any(CommentUpdateRequest.class))).thenReturn(updated);

            ResponseDto<CommentResponse> response = commentController.updateComment(1L, updateRequest);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("comment updated");
            assertThat(response.data().getContent()).isEqualTo("Updated content.");
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when comment not found")
        void updateComment_notFound_throwsEntityNotFoundException() {
            CommentUpdateRequest updateRequest = new CommentUpdateRequest();
            updateRequest.setContent("Something.");
            when(commentService.updateComment(anyLong(), any()))
                    .thenThrow(new EntityNotFoundException("Comment not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> commentController.updateComment(99L, updateRequest));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE COMMENT
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /{id} – deleteComment")
    class DeleteComment {

        @Test
        @DisplayName("returns 204 No Content when comment is deleted")
        void deleteComment_exists_returns204() {
            doNothing().when(commentService).deleteComment(1L);

            ResponseEntity<Void> response = commentController.deleteComment(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(commentService).deleteComment(1L);
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when comment not found")
        void deleteComment_notFound_throwsEntityNotFoundException() {
            doThrow(new EntityNotFoundException("Comment not found"))
                    .when(commentService).deleteComment(99L);

            assertThrows(EntityNotFoundException.class,
                    () -> commentController.deleteComment(99L));
        }
    }
}