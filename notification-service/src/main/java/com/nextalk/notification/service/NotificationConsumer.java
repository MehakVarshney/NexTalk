package com.nextalk.notification.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.nextalk.notification.dto.CreateNotificationRequest;

@Service
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${app.notification.queue}")
    public void consume(CreateNotificationRequest request) {
        notificationService.create(request);
    }
}
