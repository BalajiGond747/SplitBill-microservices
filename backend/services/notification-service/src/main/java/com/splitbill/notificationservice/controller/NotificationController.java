package com.splitbill.notificationservice.controller;

import com.splitbill.notificationservice.dto.response.ApiResponse;
import com.splitbill.notificationservice.dto.response.NotificationResponse;
import com.splitbill.notificationservice.service.NotificationService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ApiResponse<List<NotificationResponse>> getUserNotifications(@PathVariable @Positive(message = "User ID must be positive") Long userId) {

        return ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Notifications retrieved successfully")
                .data(notificationService.getNotificationsByUser(userId))
                .build();
    }

    @GetMapping("/user/{userId}/unread")
    public ApiResponse<List<NotificationResponse>> getUnreadNotifications(@PathVariable @Positive(message = "User ID must be positive") Long userId) {

        return ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Unread notifications retrieved successfully")
                .data(notificationService.getUnreadNotificationsByUser(userId))
                .build();
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse> markAsRead(@PathVariable @Positive(message = "Notification ID must be positive") Long notificationId) {

        return ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification marked as read")
                .data(notificationService.markAsRead(notificationId))
                .build();
    }

    @PatchMapping("/user/{userId}/read-all")
    public ApiResponse<Void> markAllAsRead(@PathVariable @Positive(message = "User ID must be positive") Long userId) {

        notificationService.markAllAsRead(userId);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("All notifications marked as read")
                .data(null)
                .build();
    }
}