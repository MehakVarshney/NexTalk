package com.nextalk.chat.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nextalk.chat.dto.AddMemberRequest;
import com.nextalk.chat.dto.CreateDirectRoomRequest;
import com.nextalk.chat.dto.CreateRoomRequest;
import com.nextalk.chat.dto.MemberResponse;
import com.nextalk.chat.dto.RoomAvatarUpdateRequest;
import com.nextalk.chat.dto.RoomResponse;
import com.nextalk.chat.security.CurrentUser;
import com.nextalk.chat.service.RoomService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse createGroupRoom(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody CreateRoomRequest request
    ) {
        return roomService.createGroupRoom(currentUser.getUserId(), request);
    }

    @PostMapping("/direct")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse createDirectRoom(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody CreateDirectRoomRequest request
    ) {
        return roomService.createDirectRoom(currentUser.getUserId(), request);
    }

    @GetMapping
    public List<RoomResponse> getMyRooms(@AuthenticationPrincipal CurrentUser currentUser) {
        return roomService.getMyRooms(currentUser.getUserId());
    }

    @GetMapping("/{roomId}")
    public RoomResponse getRoom(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID roomId
    ) {
        return roomService.getRoom(currentUser.getUserId(), roomId);
    }

    @PostMapping("/{roomId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse addMember(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID roomId,
            @Valid @RequestBody AddMemberRequest request
    ) {
        return roomService.addMember(currentUser.getUserId(), roomId, request);
    }

    @DeleteMapping("/{roomId}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveRoom(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID roomId
    ) {
        roomService.leaveRoom(currentUser.getUserId(), roomId);
    }

    /** Admin-only: remove another member from a group room. */
    @DeleteMapping("/{roomId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID roomId,
            @PathVariable UUID userId
    ) {
        roomService.removeMember(currentUser.getUserId(), roomId, userId);
    }

    @org.springframework.web.bind.annotation.PatchMapping("/{roomId}/avatar")
    public RoomResponse updateRoomAvatar(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID roomId,
            @Valid @RequestBody RoomAvatarUpdateRequest request
    ) {
        return roomService.updateRoomAvatar(currentUser.getUserId(), roomId, request.avatarUrl());
    }
}
