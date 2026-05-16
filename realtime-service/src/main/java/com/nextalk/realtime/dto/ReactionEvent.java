package com.nextalk.realtime.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReactionEvent(
        @NotNull(message = "Room id is required")
        UUID roomId,

        @NotNull(message = "Message id is required")
        UUID messageId,

        @NotBlank(message = "Emoji is required")
        String emoji,

        boolean removed
) {
}
