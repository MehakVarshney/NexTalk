package com.nextalk.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoomAvatarUpdateRequest(
        @NotBlank(message = "Avatar URL is required")
        @Size(max = 2048, message = "URL too long")
        String avatarUrl
) {
}
