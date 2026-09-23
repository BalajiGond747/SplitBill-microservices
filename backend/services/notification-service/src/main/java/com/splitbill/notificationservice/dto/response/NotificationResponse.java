package com.splitbill.notificationservice.dto.response;

import com.splitbill.notificationservice.entity.Notification;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;

    private Long userId;

    private Notification.NotificationType type;

    private String title;

    private String message;

    private String referenceType;

    private Long referenceId;

    private boolean read;

    private LocalDateTime createdAt;
}