package com.nextalk.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextalk.auth.dto.StatusUpdateRequest;
import com.nextalk.auth.dto.UserResponse;
import com.nextalk.auth.entity.AppUser;
import com.nextalk.auth.exception.ApiException;
import com.nextalk.auth.mapper.UserMapper;
import com.nextalk.auth.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository userRepository;
    private final UserMapper userMapper;
    public UserService(
            AppUserRepository userRepository,
            UserMapper userMapper
    ) {
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

    private AppUser findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
