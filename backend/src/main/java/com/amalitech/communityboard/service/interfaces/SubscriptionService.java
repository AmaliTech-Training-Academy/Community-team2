package com.amalitech.communityboard.service.interfaces;

import com.amalitech.communityboard.models.Category;
import com.amalitech.communityboard.models.Subscription;
import com.amalitech.communityboard.models.User;

import java.util.List;

public interface SubscriptionService {

    Subscription subscribe(User user, Category category);

    void unsubscribe(User user, Category category);

    List<Subscription> getUserSubscriptions(User user);

    List<Subscription> getCategorySubscriptions(Category category);

    List<User> getUsersWithActiveDailyRecap();
}

