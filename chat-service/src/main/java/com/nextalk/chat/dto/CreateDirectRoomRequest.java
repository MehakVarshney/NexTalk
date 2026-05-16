package com.nextalk.chat.dto;

import java.util.UUID;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public record CreateDirectRoomRequest(
        @NotNull(message = "Other user id is required")
        UUID otherUserId,

        @Size(max = 140, message = "Display name must be at most 140 characters")
        String displayName
) {
}
