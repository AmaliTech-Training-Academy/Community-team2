package com.amalitech.communityboard.repository;

import com.amalitech.communityboard.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Long> {
}
