package com.amalitech.communityboard.service.interfaces;

import com.amalitech.communityboard.models.Post;

import java.time.LocalDate;

public interface NotificationService {

    void notifySubscribersOfNewPost(Post post);

    void sendDailyRecapForDate(LocalDate date);
}

