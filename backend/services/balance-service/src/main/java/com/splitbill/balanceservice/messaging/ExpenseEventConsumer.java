package com.splitbill.balanceservice.messaging;

import com.splitbill.balanceservice.event.ExpenseEvent;
import com.splitbill.balanceservice.service.impl.BalanceServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpenseEventConsumer {

    private final BalanceServiceImpl balanceService;

    @KafkaListener(topics = "expense-events", groupId = "balance-service", containerFactory = "expenseEventKafkaListenerContainerFactory")
    public void consume(ExpenseEvent event) {

        System.out.println("========== BALANCE SERVICE RECEIVED KAFKA EVENT ==========");

        System.out.println("Event Type: " + event.getEventType());

        System.out.println("Expense ID: " + event.getExpenseId());

        System.out.println("Group ID: " + event.getGroupId());

        System.out.println("Paid By: " + event.getPaidBy());

        System.out.println("Splits: " + event.getSplits());

        switch (event.getEventType()) {

            case EXPENSE_CREATED -> balanceService.handleExpenseCreated(event);

            case EXPENSE_UPDATED -> balanceService.handleExpenseUpdated(event);

            case EXPENSE_DELETED -> balanceService.handleExpenseDeleted(event);
        }
    }
}