package com.nextalk.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nextalk.notification.dto.CreateNotificationRequest;
import com.nextalk.notification.dto.NotificationResponse;
import com.nextalk.notification.entity.Notification;
import com.nextalk.notification.entity.NotificationType;
import com.nextalk.notification.exception.ApiException;
import com.nextalk.notification.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private NotificationMapper mapper;

    @InjectMocks
    private NotificationService notificationService;

    private UUID userId;
    private UUID notificationId;
    private Notification mockNotification;
    private NotificationResponse mockResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        mockNotification = new Notification();
        mockNotification.setId(notificationId);
        mockNotification.setRecipientId(userId);
        mockNotification.setActorId(UUID.randomUUID());
        mockNotification.setType(NotificationType.MESSAGE);
        mockNotification.setTitle("New Message");
        mockNotification.setBody("You have a new message.");
        mockNotification.setRead(false);

        mockResponse = new NotificationResponse(
            notificationId, userId, mockNotification.getActorId(), NotificationType.MESSAGE,
            "New Message", "You have a new message.", null, null, false, null, null
        );
    }

    @Test
    void create_Success() {
        CreateNotificationRequest request = new CreateNotificationRequest(
            userId, UUID.randomUUID(), NotificationType.MESSAGE, "New Message", "You have a new message.", null, null
        );

        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);
        when(mapper.toResponse(any(Notification.class))).thenReturn(mockResponse);

        NotificationResponse response = notificationService.create(request);

        assertNotNull(response);
        assertEquals("New Message", response.title());
        assertFalse(response.read());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(mockNotification));
        when(mapper.toResponse(any(Notification.class))).thenReturn(mockResponse);

        notificationService.markAsRead(userId, notificationId);

        assertTrue(mockNotification.isRead());
        assertNotNull(mockNotification.getReadAt());
        verify(notificationRepository, times(1)).findById(notificationId);
    }

    @Test
    void markAsRead_Forbidden() {
        UUID otherUserId = UUID.randomUUID();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(mockNotification));

        assertThrows(ApiException.class, () -> notificationService.markAsRead(otherUserId, notificationId));
    }
}
