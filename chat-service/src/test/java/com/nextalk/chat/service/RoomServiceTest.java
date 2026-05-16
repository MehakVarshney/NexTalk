package com.nextalk.chat.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nextalk.chat.dto.AddMemberRequest;
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

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private ChatRoomRepository roomRepository;
    @Mock
    private RoomMemberRepository memberRepository;
    @Mock
    private ChatMapper mapper;

    @InjectMocks
    private RoomService roomService;

    private UUID userId;
    private UUID roomId;
    private ChatRoom mockRoom;
    private RoomResponse mockRoomResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roomId = UUID.randomUUID();

        mockRoom = new ChatRoom();
        mockRoom.setId(roomId);
        mockRoom.setName("Test Group");
        mockRoom.setType(RoomType.GROUP);
        mockRoom.setCreatedBy(userId);

        mockRoomResponse = new RoomResponse(
                roomId, "Test Group", null, RoomType.GROUP, userId, null, List.of()
        );
    }

    @Test
    void createGroupRoom_Success() {
        CreateRoomRequest request = new CreateRoomRequest("Test Group");

        when(roomRepository.save(any(ChatRoom.class))).thenReturn(mockRoom);
        when(memberRepository.save(any(RoomMember.class))).thenAnswer(i -> i.getArgument(0));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(memberRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(true);
        when(memberRepository.findByRoomId(roomId)).thenReturn(List.of());
        when(mapper.toRoomResponse(eq(mockRoom), anyList())).thenReturn(mockRoomResponse);

        RoomResponse response = roomService.createGroupRoom(userId, request);

        assertNotNull(response);
        assertEquals("Test Group", response.name());
        verify(roomRepository, times(1)).save(any(ChatRoom.class));
        verify(memberRepository, times(1)).save(any(RoomMember.class));
    }

    @Test
    void addMember_Success_AsOwner() {
        UUID newUserId = UUID.randomUUID();
        AddMemberRequest request = new AddMemberRequest(newUserId);
        
        RoomMember ownerMember = new RoomMember();
        ownerMember.setRole(MemberRole.OWNER);

        when(memberRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(true);
        when(memberRepository.findByRoomIdAndUserId(roomId, userId)).thenReturn(Optional.of(ownerMember));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(memberRepository.existsByRoomIdAndUserId(roomId, newUserId)).thenReturn(false);
        when(memberRepository.save(any(RoomMember.class))).thenAnswer(i -> i.getArgument(0));
        
        MemberResponse mockMemberResponse = new MemberResponse(UUID.randomUUID(), newUserId, MemberRole.MEMBER, null);
        when(mapper.toMemberResponse(any(RoomMember.class))).thenReturn(mockMemberResponse);

        MemberResponse response = roomService.addMember(userId, roomId, request);

        assertNotNull(response);
        assertEquals(newUserId, response.userId());
        assertEquals(MemberRole.MEMBER, response.role());
    }

    @Test
    void addMember_Forbidden_AsRegularMember() {
        UUID newUserId = UUID.randomUUID();
        AddMemberRequest request = new AddMemberRequest(newUserId);
        
        RoomMember regularMember = new RoomMember();
        regularMember.setRole(MemberRole.MEMBER);

        when(memberRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(true);
        when(memberRepository.findByRoomIdAndUserId(roomId, userId)).thenReturn(Optional.of(regularMember));

        assertThrows(ApiException.class, () -> roomService.addMember(userId, roomId, request));
    }
}
