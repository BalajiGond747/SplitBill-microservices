package com.splitbill.notificationservice.dto.event;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseEvent {

    private EventType eventType;

    private Long expenseId;

    private Long groupId;

    private Long paidBy;

    private BigDecimal amount;

    private SplitType splitType;

    private List<ExpenseSplitEvent> splits;

    private List<ExpenseSplitEvent> previousSplits;

    public enum EventType {
        EXPENSE_CREATED,
        EXPENSE_UPDATED,
        EXPENSE_DELETED
    }

    public enum SplitType {
        EQUAL,
        EXACT,
        PERCENTAGE
    }
}