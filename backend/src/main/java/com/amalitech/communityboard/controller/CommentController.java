package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.ResponseDto;
import com.amalitech.communityboard.dto.request.CommentRequest;
import com.amalitech.communityboard.dto.request.CommentUpdateRequest;
import com.amalitech.communityboard.dto.response.CommentResponse;
import com.amalitech.communityboard.security.CustomUserDetails;
import com.amalitech.communityboard.service.interfaces.CommentInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "Comments", description = "Comment management endpoints")
public class CommentController {

    private final CommentInterface commentService;

    public CommentController(CommentInterface commentService) {
        this.commentService = commentService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    @PostMapping
    @Operation(summary = "Create comment", description = "Create a new comment on a post for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseDto<CommentResponse> createComment(@Valid @RequestBody CommentRequest request,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        CommentResponse commentResponse = commentService.createComment(request, userDetails.getId());
        return new ResponseDto<>(HttpStatus.CREATED, "comment created", commentResponse);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    @Operation(summary = "Get all comments", description = "Retrieve a paginated list of comments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CommentResponse.class))))
    })
    public ResponseDto<Page<CommentResponse>> getAllComments(@PageableDefault(size = 10) Pageable pageable) {
        Page<CommentResponse> comments = commentService.getAllComments(pageable);
        return new ResponseDto<>(HttpStatus.OK, "comments retrieved", comments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    @Operation(summary = "Get comment by id", description = "Retrieve a single comment by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment retrieved",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseDto<CommentResponse> getCommentById(@PathVariable Long id) {
        CommentResponse comment = commentService.getCommentById(id);
        return new ResponseDto<>(HttpStatus.OK, "comment retrieved", comment);
    }

    @GetMapping("/by-post/{postId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    @Operation(summary = "Get comments by post", description = "Retrieve comments for a specific post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CommentResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseDto<Page<CommentResponse>> getCommentsByPost(@PathVariable Long postId,
                                                                @PageableDefault(size = 10) Pageable pageable) {
        Page<CommentResponse> comments = commentService.getCommentByPostId(postId, pageable);
        return new ResponseDto<>(HttpStatus.OK, "comments retrieved", comments);
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    @Operation(summary = "Get comments by user", description = "Retrieve comments created by a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CommentResponse.class)))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseDto<Page<CommentResponse>> getCommentsByUser(@PathVariable Long userId,
                                                                @PageableDefault(size = 10) Pageable pageable) {
        Page<CommentResponse> comments = commentService.getCommentByUserId(userId, pageable);
        return new ResponseDto<>(HttpStatus.OK, "comments retrieved", comments);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER') or @userSecurity.isCommentOwner(#id, authentication)")
    @Operation(summary = "Update comment", description = "Update an existing comment. Only owner or admin can update.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseDto<CommentResponse> updateComment(@PathVariable Long id,
                                                      @Valid @RequestBody CommentUpdateRequest request) {
        CommentResponse updated = commentService.updateComment(id, request);
        return new ResponseDto<>(HttpStatus.OK, "comment updated", updated);
    }

    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCommentOwner(#id, authentication)")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete comment", description = "Delete a comment. Only owner or admin can delete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment deleted"),
            @ApiResponse(responseCode = "404", description = "Comment not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
