package com.nextalk.auth.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextalk.auth.dto.StatusUpdateRequest;
import com.nextalk.auth.dto.UserResponse;
import com.nextalk.auth.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getProfile(Principal principal) {
        return userService.getProfile(principal.getName());
    }

    @PatchMapping("/me/status")
    public UserResponse updateStatus(Principal principal, @Valid @RequestBody StatusUpdateRequest request) {
        return userService.updateStatus(principal.getName(), request);
    }
}
