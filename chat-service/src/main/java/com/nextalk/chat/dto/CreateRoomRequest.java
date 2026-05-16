package com.nextalk.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
        @NotBlank(message = "Room name is required")
        @Size(max = 140, message = "Room name can be up to 140 characters")
        String name
) {
}
