package com.nextalk.chat.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nextalk.chat.dto.MessageRequest;
import com.nextalk.chat.dto.MessageResponse;
import com.nextalk.chat.dto.ReactionRequest;
import com.nextalk.chat.dto.ReactionResponse;
import com.nextalk.chat.security.CurrentUser;
import com.nextalk.chat.service.MessageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/rooms/{roomId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse sendMessage(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID roomId,
            @Valid @RequestBody MessageRequest request
    ) {
        return messageService.sendMessage(currentUser.getUserId(), roomId, request);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public Page<MessageResponse> getMessages(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return messageService.getMessages(currentUser.getUserId(), roomId, page, size);
    }

    @PatchMapping("/messages/{messageId}")
    public MessageResponse editMessage(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID messageId,
            @Valid @RequestBody MessageRequest request
    ) {
        return messageService.editMessage(currentUser.getUserId(), messageId, request);
    }

    @DeleteMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID messageId
    ) {
        messageService.deleteMessage(currentUser.getUserId(), messageId);
    }

    @PostMapping("/messages/{messageId}/reactions")
    @ResponseStatus(HttpStatus.CREATED)
    public ReactionResponse addReaction(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID messageId,
            @Valid @RequestBody ReactionRequest request
    ) {
        return messageService.addReaction(currentUser.getUserId(), messageId, request);
    }

    @DeleteMapping("/messages/{messageId}/reactions/{emoji}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeReaction(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID messageId,
            @PathVariable String emoji
    ) {
        messageService.removeReaction(currentUser.getUserId(), messageId, emoji);
    }
}
