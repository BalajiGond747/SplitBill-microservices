package com.splitbill.expenseservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSplitResponse {

    private Long id;
    private Long userId;
    private BigDecimal amount;
    private BigDecimal percentage;
}