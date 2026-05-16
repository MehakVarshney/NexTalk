package com.nextalk.chat.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextalk.chat.entity.UserPresence;

public interface UserPresenceRepository extends JpaRepository<UserPresence, UUID> {
}
