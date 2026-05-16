package com.nextalk.realtime.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record RoomEvent(
        @NotNull(message = "Room id is required")
        UUID roomId,

        @NotNull(message = "Event type is required")
        EventType type,

        UUID messageId,
        String content
) {
}
