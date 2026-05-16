package com.nextalk.chat.dto;

import java.time.Instant;
import java.util.UUID;

public record ReactionResponse(
        UUID id,
        UUID messageId,
        UUID userId,
        String emoji,
        Instant createdAt
) {
}
