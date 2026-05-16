package com.nextalk.realtime.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record TypingEvent(
        @NotNull(message = "Room id is required")
        UUID roomId,

        boolean typing
) {
}
