package com.nextalk.chat.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nextalk.chat.entity.Message;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByRoomIdOrderByCreatedAtDesc(UUID roomId, Pageable pageable);
}
