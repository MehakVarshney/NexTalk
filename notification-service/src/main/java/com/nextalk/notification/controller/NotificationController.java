package com.nextalk.notification.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nextalk.notification.dto.CreateNotificationRequest;
import com.nextalk.notification.dto.NotificationResponse;
import com.nextalk.notification.dto.UnreadCountResponse;
import com.nextalk.notification.security.CurrentUser;
import com.nextalk.notification.service.NotificationPublisher;
import com.nextalk.notification.service.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationPublisher notificationPublisher;

    public NotificationController(NotificationService notificationService, NotificationPublisher notificationPublisher) {
        this.notificationService = notificationService;
        this.notificationPublisher = notificationPublisher;
    }

    @PostMapping("/publish")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void publish(@Valid @RequestBody CreateNotificationRequest request) {
        notificationPublisher.publish(request);
    }

    @GetMapping
    public Page<NotificationResponse> getMyNotifications(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return notificationService.getMyNotifications(currentUser.getUserId(), page, size);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse getUnreadCount(@AuthenticationPrincipal CurrentUser currentUser) {
        return notificationService.getUnreadCount(currentUser.getUserId());
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID notificationId
    ) {
        return notificationService.markAsRead(currentUser.getUserId(), notificationId);
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(@AuthenticationPrincipal CurrentUser currentUser) {
        notificationService.markAllAsRead(currentUser.getUserId());
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID notificationId
    ) {
        notificationService.delete(currentUser.getUserId(), notificationId);
    }
}
