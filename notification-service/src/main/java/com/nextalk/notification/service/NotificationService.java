package com.nextalk.notification.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextalk.notification.dto.CreateNotificationRequest;
import com.nextalk.notification.dto.NotificationResponse;
import com.nextalk.notification.dto.UnreadCountResponse;
import com.nextalk.notification.entity.Notification;
import com.nextalk.notification.exception.ApiException;
import com.nextalk.notification.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper mapper;

    public NotificationService(NotificationRepository notificationRepository, NotificationMapper mapper) {
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
    }

    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setRecipientId(request.recipientId());
        notification.setActorId(request.actorId());
        notification.setType(request.type());
        notification.setTitle(request.title().trim());
        notification.setBody(request.body().trim());
        notification.setResourceType(request.resourceType());
        notification.setResourceId(request.resourceId());
        notification.setRead(false);

        return mapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(UUID currentUserId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUserId, pageRequest)
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID currentUserId) {
        return new UnreadCountResponse(notificationRepository.countByRecipientIdAndReadFalse(currentUserId));
    }

    @Transactional
    public NotificationResponse markAsRead(UUID currentUserId, UUID notificationId) {
        Notification notification = findOwnedNotification(currentUserId, notificationId);
        notification.setRead(true);
        notification.setReadAt(Instant.now());
        return mapper.toResponse(notification);
    }

    @Transactional
    public void markAllAsRead(UUID currentUserId) {
        notificationRepository.markAllAsReadByRecipientId(currentUserId, Instant.now());
    }

    @Transactional
    public void delete(UUID currentUserId, UUID notificationId) {
        Notification notification = findOwnedNotification(currentUserId, notificationId);
        notificationRepository.delete(notification);
    }

    private Notification findOwnedNotification(UUID currentUserId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!notification.getRecipientId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot access this notification");
        }
        return notification;
    }
}
