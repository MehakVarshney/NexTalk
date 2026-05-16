package com.nextalk.chat.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextalk.chat.dto.MessageRequest;
import com.nextalk.chat.dto.MessageResponse;
import com.nextalk.chat.dto.ReactionRequest;
import com.nextalk.chat.dto.ReactionResponse;
import com.nextalk.chat.entity.ChatRoom;
import com.nextalk.chat.entity.Message;
import com.nextalk.chat.entity.MessageReaction;
import com.nextalk.chat.exception.ApiException;
import com.nextalk.chat.entity.RoomMember;
import com.nextalk.chat.repository.ChatRoomRepository;
import com.nextalk.chat.repository.MessageReactionRepository;
import com.nextalk.chat.repository.MessageRepository;
import com.nextalk.chat.repository.RoomMemberRepository;
import com.nextalk.chat.dto.NotificationRequest;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageReactionRepository reactionRepository;
    private final ChatRoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomService roomService;
    private final ChatMapper mapper;
    private final RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    public MessageService(
            MessageRepository messageRepository,
            MessageReactionRepository reactionRepository,
            ChatRoomRepository roomRepository,
            RoomMemberRepository roomMemberRepository,
            RoomService roomService,
            ChatMapper mapper,
            RestTemplate restTemplate
    ) {
        this.messageRepository = messageRepository;
        this.reactionRepository = reactionRepository;
        this.roomRepository = roomRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.roomService = roomService;
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public MessageResponse sendMessage(UUID currentUserId, UUID roomId, MessageRequest request) {
        roomService.ensureMember(roomId, currentUserId);
        ChatRoom room = findRoom(roomId);

        Message message = new Message();
        message.setRoom(room);
        message.setSenderId(currentUserId);
        message.setContent(request.content().trim());
        message.setType(request.type());
        message.setEdited(false);
        message.setDeleted(false);

        Message savedMessage = messageRepository.save(message);
        
        // Trigger notifications asynchronously
        CompletableFuture.runAsync(() -> notifyMembers(room, currentUserId, savedMessage));
        
        return mapper.toMessageResponse(savedMessage, reactionRepository.findByMessageId(savedMessage.getId()));
    }

    private void notifyMembers(ChatRoom room, UUID senderId, Message message) {
        try {
            String title = room.getType().name().equals("DIRECT") ? "New Message" : "New Message in " + room.getName();
            String bodyContent = message.getType().name().equals("MEDIA") ? "Sent a media file" : message.getContent();
            String bodySnippet = bodyContent.length() > 50 ? bodyContent.substring(0, 47) + "..." : bodyContent;
            
            List<RoomMember> members = roomMemberRepository.findByRoomId(room.getId());
            for (RoomMember member : members) {
                if (!member.getUserId().equals(senderId)) {
                    NotificationRequest notifRequest = new NotificationRequest(
                            member.getUserId(),
                            senderId,
                            "NEW_MESSAGE",
                            title,
                            bodySnippet,
                            "ROOM",
                            room.getId()
                    );
                    try {
                        restTemplate.postForEntity("http://notification-service/api/notifications/publish", notifRequest, Void.class);
                    } catch (Exception e) {
                        log.error("Failed to send notification to user {}", member.getUserId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to process notifications for message {}", message.getId(), e);
        }
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(UUID currentUserId, UUID roomId, int page, int size) {
        roomService.ensureMember(roomId, currentUserId);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        return messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageRequest)
                .map(message -> mapper.toMessageResponse(message, reactionRepository.findByMessageId(message.getId())));
    }

    @Transactional
    public MessageResponse editMessage(UUID currentUserId, UUID messageId, MessageRequest request) {
        Message message = findMessage(messageId);
        roomService.ensureMember(message.getRoom().getId(), currentUserId);
        ensureSender(message, currentUserId);

        if (message.isDeleted()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Deleted message cannot be edited");
        }

        message.setContent(request.content().trim());
        message.setType(request.type());
        message.setEdited(true);
        return mapper.toMessageResponse(message, reactionRepository.findByMessageId(message.getId()));
    }

    @Transactional
    public void deleteMessage(UUID currentUserId, UUID messageId) {
        Message message = findMessage(messageId);
        roomService.ensureMember(message.getRoom().getId(), currentUserId);
        ensureSender(message, currentUserId);

        message.setDeleted(true);
        message.setContent("This message was deleted");
    }

    @Transactional
    public ReactionResponse addReaction(UUID currentUserId, UUID messageId, ReactionRequest request) {
        Message message = findMessage(messageId);
        roomService.ensureMember(message.getRoom().getId(), currentUserId);

        String emoji = request.emoji().trim();
        MessageReaction reaction = reactionRepository
                .findByMessageIdAndUserIdAndEmoji(messageId, currentUserId, emoji)
                .orElseGet(() -> {
                    MessageReaction newReaction = new MessageReaction();
                    newReaction.setMessage(message);
                    newReaction.setUserId(currentUserId);
                    newReaction.setEmoji(emoji);
                    return newReaction;
                });

        return mapper.toReactionResponse(reactionRepository.save(reaction));
    }

    @Transactional
    public void removeReaction(UUID currentUserId, UUID messageId, String emoji) {
        Message message = findMessage(messageId);
        roomService.ensureMember(message.getRoom().getId(), currentUserId);

        MessageReaction reaction = reactionRepository
                .findByMessageIdAndUserIdAndEmoji(messageId, currentUserId, emoji)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reaction not found"));
        reactionRepository.delete(reaction);
    }

    private ChatRoom findRoom(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));
    }

    private Message findMessage(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Message not found"));
    }

    private void ensureSender(Message message, UUID currentUserId) {
        if (!message.getSenderId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only sender can update this message");
        }
    }
}
