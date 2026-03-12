package com.amalitech.communityboard.repository;

import com.amalitech.communityboard.models.Category;
import com.amalitech.communityboard.models.Subscription;
import com.amalitech.communityboard.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserAndCategory(User user, Category category);

    List<Subscription> findByUser(User user);

    List<Subscription> findByCategory(Category category);

    @Query("SELECT DISTINCT s.user FROM Subscription s WHERE s.dailyRecapEnabled = true AND s.muted = false")
    List<User> findDistinctUsersWithActiveDailyRecap();
}
