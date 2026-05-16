package com.nextalk.chat.dto;

import java.util.UUID;

public record NotificationRequest(
        UUID recipientId,
        UUID actorId,
        String type,
        String title,
        String body,
        String resourceType,
        UUID resourceId
) {
}
