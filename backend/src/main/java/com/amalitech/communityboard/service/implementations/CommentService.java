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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService implements CommentInterface {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
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
    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("comment not found"));
        return commentMapper.toResponse(comment);
    }

    @Override
    public Page<CommentResponse> getAllComments(Pageable pageable) {
        Page<Comment> comments = commentRepository.findAll(pageable);
        return comments.map(commentMapper::toResponse);
    }

    @Override
    public Page<CommentResponse> getCommentByPostId(Long postId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByPostId(postId, pageable);
        return comments.map(commentMapper::toResponse);
    }

    @Override
    public Page<CommentResponse> getCommentByUserId(Long userId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByUserId(userId, pageable);
        return comments.map(commentMapper::toResponse);
    }

    @Override
    public CommentResponse updateComment(Long id, CommentUpdateRequest comment) {
        Comment existing = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("comment not found"));
        existing.setContent(comment.getContent());
        return commentMapper.toResponse(commentRepository.save(existing));
    }

    @Override
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("comment not found"));
        commentRepository.delete(comment);
    }
}
