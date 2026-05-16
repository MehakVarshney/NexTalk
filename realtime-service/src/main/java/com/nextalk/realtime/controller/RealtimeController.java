package com.nextalk.realtime.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import com.nextalk.realtime.dto.PresenceEvent;
import com.nextalk.realtime.dto.ReactionEvent;
import com.nextalk.realtime.dto.ReadReceiptEvent;
import com.nextalk.realtime.dto.RoomEvent;
import com.nextalk.realtime.dto.TypingEvent;
import com.nextalk.realtime.security.CurrentUser;
import com.nextalk.realtime.service.RealtimeEventFactory;

import jakarta.validation.Valid;

@Controller
@Validated
public class RealtimeController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RealtimeEventFactory eventFactory;

    public RealtimeController(SimpMessagingTemplate messagingTemplate, RealtimeEventFactory eventFactory) {
        this.messagingTemplate = messagingTemplate;
        this.eventFactory = eventFactory;
    }

    @MessageMapping("/typing")
    public void typing(Principal principal, @Valid @Payload TypingEvent event) {
        CurrentUser user = currentUser(principal);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + event.roomId() + "/typing",
                eventFactory.typing(user, event)
        );
    }

    @MessageMapping("/read")
    public void readReceipt(Principal principal, @Valid @Payload ReadReceiptEvent event) {
        CurrentUser user = currentUser(principal);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + event.roomId() + "/reads",
                eventFactory.readReceipt(user, event)
        );
    }

    @MessageMapping("/presence")
    public void presence(Principal principal, @Valid @Payload PresenceEvent event) {
        CurrentUser user = currentUser(principal);
        messagingTemplate.convertAndSend(
                "/topic/presence",
                eventFactory.presence(user, event)
        );
    }

    @MessageMapping("/reaction")
    public void reaction(Principal principal, @Valid @Payload ReactionEvent event) {
        CurrentUser user = currentUser(principal);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + event.roomId() + "/reactions",
                eventFactory.reaction(user, event)
        );
    }

    @MessageMapping("/room-event")
    public void roomEvent(Principal principal, @Valid @Payload RoomEvent event) {
        CurrentUser user = currentUser(principal);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + event.roomId() + "/events",
                eventFactory.roomEvent(user, event)
        );
    }

    private CurrentUser currentUser(Principal principal) {
        if (principal instanceof CurrentUser currentUser) {
            return currentUser;
        }
        throw new IllegalArgumentException("Authenticated WebSocket user is required");
    }
}
