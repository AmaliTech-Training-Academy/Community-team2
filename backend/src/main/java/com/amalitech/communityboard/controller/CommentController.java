package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.ResponseDto;
import com.amalitech.communityboard.dto.request.CommentRequest;
import com.amalitech.communityboard.dto.request.CommentUpdateRequest;
import com.amalitech.communityboard.dto.response.CommentResponse;
import com.amalitech.communityboard.security.CustomUserDetails;
import com.amalitech.communityboard.service.interfaces.CommentInterface;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentInterface commentService;

    public CommentController(CommentInterface commentService) {
        this.commentService = commentService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    @PostMapping
    public ResponseDto<CommentResponse> createComment(@Valid @RequestBody CommentRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        CommentResponse commentResponse = commentService.createComment(request,userDetails.getId());
        return new ResponseDto<>(HttpStatus.CREATED, "comment created", commentResponse);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    public ResponseDto<Page<CommentResponse>> getAllComments(@PageableDefault(size = 10) Pageable pageable) {
        Page<CommentResponse> comments = commentService.getAllComments(pageable);
        return new ResponseDto<>(HttpStatus.OK, "comments retrieved", comments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    public ResponseDto<CommentResponse> getCommentById(@PathVariable Long id) {
        CommentResponse comment = commentService.getCommentById(id);
        return new ResponseDto<>(HttpStatus.OK, "comment retrieved", comment);
    }

    @GetMapping("/by-post/{postId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    public ResponseDto<Page<CommentResponse>> getCommentsByPost(@PathVariable Long postId, @PageableDefault(size = 10) Pageable pageable) {
        Page<CommentResponse> comments = commentService.getCommentByPostId(postId, pageable);
        return new ResponseDto<>(HttpStatus.OK, "comments retrieved", comments);
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    public ResponseDto<Page<CommentResponse>> getCommentsByUser(@PathVariable Long userId, @PageableDefault(size = 10) Pageable pageable) {
        Page<CommentResponse> comments = commentService.getCommentByUserId(userId, pageable);
        return new ResponseDto<>(HttpStatus.OK, "comments retrieved", comments);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER') or @userSecurity.isCommentOwner(#id, authentication)")

    public ResponseDto<CommentResponse> updateComment(@PathVariable Long id, @Valid @RequestBody CommentUpdateRequest request) {
        CommentResponse updated = commentService.updateComment(id, request);
        return new ResponseDto<>(HttpStatus.OK, "comment updated", updated);
    }

    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCommentOwner(#id, authentication)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
