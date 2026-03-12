package com.amalitech.communityboard.events;

import com.amalitech.communityboard.models.Post;
import com.amalitech.communityboard.repository.PostRepository;
import com.amalitech.communityboard.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostCreatedEventListener {

    private final NotificationService notificationService;
    private final PostRepository postRepository;

    @Async
    @EventListener
    @Transactional(readOnly = true)
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        try {
            Post post = postRepository.findById(event.getPost().getId())
                    .orElse(null);
            if (post == null) {
                log.warn("Post with id {} not found when handling PostCreatedEvent", event.getPost().getId());
                return;
            }
            notificationService.notifySubscribersOfNewPost(post);
        } catch (Exception ex) {
            log.error("Failed to process PostCreatedEvent for post {}: {}", event.getPost().getId(), ex.getMessage());
        }
    }
}
