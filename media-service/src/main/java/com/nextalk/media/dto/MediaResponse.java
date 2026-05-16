package com.nextalk.media.dto;

import java.time.Instant;
import java.util.UUID;

public record MediaResponse(
        UUID id,
        UUID ownerId,
        String originalFileName,
        String contentType,
        long sizeBytes,
        boolean image,
        String fileUrl,
        String thumbnailUrl,
        Instant createdAt
) {
}
