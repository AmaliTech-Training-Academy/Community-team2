package com.amalitech.communityboard.service.implementations;

import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.models.Category;
import com.amalitech.communityboard.models.Subscription;
import com.amalitech.communityboard.models.User;
import com.amalitech.communityboard.repository.CategoryRepository;
import com.amalitech.communityboard.repository.SubscriptionRepository;
import com.amalitech.communityboard.repository.UserRepository;
import com.amalitech.communityboard.service.interfaces.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Subscription subscribe(User user, Category category) {
        return subscriptionRepository.findByUserAndCategory(user, category)
                .orElseGet(() -> {
                    Subscription subscription = Subscription.builder()
                            .user(user)
                            .category(category)
                            .build();
                    return subscriptionRepository.save(subscription);
                });
    }

    @Override
    @Transactional
    public void unsubscribe(User user, Category category) {
        Subscription subscription = subscriptionRepository.findByUserAndCategory(user, category)
                .orElseThrow(() -> new EntityNotFoundException("subscription not found"));
        subscriptionRepository.delete(subscription);
    }

    @Override
    public List<Subscription> getUserSubscriptions(User user) {
        return subscriptionRepository.findByUser(user);
    }

    @Override
    public List<Subscription> getCategorySubscriptions(Category category) {
        return subscriptionRepository.findByCategory(category);
    }

    public List<User> getUsersWithActiveDailyRecap() {
        return subscriptionRepository.findDistinctUsersWithActiveDailyRecap();
    }
}
