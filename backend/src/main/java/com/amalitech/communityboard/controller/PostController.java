package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.ResponseDto;
import com.amalitech.communityboard.dto.request.PostFilter;
import com.amalitech.communityboard.dto.request.PostRequest;
import com.amalitech.communityboard.dto.request.PostUpdateRequest;
import com.amalitech.communityboard.dto.response.PostResponse;
import com.amalitech.communityboard.security.CustomUserDetails;
import com.amalitech.communityboard.service.interfaces.PostInterface;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Posts", description = "Post management endpoints")
public class PostController {

    private final PostInterface postService;

    public PostController(PostInterface postService) {
        this.postService = postService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    @Operation(summary = "Create post", description = "Create a new post for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseDto<PostResponse> createPost(
            @Valid @RequestPart("post") PostRequest request,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        PostResponse postResponse = postService.createPost(request, principal.getId(), image);
        return new ResponseDto<>(HttpStatus.CREATED, "post created", postResponse);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all posts", description = "Retrieve a paginated list of posts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PostResponse.class))))
    })
    public ResponseDto<Page<PostResponse>> getAllPosts(@ModelAttribute PostFilter filter,
                                                       @PageableDefault(size = 10) Pageable pageable) {
        Page<PostResponse> posts = postService.getAllPosts(filter, pageable);
        return new ResponseDto<>(HttpStatus.OK, "posts retrieved", posts);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get post by id", description = "Retrieve a single post by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post retrieved",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseDto<PostResponse> getPostById(@PathVariable Long id) {
        PostResponse post = postService.getPostById(id);
        return new ResponseDto<>(HttpStatus.OK, "post retrieved", post);
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get posts by user", description = "Retrieve posts created by a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PostResponse.class))))
    })
    public ResponseDto<Page<PostResponse>> getPostsByUser(@PathVariable Long userId,
                                                          @ModelAttribute PostFilter filter,
                                                          @PageableDefault(size = 10) Pageable pageable) {
        Page<PostResponse> posts = postService.getPostByUserId(userId, filter, pageable);
        return new ResponseDto<>(HttpStatus.OK, "posts retrieved", posts);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isPostOwner(#id, authentication)")
    @Operation(summary = "Update post", description = "Update an existing post. Only owner or admin can update.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post updated",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseDto<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request) {

        PostResponse updated = postService.updatePost(id, request);
        return new ResponseDto<>(HttpStatus.OK, "post updated", updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isPostOwner(#id, authentication)")
    @Operation(summary = "Delete post", description = "Delete a post. Only owner or admin can delete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post deleted"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
