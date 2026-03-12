package com.amalitech.communityboard.notification;

import lombok.Builder;

@Builder
public record NotificationDto(String subject,String message,String recipient,String link) {
}
