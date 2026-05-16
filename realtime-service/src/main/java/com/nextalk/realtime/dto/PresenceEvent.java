package com.nextalk.realtime.dto;

import jakarta.validation.constraints.NotBlank;

public record PresenceEvent(
        @NotBlank(message = "Status is required")
        String status
) {
}
