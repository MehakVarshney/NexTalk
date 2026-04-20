package com.nextalk.auth.dto;

import java.time.Instant;
import java.util.UUID;

import com.nextalk.auth.entity.AuthProvider;
import com.nextalk.auth.entity.UserStatus;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String avatarUrl,
        AuthProvider provider,
        UserStatus status,
        Instant createdAt
) {
}
