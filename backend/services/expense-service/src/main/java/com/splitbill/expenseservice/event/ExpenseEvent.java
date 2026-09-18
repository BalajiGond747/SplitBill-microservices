package com.splitbill.expenseservice.event;

import com.splitbill.expenseservice.entity.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private Expense.SplitType splitType;

    private List<ExpenseSplitEvent> splits;

    public enum EventType {
        EXPENSE_CREATED,
        EXPENSE_UPDATED,
        EXPENSE_DELETED
    }

}