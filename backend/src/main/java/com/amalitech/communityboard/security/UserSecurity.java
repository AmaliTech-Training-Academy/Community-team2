package com.amalitech.communityboard.security;

import com.amalitech.communityboard.models.Post;
import com.amalitech.communityboard.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Security helper for SpEL-based ownership checks in @PreAuthorize.
 */
@Component("userSecurity")
@RequiredArgsConstructor
@Slf4j
public class UserSecurity {

    private final PostRepository postRepository;

    /**
     * Returns true if the given userId is the author of the post with the given id.
     * Used from SpEL: @userSecurity.isPostOwner(#postId, principal.id)
     */
    public boolean isPostOwner(Long postId, Long userId) {
        if (postId == null || userId == null) {
            return false;
        }
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
}

