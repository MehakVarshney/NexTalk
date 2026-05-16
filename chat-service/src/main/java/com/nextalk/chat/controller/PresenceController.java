package com.nextalk.chat.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextalk.chat.dto.PresenceRequest;
import com.nextalk.chat.dto.PresenceResponse;
import com.nextalk.chat.security.CurrentUser;
import com.nextalk.chat.service.PresenceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat/presence")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @PatchMapping("/me")
    public PresenceResponse updatePresence(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody PresenceRequest request
    ) {
        return presenceService.updatePresence(currentUser.getUserId(), request);
    }

    @GetMapping("/{userId}")
    public PresenceResponse getPresence(@PathVariable UUID userId) {
        return presenceService.getPresence(userId);
    }
}
