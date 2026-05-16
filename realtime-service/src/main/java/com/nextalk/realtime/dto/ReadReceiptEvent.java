package com.nextalk.realtime.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ReadReceiptEvent(
        @NotNull(message = "Room id is required")
        UUID roomId,

        @NotNull(message = "Message id is required")
        UUID messageId
) {
}
