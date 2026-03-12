package com.amalitech.communityboard.service.implementations;

import com.amalitech.communityboard.models.Category;
import com.amalitech.communityboard.models.Post;
import com.amalitech.communityboard.models.Subscription;
import com.amalitech.communityboard.models.User;
import com.amalitech.communityboard.notification.EmailNotificationService;
import com.amalitech.communityboard.notification.NotificationDto;
import com.amalitech.communityboard.repository.PostRepository;
import com.amalitech.communityboard.service.interfaces.NotificationService;
import com.amalitech.communityboard.service.interfaces.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final SubscriptionService subscriptionService;
    private final PostRepository postRepository;

    private final JavaMailSender mailSender;
    private final EmailNotificationService emailNotificationService;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${app.frontend}")
    private String dashboardUrl;

    @Override
    public void notifySubscribersOfNewPost(Post post) {
        Category category = post.getCategory();
        List<Subscription> subscriptions = subscriptionService.getCategorySubscriptions(category);

        for (Subscription subscription : subscriptions) {
            if (!subscription.isImmediateNotificationsEnabled() || subscription.isMuted()) {
                continue;
            }
            User user = subscription.getUser();
            if (user.getEmail() == null) {
                continue;
            }
            if (post.getAuthor() != null && post.getAuthor().getId().equals(user.getId())) {
                continue;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setSubject("New post in " + category.getName() + ": " + post.getTitle());
            message.setText("Hi " + user.getUsername() + ",\n\n" +
                    "A new post was created in a category you are subscribed to (" + category.getName() + ").\n" +
                    "Title: " + post.getTitle() + "\n\n" +
                    "Visit the app to read more.\n\n" +
                    "If you no longer want these emails, update your subscription settings.");

            try {
                mailSender.send(message);
            } catch (Exception e) {
                log.error("Failed to send new post notification email to {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    @Override
    public void sendDailyRecapForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Post> posts = postRepository.findByCreatedAtBetween(start, end);
        if (posts.isEmpty()) {
            log.info("Daily recap: no posts created on {}", date);
            return;
        }

        Map<Category, Long> countsByCategory = posts.stream()
            .collect(Collectors.groupingBy(Post::getCategory, Collectors.counting()));

        // Fetch only users who have at least one active daily recap subscription
        List<User> allUsers = subscriptionService.getUsersWithActiveDailyRecap();

        for (User user : allUsers) {
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                continue;
            }

            List<Subscription> subscriptions = subscriptionService.getUserSubscriptions(user).stream()
                .filter(sub -> sub.isDailyRecapEnabled() && !sub.isMuted())
                .toList();

            if (subscriptions.isEmpty()) {
                continue;
            }

            Map<Category, Long> userCounts = new LinkedHashMap<>();
            for (Subscription sub : subscriptions) {
                Category category = sub.getCategory();
                Long count = countsByCategory.get(category);
                if (count != null && count > 0) {
                    userCounts.put(category, count);
                }
            }

            if (userCounts.isEmpty()) {
                continue;
            }

            // change to real URL or config later

            StringBuilder customMessage = new StringBuilder();
            customMessage.append("Your daily recap is ready for ")
                .append(date)
                .append(".\n");

            userCounts.forEach((category, count) -> customMessage
                .append(" • ")
                .append(count)
                .append(" new post")
                .append(count == 1 ? "" : "s")
                .append(" in ")
                .append(category.getName())
                .append("\n"));

            String subject = "Your CommunityBoard Daily Recap";

            NotificationDto dto = NotificationDto.builder()
                    .recipient(user.getEmail())
                    .subject(subject)
                    .link(dashboardUrl)
                    .context(customMessage.toString()).build();

            try {
                System.out.println("sending emails");
                emailNotificationService.send(dto);
                log.info("Daily recap (templated): sent digest to {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send daily recap email (templated) to {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }
}

