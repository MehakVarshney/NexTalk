package com.nextalk.auth.controller;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextalk.auth.dto.AvatarUpdateRequest;
import com.nextalk.auth.dto.StatusUpdateRequest;
import com.nextalk.auth.dto.UserResponse;
import com.nextalk.auth.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse getProfile(Principal principal) {
        return userService.getProfile(principal.getName());
    }

    @GetMapping("/search")
    public List<UserResponse> searchUsers(
            Principal principal,
            @RequestParam String query
    ) {
        return userService.searchUsers(principal.getName(), query);
    }

    @GetMapping("/directory")
    public List<UserResponse> directory(@RequestParam Set<UUID> ids) {
        return userService.directoryLookup(ids);
    }

    @PatchMapping("/me/status")
    public UserResponse updateStatus(Principal principal, @Valid @RequestBody StatusUpdateRequest request) {
        return userService.updateStatus(principal.getName(), request);
    }

    @PatchMapping("/me/avatar")
    public UserResponse updateAvatar(Principal principal, @Valid @RequestBody AvatarUpdateRequest request) {
        return userService.updateAvatar(principal.getName(), request);
    }
}

