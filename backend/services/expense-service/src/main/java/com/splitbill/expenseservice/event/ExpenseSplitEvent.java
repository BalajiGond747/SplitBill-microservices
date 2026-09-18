package com.splitbill.expenseservice.event;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public  class ExpenseSplitEvent {

    private Long userId;

    private BigDecimal amount;

    private BigDecimal percentage;
}