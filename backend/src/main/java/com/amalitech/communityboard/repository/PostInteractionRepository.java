package com.amalitech.communityboard.repository;

import com.amalitech.communityboard.models.Post;
import com.amalitech.communityboard.models.PostInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostInteractionRepository extends JpaRepository<PostInteraction, Long> {

    List<PostInteraction> findByOccurredAtBetween(LocalDateTime start, LocalDateTime end);

    List<PostInteraction> findByPostAndOccurredAtBetween(Post post, LocalDateTime start, LocalDateTime end);
}

