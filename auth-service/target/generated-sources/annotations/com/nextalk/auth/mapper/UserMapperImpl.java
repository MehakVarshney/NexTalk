package com.nextalk.auth.mapper;

import com.nextalk.auth.dto.UserResponse;
import com.nextalk.auth.entity.AppUser;
import com.nextalk.auth.entity.AuthProvider;
import com.nextalk.auth.entity.UserStatus;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-21T10:22:12+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 25.0.1 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(AppUser user) {
        if ( user == null ) {
            return null;
        }

        UUID id = null;
        String name = null;
        String email = null;
        String avatarUrl = null;
        AuthProvider provider = null;
        UserStatus status = null;
        Instant createdAt = null;

        UserResponse userResponse = new UserResponse( id, name, email, avatarUrl, provider, status, createdAt );

        return userResponse;
    }
}
