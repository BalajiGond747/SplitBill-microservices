package com.splitbill.notificationservice.controller;

import com.splitbill.notificationservice.dto.response.NotificationResponse;
import com.splitbill.notificationservice.entity.Notification;
import com.splitbill.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void getUserNotifications_shouldReturnNotifications() throws Exception {

        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .userId(10L)
                .type(Notification.NotificationType.EXPENSE_CREATED)
                .title("New expense added")
                .message("A new expense was added.")
                .read(false)
                .build();

        when(notificationService.getNotificationsByUser(10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/notifications/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(10));
    }

    @Test
    void getUnreadNotifications_shouldReturnNotifications() throws Exception {

        when(notificationService.getUnreadNotificationsByUser(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notifications/user/10/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void markAsRead_shouldReturnUpdatedNotification() throws Exception {

        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .userId(10L)
                .read(true)
                .build();

        when(notificationService.markAsRead(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.read").value(true));
    }

    @Test
    void markAllAsRead_shouldReturnSuccess() throws Exception {

        mockMvc.perform(patch("/api/v1/notifications/user/10/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All notifications marked as read"));
    }
}