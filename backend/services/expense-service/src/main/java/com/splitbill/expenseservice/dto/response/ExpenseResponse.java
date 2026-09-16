package com.splitbill.expenseservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.splitbill.expenseservice.entity.Expense;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private Long id;
    private Long groupId;
    private Long paidBy;
    private BigDecimal amount;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;

    private Expense.SplitType splitType;

    private List<ExpenseSplitResponse> splits;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}