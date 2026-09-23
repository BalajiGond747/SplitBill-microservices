package com.splitbill.notificationservice.service;

import com.splitbill.notificationservice.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getNotificationsByUser(Long userId);

    List<NotificationResponse> getUnreadNotificationsByUser(Long userId);

    NotificationResponse markAsRead(Long notificationId);

    void markAllAsRead(Long userId);
}