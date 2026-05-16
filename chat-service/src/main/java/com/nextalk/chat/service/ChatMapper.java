package com.nextalk.chat.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.nextalk.chat.dto.MemberResponse;
import com.nextalk.chat.dto.MessageResponse;
import com.nextalk.chat.dto.PresenceResponse;
import com.nextalk.chat.dto.ReactionResponse;
import com.nextalk.chat.dto.RoomResponse;
import com.nextalk.chat.entity.ChatRoom;
import com.nextalk.chat.entity.Message;
import com.nextalk.chat.entity.MessageReaction;
import com.nextalk.chat.entity.RoomMember;
import com.nextalk.chat.entity.UserPresence;

@Component
public class ChatMapper {

    public RoomResponse toRoomResponse(ChatRoom room, List<RoomMember> members) {
        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getAvatarUrl(),
                room.getType(),
                room.getCreatedBy(),
                room.getCreatedAt(),
                members.stream().map(this::toMemberResponse).toList()
        );
    }

    public MemberResponse toMemberResponse(RoomMember member) {
        return new MemberResponse(
                member.getId(),
                member.getUserId(),
                member.getRole(),
                member.getJoinedAt()
        );
    }

    public MessageResponse toMessageResponse(Message message, List<MessageReaction> reactions) {
        return new MessageResponse(
                message.getId(),
                message.getRoom().getId(),
                message.getSenderId(),
                message.getContent(),
                message.getType(),
                message.isEdited(),
                message.isDeleted(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                reactions.stream().map(this::toReactionResponse).toList()
        );
    }

    public ReactionResponse toReactionResponse(MessageReaction reaction) {
        return new ReactionResponse(
                reaction.getId(),
                reaction.getMessage().getId(),
                reaction.getUserId(),
                reaction.getEmoji(),
                reaction.getCreatedAt()
        );
    }

    public PresenceResponse toPresenceResponse(UserPresence presence) {
        return new PresenceResponse(
                presence.getUserId(),
                presence.getStatus(),
                presence.getLastSeenAt()
        );
    }
}
