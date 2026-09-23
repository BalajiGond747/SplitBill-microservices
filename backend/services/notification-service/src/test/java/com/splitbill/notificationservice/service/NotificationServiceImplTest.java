package com.splitbill.notificationservice.service;

import com.splitbill.notificationservice.dto.response.NotificationResponse;
import com.splitbill.notificationservice.entity.Notification;
import com.splitbill.notificationservice.mapper.NotificationMapper;
import com.splitbill.notificationservice.repository.NotificationRepository;
import com.splitbill.notificationservice.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {

        notification = Notification.builder()
                .id(1L)
                .userId(10L)
                .type(Notification.NotificationType.EXPENSE_CREATED)
                .title("New expense added")
                .message("A new expense of ₹500 was added.")
                .referenceType("EXPENSE")
                .referenceId(100L)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getNotificationsByUser_shouldReturnNotifications() {

        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .userId(10L)
                .type(Notification.NotificationType.EXPENSE_CREATED)
                .title("New expense added")
                .message("A new expense of ₹500 was added.")
                .read(false)
                .build();

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(notification));

        when(notificationMapper.toResponse(notification)).thenReturn(response);

        List<NotificationResponse> result = notificationService.getNotificationsByUser(10L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0)
                .getId());
        assertEquals(10L, result.get(0)
                .getUserId());

        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(10L);

        verify(notificationMapper).toResponse(notification);
    }

    @Test
    void markAsRead_shouldMarkNotificationAsRead() {

        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .userId(10L)
                .read(true)
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        when(notificationRepository.save(notification)).thenReturn(notification);

        when(notificationMapper.toResponse(notification)).thenReturn(response);

        NotificationResponse result = notificationService.markAsRead(1L);

        assertTrue(notification.isRead());
        assertTrue(result.isRead());

        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_whenNotificationDoesNotExist_shouldThrowException() {

        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> notificationService.markAsRead(999L));

        verify(notificationRepository).findById(999L);
        verifyNoInteractions(notificationMapper);
    }

    @Test
    void markAllAsRead_shouldMarkAllUnreadNotifications() {

        Notification secondNotification = Notification.builder()
                .id(2L)
                .userId(10L)
                .type(Notification.NotificationType.EXPENSE_UPDATED)
                .title("Expense updated")
                .message("Expense updated.")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(10L, false)).thenReturn(List.of(notification, secondNotification));

        notificationService.markAllAsRead(10L);

        assertTrue(notification.isRead());
        assertTrue(secondNotification.isRead());

        verify(notificationRepository).saveAll(List.of(notification, secondNotification));
    }
}