package com.nextalk.chat.dto;

import java.time.Instant;
import java.util.UUID;

import com.nextalk.chat.entity.MemberRole;

public record MemberResponse(
        UUID id,
        UUID userId,
        MemberRole role,
        Instant joinedAt
) {
}
