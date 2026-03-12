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

import java.time.Year;
import java.util.Map;


@RequiredArgsConstructor
@Slf4j
@Component
public class EmailNotificationService implements Notification {

    private static final String DEFAULT_TEMPLATE = "general-email-template";

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
            context.setVariable("subject", message.subject());
            context.setVariable("year", Year.now().getValue());

            // Merge any additional context variables if provided
            if (message.context() instanceof Map<?, ?> map) {
                map.forEach((k, v) -> {
                    if (k != null) {
                        context.setVariable(String.valueOf(k), v);
                    }
                });
            }

            String templateName = (message.templateName() != null && !message.templateName().isBlank())
                    ? message.templateName()
                    : DEFAULT_TEMPLATE;

            String htmlContent = templateEngine.process(templateName, context);

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
