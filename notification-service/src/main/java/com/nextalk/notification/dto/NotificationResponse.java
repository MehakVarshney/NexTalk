package com.nextalk.notification.dto;

import java.time.Instant;
import java.util.UUID;

import com.nextalk.notification.entity.NotificationType;

public record NotificationResponse(
        UUID id,
        UUID recipientId,
        UUID actorId,
        NotificationType type,
        String title,
        String body,
        String resourceType,
        UUID resourceId,
        boolean read,
        Instant readAt,
        Instant createdAt
) {
}
