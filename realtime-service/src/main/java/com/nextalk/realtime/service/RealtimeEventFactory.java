package com.nextalk.realtime.service;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.nextalk.realtime.dto.EventType;
import com.nextalk.realtime.dto.PresenceEvent;
import com.nextalk.realtime.dto.ReactionEvent;
import com.nextalk.realtime.dto.ReadReceiptEvent;
import com.nextalk.realtime.dto.RealtimeEventResponse;
import com.nextalk.realtime.dto.RoomEvent;
import com.nextalk.realtime.dto.TypingEvent;
import com.nextalk.realtime.security.CurrentUser;

@Component
public class RealtimeEventFactory {

    public RealtimeEventResponse typing(CurrentUser user, TypingEvent event) {
        return new RealtimeEventResponse(
                EventType.TYPING,
                event.roomId(),
                null,
                user.getUserId(),
                user.getDisplayName(),
                null,
                null,
                null,
                event.typing(),
                false,
                Instant.now()
        );
    }

    public RealtimeEventResponse readReceipt(CurrentUser user, ReadReceiptEvent event) {
        return new RealtimeEventResponse(
                EventType.READ_RECEIPT,
                event.roomId(),
                event.messageId(),
                user.getUserId(),
                user.getDisplayName(),
                null,
                null,
                null,
                false,
                false,
                Instant.now()
        );
    }

    public RealtimeEventResponse presence(CurrentUser user, PresenceEvent event) {
        return new RealtimeEventResponse(
                EventType.PRESENCE,
                null,
                null,
                user.getUserId(),
                user.getDisplayName(),
                null,
                null,
                event.status(),
                false,
                false,
                Instant.now()
        );
    }

    public RealtimeEventResponse reaction(CurrentUser user, ReactionEvent event) {
        return new RealtimeEventResponse(
                EventType.REACTION,
                event.roomId(),
                event.messageId(),
                user.getUserId(),
                user.getDisplayName(),
                null,
                event.emoji(),
                null,
                false,
                event.removed(),
                Instant.now()
        );
    }

    public RealtimeEventResponse roomEvent(CurrentUser user, RoomEvent event) {
        return new RealtimeEventResponse(
                event.type(),
                event.roomId(),
                event.messageId(),
                user.getUserId(),
                user.getDisplayName(),
                event.content(),
                null,
                null,
                false,
                false,
                Instant.now()
        );
    }
}
