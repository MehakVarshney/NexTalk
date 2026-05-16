package com.nextalk.chat.dto;

import com.nextalk.chat.entity.MessageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageRequest(
        @NotBlank(message = "Message content is required")
        @Size(max = 2000, message = "Message can be up to 2000 characters")
        String content,

        @NotNull(message = "Message type is required")
        MessageType type
) {
}
