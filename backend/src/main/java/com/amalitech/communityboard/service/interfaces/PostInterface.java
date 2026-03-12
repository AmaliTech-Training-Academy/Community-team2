package com.amalitech.communityboard.service.interfaces;

import com.amalitech.communityboard.dto.request.PostFilter;
import com.amalitech.communityboard.dto.request.PostRequest;
import com.amalitech.communityboard.dto.request.PostUpdateRequest;
import com.amalitech.communityboard.dto.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostInterface {
    PostResponse createPost(PostRequest post, Long userId);
    PostResponse getPostById(Long id);
    Page<PostResponse> getAllPosts(PostFilter filter, Pageable pageable);
    Page<PostResponse> getPostByUserId(Long userId, PostFilter filter, Pageable pageable);
    PostResponse updatePost(Long id, PostUpdateRequest post);
    void deletePost(Long id);
}
