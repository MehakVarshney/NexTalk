package com.nextalk.chat.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(
        @NotNull(message = "User id is required")
        UUID userId
) {
}
