package com.amalitech.communityboard.events;

import com.amalitech.communityboard.models.User;
import com.amalitech.communityboard.notification.EmailNotificationService;
import com.amalitech.communityboard.notification.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedEventListener {

    private final EmailNotificationService emailNotificationService;

    @Async("executor")
    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        User user = event.getUser();
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        try {
            String subject = "Welcome to CommunityBoard";
            String message = String.format(
                    "Hi %s,\n\n" +
                    "Your account on CommunityBoard has been created successfully.\n" +
                    "You can now sign in and start exploring your community.",
                    user.getUsername() != null ? user.getUsername() : user.getEmail()
            );

            String link = "http://localhost:3000";

            Map<String, Object> context = new HashMap<>();
            context.put("userName", user.getUsername() != null ? user.getUsername() : user.getEmail());
            context.put("userEmail", user.getEmail());
            context.put("createdAt", LocalDateTime.now());
            context.put("dashboardUrl", link);
            context.put("supportEmail", "support@example.com");
            context.put("year", LocalDateTime.now().getYear());

            NotificationDto dto = NotificationDto.builder()
                    .subject(subject)
                    .recipient(user.getEmail())
                    .message(message)
                    .link(link)
                    .templateName("user-created-email")
                    .context(context)
                    .build();

            emailNotificationService.send(dto);
            log.info("Sent welcome email to newly created user: {}", user.getEmail());
        } catch (Exception ex) {
            log.error("Failed to send welcome email for user {}: {}", user.getEmail(), ex.getMessage());
        }
    }
}
