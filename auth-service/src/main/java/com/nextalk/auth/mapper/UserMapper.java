package com.nextalk.auth.mapper;

import org.springframework.stereotype.Component;

import com.nextalk.auth.dto.UserResponse;
import com.nextalk.auth.entity.AppUser;

@Component
public class UserMapper {

    public UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getProvider(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
