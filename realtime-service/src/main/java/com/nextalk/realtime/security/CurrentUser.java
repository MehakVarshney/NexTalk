package com.nextalk.realtime.security;

import java.security.Principal;
import java.util.UUID;

public class CurrentUser implements Principal {

    private final UUID userId;
    private final String email;
    private final String displayName;

    public CurrentUser(UUID userId, String email, String displayName) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getName() {
        return userId.toString();
    }
}
