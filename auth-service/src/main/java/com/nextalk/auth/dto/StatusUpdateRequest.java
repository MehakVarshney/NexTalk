package com.nextalk.auth.dto;

import com.nextalk.auth.entity.UserStatus;

import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "Status is required")
        UserStatus status
) {
}
