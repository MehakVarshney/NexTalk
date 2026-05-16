package com.nextalk.auth.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextalk.auth.dto.AvatarUpdateRequest;
import com.nextalk.auth.dto.StatusUpdateRequest;
import com.nextalk.auth.dto.UserResponse;
import com.nextalk.auth.entity.AppUser;
import com.nextalk.auth.exception.ApiException;
import com.nextalk.auth.mapper.UserMapper;
import com.nextalk.auth.repository.AppUserRepository;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(AppUserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        return userMapper.toResponse(findByEmail(email));
    }

    @Transactional
    public UserResponse updateStatus(String email, StatusUpdateRequest request) {
        AppUser user = findByEmail(email);
        user.setStatus(request.status());
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateAvatar(String email, AvatarUpdateRequest request) {
        AppUser user = findByEmail(email);
        user.setAvatarUrl(request.avatarUrl());
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String currentEmail, String query) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.length() < 2) {
            return List.of();
        }

        String currentUserEmail = currentEmail == null ? "" : currentEmail.trim().toLowerCase();
        return userRepository
                .findTop8ByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByNameAsc(normalized, normalized)
                .stream()
                .filter(user -> !user.getEmail().equalsIgnoreCase(currentUserEmail))
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> directoryLookup(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return userRepository.findAllById(ids)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    private AppUser findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
