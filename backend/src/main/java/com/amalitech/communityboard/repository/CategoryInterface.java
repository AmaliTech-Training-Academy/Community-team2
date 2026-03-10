package com.amalitech.communityboard.repository;

import com.amalitech.communityboard.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryInterface extends JpaRepository<Category,Long> {
}
