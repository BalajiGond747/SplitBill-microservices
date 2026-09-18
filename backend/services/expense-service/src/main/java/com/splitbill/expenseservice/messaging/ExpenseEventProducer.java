package com.splitbill.expenseservice.messaging;

import com.splitbill.expenseservice.event.ExpenseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpenseEventProducer {

    private static final String EXPENSE_EVENTS_TOPIC = "expense-events";

    private final KafkaTemplate<String, ExpenseEvent> kafkaTemplate;

    public void publish(ExpenseEvent event) {
        kafkaTemplate.send(
                EXPENSE_EVENTS_TOPIC,
                event.getExpenseId().toString(),
                event
        );
    }
}