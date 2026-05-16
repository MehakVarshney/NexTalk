package com.nextalk.notification.dto;

import java.util.UUID;

import com.nextalk.notification.entity.NotificationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        @NotNull(message = "Recipient id is required")
        UUID recipientId,

        UUID actorId,

        @NotNull(message = "Notification type is required")
        NotificationType type,

        @NotBlank(message = "Title is required")
        @Size(max = 160, message = "Title can be up to 160 characters")
        String title,

        @NotBlank(message = "Body is required")
        @Size(max = 500, message = "Body can be up to 500 characters")
        String body,

        @Size(max = 120, message = "Resource type can be up to 120 characters")
        String resourceType,

        UUID resourceId
) {
}
