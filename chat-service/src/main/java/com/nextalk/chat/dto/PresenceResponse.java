package com.nextalk.chat.dto;

import java.time.Instant;
import java.util.UUID;

import com.nextalk.chat.entity.PresenceStatus;

public record PresenceResponse(
        UUID userId,
        PresenceStatus status,
        Instant lastSeenAt
) {
}
