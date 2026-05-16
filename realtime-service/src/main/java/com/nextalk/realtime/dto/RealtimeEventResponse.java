package com.nextalk.realtime.dto;

import java.time.Instant;
import java.util.UUID;

public record RealtimeEventResponse(
        EventType type,
        UUID roomId,
        UUID messageId,
        UUID userId,
        String userName,
        String content,
        String emoji,
        String status,
        boolean typing,
        boolean removed,
        Instant occurredAt
) {
}
