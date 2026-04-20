package com.nextalk.auth.mapper;

import org.mapstruct.Mapper;

import com.nextalk.auth.dto.UserResponse;
import com.nextalk.auth.entity.AppUser;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(AppUser user);
}
