package com.nextalk.chat.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextalk.chat.dto.PresenceRequest;
import com.nextalk.chat.dto.PresenceResponse;
import com.nextalk.chat.entity.PresenceStatus;
import com.nextalk.chat.entity.UserPresence;
import com.nextalk.chat.repository.UserPresenceRepository;

@Service
public class PresenceService {

    private final UserPresenceRepository presenceRepository;
    private final ChatMapper mapper;

    public PresenceService(UserPresenceRepository presenceRepository, ChatMapper mapper) {
        this.presenceRepository = presenceRepository;
        this.mapper = mapper;
    }

    @Transactional
    public PresenceResponse updatePresence(UUID currentUserId, PresenceRequest request) {
        UserPresence presence = presenceRepository.findById(currentUserId).orElseGet(() -> {
            UserPresence newPresence = new UserPresence();
            newPresence.setUserId(currentUserId);
            return newPresence;
        });

        presence.setStatus(request.status());
        presence.setLastSeenAt(Instant.now());
        return mapper.toPresenceResponse(presenceRepository.save(presence));
    }

    @Transactional(readOnly = true)
    public PresenceResponse getPresence(UUID userId) {
        UserPresence presence = presenceRepository.findById(userId).orElseGet(() -> {
            UserPresence defaultPresence = new UserPresence();
            defaultPresence.setUserId(userId);
            defaultPresence.setStatus(PresenceStatus.OFFLINE);
            defaultPresence.setLastSeenAt(Instant.now());
            return defaultPresence;
        });
        return mapper.toPresenceResponse(presence);
    }
}
