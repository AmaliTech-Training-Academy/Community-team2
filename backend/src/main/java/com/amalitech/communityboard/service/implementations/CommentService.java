package com.amalitech.communityboard.service.implementations;


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
import com.amalitech.communityboard.service.interfaces.CommentInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService implements CommentInterface {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "comments-by-post", key = "#request.postId"),
            @CacheEvict(value = "comments-by-user", key = "#userId"),
            @CacheEvict(value = "comments-all",     allEntries = true)
    })
    public CommentResponse createComment(CommentRequest request, Long userId) {

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Comment entity = commentMapper.toEntity(request);
        entity.setPost(post);
        entity.setUser(user);

        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
            entity.setParent(parentComment);
        } else {
            entity.setParent(null);
        }

        return commentMapper.toResponse(commentRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#id")
    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        return commentMapper.toResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "comments-all",
            key   = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort"
    )
    public Page<CommentResponse> getAllComments(Pageable pageable) {
        Page<Comment> comments = commentRepository.findAll(pageable);
        return comments.map(commentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "comments-by-post",
            key   = "#postId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize"
    )
    public Page<CommentResponse> getCommentByPostId(Long postId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByPostId(postId, pageable);
        return comments.map(commentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "comments-by-user",
            key   = "#userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize"
    )
    public Page<CommentResponse> getCommentByUserId(Long userId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByUserId(userId, pageable);
        return comments.map(commentMapper::toResponse);
    }

    @Override
    @Caching(
            put  = { @CachePut(value = "comments", key = "#id") },
            evict = {
                    @CacheEvict(value = "comments-by-post", allEntries = true),
                    @CacheEvict(value = "comments-by-user", allEntries = true),
                    @CacheEvict(value = "comments-all",     allEntries = true)
            }
    )
    public CommentResponse updateComment(Long id, CommentUpdateRequest comment) {
        Comment existing = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        existing.setContent(comment.getContent());
        return commentMapper.toResponse(existing);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "comments",         key = "#id"),
            @CacheEvict(value = "comments-by-post", allEntries = true),
            @CacheEvict(value = "comments-by-user", allEntries = true),
            @CacheEvict(value = "comments-all",     allEntries = true)
    })
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new EntityNotFoundException("Comment not found");
        }
        commentRepository.deleteById(id);
    }
}
