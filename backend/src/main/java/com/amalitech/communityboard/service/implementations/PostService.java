package com.amalitech.communityboard.service.implementations;

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
import com.amalitech.communityboard.service.interfaces.PostInterface;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class PostService implements PostInterface {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostMapper postMapper;
    @Override
    public PostResponse createPost(PostRequest post) {
        User user = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("user not found"));

        Category category = categoryRepository.findById(post.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("category not found"));

        Post entity = postMapper.toEntity(post);
        entity.setAuthor(user);
        entity.setCategory(category);

        return postMapper.toResponse(postRepository.save(entity));
    }

    @Override
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("post not found"));
        return postMapper.toResponse(post);
    }

    @Override
    public Page<PostResponse> getAllPost(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(postMapper::toResponse);
    }

    @Override
    public Page<PostResponse> getPostByUserId(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByAuthor_Id(userId, pageable);
        return posts.map(postMapper::toResponse);
    }

    @Override
    public PostResponse updatePost(Long id, PostUpdateRequest post) {
        Post existing = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("post not found"));

        if (post.getTitle() != null && !post.getTitle().isBlank()) {
            existing.setTitle(post.getTitle());
        }
        if (post.getContent() != null && !post.getContent().isBlank()) {
            existing.setContent(post.getContent());
        }
        if (post.getCategoryId() != null) {
            Category category = categoryRepository.findById(post.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("category not found"));
            existing.setCategory(category);
        }

        return postMapper.toResponse(postRepository.save(existing));
    }

    @Override
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("post not found"));
        postRepository.delete(post);
    }
}
