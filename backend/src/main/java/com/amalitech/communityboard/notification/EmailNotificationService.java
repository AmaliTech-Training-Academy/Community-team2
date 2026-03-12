package com.amalitech.communityboard.notification;


import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@RequiredArgsConstructor
@Slf4j
@Component
public class EmailNotificationService implements Notification {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String sender;

    @Async("executor")
    @Override
    public void send(NotificationDto message) {
        try {
            Context context = new Context();
            context.setVariable("customMessage", message.message());
            context.setVariable("actionLink", message.link());

            String htmlContent = templateEngine.process("rest-email-template", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(sender);
            helper.setTo(message.recipient());
            helper.setSubject(message.subject());
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage());
        }
    }
}
