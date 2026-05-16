package com.nextalk.chat.security;

import java.util.UUID;

public class CurrentUser {

    private final UUID userId;
    private final String email;
    private final String name;

    public CurrentUser(UUID userId, String email, String name) {
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
