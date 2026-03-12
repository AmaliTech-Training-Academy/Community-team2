package com.amalitech.communityboard.repository;

import com.amalitech.communityboard.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long> {
    Page<Post> findByAuthor_Id(Long authorId, Pageable pageable);

    List<Post> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
