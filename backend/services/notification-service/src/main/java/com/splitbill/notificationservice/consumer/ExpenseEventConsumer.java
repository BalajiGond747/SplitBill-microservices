package com.splitbill.notificationservice.consumer;

import com.splitbill.notificationservice.dto.event.ExpenseEvent;
import com.splitbill.notificationservice.dto.event.ExpenseSplitEvent;
import com.splitbill.notificationservice.entity.Notification;
import com.splitbill.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseEventConsumer {

    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "expense-events", groupId = "notification-service")
    @Transactional
    public void consumeExpenseEvent(ExpenseEvent event) {

        if (event == null || event.getEventType() == null) {
            log.warn("Received invalid expense event");
            return;
        }

        log.info("Received expense event: type={}, expenseId={}, groupId={}", event.getEventType(), event.getExpenseId(), event.getGroupId());

        Set<Long> affectedUsers = getAffectedUsers(event);

        if (affectedUsers.isEmpty()) {
            log.warn("No affected users found for expense event: expenseId={}", event.getExpenseId());
            return;
        }

        Notification.NotificationType notificationType = mapNotificationType(event.getEventType());

        String title = buildTitle(event.getEventType());

        String message = buildMessage(event);

        for (Long userId : affectedUsers) {

            Notification notification = Notification.builder()
                    .userId(userId)
                    .type(notificationType)
                    .title(title)
                    .message(message)
                    .referenceType("EXPENSE")
                    .referenceId(event.getExpenseId())
                    .read(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }
    }

    private Set<Long> getAffectedUsers(ExpenseEvent event) {

        Set<Long> users = new HashSet<>();

        if (event.getPaidBy() != null) {
            users.add(event.getPaidBy());
        }

        addSplitUsers(users, event.getSplits());

        if (event.getEventType() == ExpenseEvent.EventType.EXPENSE_UPDATED || event.getEventType() == ExpenseEvent.EventType.EXPENSE_DELETED) {

            addSplitUsers(users, event.getPreviousSplits());
        }

        return users;
    }

    private void addSplitUsers(Set<Long> users, java.util.List<ExpenseSplitEvent> splits) {

        if (splits == null) {
            return;
        }

        splits.stream()
                .map(ExpenseSplitEvent::getUserId)
                .filter(java.util.Objects::nonNull)
                .forEach(users::add);
    }

    private Notification.NotificationType mapNotificationType(ExpenseEvent.EventType eventType) {

        return switch (eventType) {
            case EXPENSE_CREATED -> Notification.NotificationType.EXPENSE_CREATED;

            case EXPENSE_UPDATED -> Notification.NotificationType.EXPENSE_UPDATED;

            case EXPENSE_DELETED -> Notification.NotificationType.EXPENSE_DELETED;
        };
    }

    private String buildTitle(ExpenseEvent.EventType eventType) {

        return switch (eventType) {
            case EXPENSE_CREATED -> "New expense added";
            case EXPENSE_UPDATED -> "Expense updated";
            case EXPENSE_DELETED -> "Expense deleted";
        };
    }

    private String buildMessage(ExpenseEvent event) {

        String amount = event.getAmount() != null ? event.getAmount()
                .toPlainString() : "unknown";

        return switch (event.getEventType()) {
            case EXPENSE_CREATED -> "A new expense of ₹" + amount + " was added to group " + event.getGroupId() + ".";

            case EXPENSE_UPDATED -> "An expense of ₹" + amount + " in group " + event.getGroupId() + " was updated.";

            case EXPENSE_DELETED -> "An expense in group " + event.getGroupId() + " was deleted.";
        };
    }
}