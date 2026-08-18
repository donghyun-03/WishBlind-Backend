package com.example.wishBlind.notification.dto;

import com.example.wishBlind.notification.domain.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "알림 응답")
public record NotificationResponse(
        Long id,
        String type,
        String typeLabel,
        String title,
        String message,
        Long giftSessionId,
        boolean read,
        LocalDateTime createdAt
) {

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType().name(),
                n.getType().getLabel(),
                n.getTitle(),
                n.getMessage(),
                n.getGiftSessionId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
