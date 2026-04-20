package com.nextalk.auth.dto;

public record AuthResponse(
        String tokenType,
        String accessToken,
        UserResponse user
) {
}
