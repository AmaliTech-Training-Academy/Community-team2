package com.amalitech.communityboard.security;

import com.amalitech.communityboard.models.Comment;
import com.amalitech.communityboard.models.Post;
import com.amalitech.communityboard.repository.CommentRepository;
import com.amalitech.communityboard.repository.PostRepository;
import com.amalitech.communityboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Security helper for SpEL-based ownership checks in @PreAuthorize.
 */
@Component("userSecurity")
@RequiredArgsConstructor
@Slf4j
public class UserSecurity {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private  final UserRepository userRepository;



    public boolean isOwner(Long targetUserId, Authentication authentication) {
        if (targetUserId == null || authentication == null) {
            return false;
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return false;
        }

        Long currentUserId = userDetails.getId();

        return currentUserId.equals(targetUserId);
    }
    /**
     * Returns true if the given userId is the author of the post with the given id.
     * Used from SpEL: @userSecurity.isPostOwner(#postId, principal.id)
     */
    public boolean isPostOwner(Long postId, Authentication authentication) {
        if (postId == null || authentication == null) {
            return false;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        try {
            return postRepository.findById(postId)
                    .map(Post::getAuthor)
                    .map(author -> userId.equals(author.getId()))
                    .orElse(false);
        } catch (Exception e) {
            log.error("[SECURITY] Failed ownership check for post {} and user {}: {}", postId, userId, e.getMessage(), e);
            return false;
        }
    }

    public boolean isCommentOwner(Long commentId, Authentication authentication) {

        if (commentId == null || authentication == null) {
            return false;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        try {
            return commentRepository.findById(commentId)
                    .map(Comment::getUser)
                    .map(user -> userId.equals(user.getId()))
                    .orElse(false);
        } catch (Exception e) {
            log.error("[SECURITY] Failed ownership check for comment {} and user {}: {}",
                    commentId, userId, e.getMessage(), e);
            return false;
        }
    }
}

