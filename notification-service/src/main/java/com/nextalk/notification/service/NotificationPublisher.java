package com.nextalk.notification.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nextalk.notification.dto.CreateNotificationRequest;

@Service
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public NotificationPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.notification.exchange}") String exchange,
            @Value("${app.notification.routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(CreateNotificationRequest request) {
        rabbitTemplate.convertAndSend(exchange, routingKey, request);
    }
}
