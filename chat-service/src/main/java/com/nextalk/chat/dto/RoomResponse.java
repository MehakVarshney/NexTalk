package com.nextalk.chat.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nextalk.chat.entity.RoomType;

public record RoomResponse(
        UUID id,
        String name,
        String avatarUrl,
        RoomType type,
        UUID createdBy,
        Instant createdAt,
        List<MemberResponse> members
) {
}
