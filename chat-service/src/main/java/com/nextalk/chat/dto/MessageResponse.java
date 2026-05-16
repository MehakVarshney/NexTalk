package com.nextalk.chat.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nextalk.chat.entity.MessageType;

public record MessageResponse(
        UUID id,
        UUID roomId,
        UUID senderId,
        String content,
        MessageType type,
        boolean edited,
        boolean deleted,
        Instant createdAt,
        Instant updatedAt,
        List<ReactionResponse> reactions
) {
}
