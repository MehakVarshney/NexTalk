package com.nextalk.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AvatarUpdateRequest(
        @NotBlank(message = "Avatar URL is required")
        @Size(max = 2048, message = "URL too long")
        String avatarUrl
) {
}
