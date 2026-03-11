package com.amalitech.communityboard.service.interfaces;


import com.amalitech.communityboard.dto.request.CommentRequest;
import com.amalitech.communityboard.dto.request.CommentUpdateRequest;
import com.amalitech.communityboard.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentInterface {
    CommentResponse createComment(CommentRequest comment,Long userId);
    CommentResponse getCommentById(Long id);
    Page<CommentResponse> getAllComments(Pageable pageable);
    Page<CommentResponse> getCommentByPostId(Long postId, Pageable pageable);
    Page<CommentResponse> getCommentByUserId(Long userId, Pageable pageable);
    CommentResponse updateComment(Long id, CommentUpdateRequest comment);
    void deleteComment(Long id);
}
