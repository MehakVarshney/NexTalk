package com.nextalk.chat.dto;

import com.nextalk.chat.entity.PresenceStatus;

import jakarta.validation.constraints.NotNull;

public record PresenceRequest(
        @NotNull(message = "Presence status is required")
        PresenceStatus status
) {
}
