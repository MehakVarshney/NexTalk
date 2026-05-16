package com.nextalk.chat.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextalk.chat.dto.AddMemberRequest;
import com.nextalk.chat.dto.CreateDirectRoomRequest;
import com.nextalk.chat.dto.CreateRoomRequest;
import com.nextalk.chat.dto.MemberResponse;
import com.nextalk.chat.dto.RoomResponse;
import com.nextalk.chat.entity.ChatRoom;
import com.nextalk.chat.entity.MemberRole;
import com.nextalk.chat.entity.RoomMember;
import com.nextalk.chat.entity.RoomType;
import com.nextalk.chat.exception.ApiException;
import com.nextalk.chat.repository.ChatRoomRepository;
import com.nextalk.chat.repository.RoomMemberRepository;

@Service
public class RoomService {

    private final ChatRoomRepository roomRepository;
    private final RoomMemberRepository memberRepository;
    private final ChatMapper mapper;

    public RoomService(
            ChatRoomRepository roomRepository,
            RoomMemberRepository memberRepository,
            ChatMapper mapper
    ) {
        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
        this.mapper = mapper;
    }

    @Transactional
    public RoomResponse createGroupRoom(UUID currentUserId, CreateRoomRequest request) {
        ChatRoom room = new ChatRoom();
        room.setName(request.name().trim());
        room.setType(RoomType.GROUP);
        room.setCreatedBy(currentUserId);
        ChatRoom savedRoom = roomRepository.save(room);

        addMember(savedRoom, currentUserId, MemberRole.OWNER);
        return getRoom(currentUserId, savedRoom.getId());
    }

    @Transactional
    public RoomResponse createDirectRoom(UUID currentUserId, CreateDirectRoomRequest request) {
        if (currentUserId.equals(request.otherUserId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Direct room requires another user");
        }

        List<UUID> commonRoomIds = memberRepository.findCommonRoomIds(currentUserId, request.otherUserId());
        for (UUID roomId : commonRoomIds) {
            ChatRoom existingRoom = roomRepository.findById(roomId).orElse(null);
            if (existingRoom != null && existingRoom.getType() == RoomType.DIRECT) {
                return getRoom(currentUserId, existingRoom.getId());
            }
        }

        ChatRoom room = new ChatRoom();
        String displayName = request.displayName() == null ? "" : request.displayName().trim();
        room.setName(displayName.isBlank() ? "Direct Chat" : displayName);
        room.setType(RoomType.DIRECT);
        room.setCreatedBy(currentUserId);
        ChatRoom savedRoom = roomRepository.save(room);

        addMember(savedRoom, currentUserId, MemberRole.OWNER);
        addMember(savedRoom, request.otherUserId(), MemberRole.MEMBER);
        return getRoom(currentUserId, savedRoom.getId());
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getMyRooms(UUID currentUserId) {
        return memberRepository.findByUserId(currentUserId)
                .stream()
                .map(RoomMember::getRoom)
                .map(room -> mapper.toRoomResponse(room, memberRepository.findByRoomId(room.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoom(UUID currentUserId, UUID roomId) {
        ensureMember(roomId, currentUserId);
        ChatRoom room = findRoom(roomId);
        return mapper.toRoomResponse(room, memberRepository.findByRoomId(roomId));
    }

    @Transactional
    public MemberResponse addMember(UUID currentUserId, UUID roomId, AddMemberRequest request) {
        ensureMember(roomId, currentUserId);
        ensureAdmin(roomId, currentUserId);
        ChatRoom room = findRoom(roomId);
        if (room.getType() == RoomType.DIRECT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Members cannot be added to direct rooms");
        }
        if (memberRepository.existsByRoomIdAndUserId(roomId, request.userId())) {
            throw new ApiException(HttpStatus.CONFLICT, "User is already a room member");
        }
        return mapper.toMemberResponse(addMember(room, request.userId(), MemberRole.MEMBER));
    }

    @Transactional
    public void removeMember(UUID currentUserId, UUID roomId, UUID targetUserId) {
        ensureMember(roomId, currentUserId);
        ensureAdmin(roomId, currentUserId);
        if (currentUserId.equals(targetUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Admins cannot remove themselves — use leave instead");
        }
        RoomMember target = memberRepository.findByRoomIdAndUserId(roomId, targetUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User is not a member of this room"));
        memberRepository.delete(target);
    }

    @Transactional
    public RoomResponse updateRoomAvatar(UUID currentUserId, UUID roomId, String avatarUrl) {
        ensureMember(roomId, currentUserId);
        ensureAdmin(roomId, currentUserId);
        ChatRoom room = findRoom(roomId);
        if (room.getType() == RoomType.DIRECT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Direct rooms do not have group avatars");
        }
        room.setAvatarUrl(avatarUrl);
        ChatRoom saved = roomRepository.save(room);
        return mapper.toRoomResponse(saved, memberRepository.findByRoomId(roomId));
    }

    @Transactional
    public void leaveRoom(UUID currentUserId, UUID roomId) {
        RoomMember member = memberRepository.findByRoomIdAndUserId(roomId, currentUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room membership not found"));
        memberRepository.delete(member);
    }

    public void ensureMember(UUID roomId, UUID userId) {
        if (!memberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not a member of this room");
        }
    }

    private void ensureAdmin(UUID roomId, UUID userId) {
        MemberRole role = memberRepository.findByRoomIdAndUserId(roomId, userId)
                .map(RoomMember::getRole)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You are not a member of this room"));
        if (role != MemberRole.OWNER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only group admins can perform this action");
        }
    }

    private RoomMember addMember(ChatRoom room, UUID userId, MemberRole role) {
        RoomMember member = new RoomMember();
        member.setRoom(room);
        member.setUserId(userId);
        member.setRole(role);
        return memberRepository.save(member);
    }

    private ChatRoom findRoom(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));
    }
}
