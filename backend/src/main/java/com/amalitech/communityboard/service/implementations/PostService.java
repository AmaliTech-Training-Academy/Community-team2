package com.amalitech.communityboard.service.implementations;

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
import com.amalitech.communityboard.service.interfaces.PostInterface;
import com.amalitech.communityboard.specification.PostSpecifications;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
@Transactional
public class PostService implements PostInterface {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostMapper postMapper;


    @Override
    @CacheEvict(value = "posts-filtered", allEntries = true)
    public PostResponse createPost(PostRequest post,Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));

        Category category = categoryRepository.findById(post.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("category not found"));

        Post entity = postMapper.toEntity(post);
        entity.setAuthor(user);
        entity.setCategory(category);

        return postMapper.toResponse(postRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "#id")
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("post not found"));
        return postMapper.toResponse(post);
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "posts-filtered",
            keyGenerator = "postCacheKeyGenerator"
    )
    public Page<PostResponse> getAllPosts(PostFilter filter, Pageable pageable) {
        Specification<Post> spec = PostSpecifications.fromFilter(filter);
        Page<Post> posts = postRepository.findAll(spec, pageable);
        return posts.map(postMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "posts-filtered",
            keyGenerator = "postCacheKeyGenerator"
    )
    public Page<PostResponse> getPostByUserId(Long userId, PostFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = new PostFilter();
        }
        filter.setAuthorId(userId);
        return getAllPosts(filter, pageable);
    }

    @Override
    @Caching(
            put    = { @CachePut(value = "posts", key = "#id") },
            evict  = { @CacheEvict(value = "posts-filtered", allEntries = true) }
    )
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

        return postMapper.toResponse(existing);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "posts",          key = "#id"),
            @CacheEvict(value = "posts-filtered", allEntries = true)
    })
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new EntityNotFoundException("post not found");
        }
        postRepository.deleteById(id);
    }
}
