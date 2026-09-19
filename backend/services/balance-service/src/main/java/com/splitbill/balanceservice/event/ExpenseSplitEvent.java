package com.splitbill.balanceservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSplitEvent {

    private Long userId;
    private BigDecimal amount;
    private BigDecimal percentage;
}