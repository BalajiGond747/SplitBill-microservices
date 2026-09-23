package com.splitbill.notificationservice.service.impl;

import com.splitbill.notificationservice.dto.response.NotificationResponse;
import com.splitbill.notificationservice.entity.Notification;
import com.splitbill.notificationservice.exception.ResourceNotFoundException;
import com.splitbill.notificationservice.mapper.NotificationMapper;
import com.splitbill.notificationservice.repository.NotificationRepository;
import com.splitbill.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByUser(Long userId) {

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotificationsByUser(Long userId) {

        return notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public NotificationResponse markAsRead(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        notification.setRead(true);

        Notification savedNotification = notificationRepository.save(notification);

        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    public void markAllAsRead(Long userId) {

        List<Notification> notifications = notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false);

        notifications.forEach(notification -> notification.setRead(true));

        notificationRepository.saveAll(notifications);
    }
}