package com.nextalk.notification.service;

import org.springframework.stereotype.Component;

import com.nextalk.notification.dto.NotificationResponse;
import com.nextalk.notification.entity.Notification;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientId(),
                notification.getActorId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getResourceType(),
                notification.getResourceId(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
