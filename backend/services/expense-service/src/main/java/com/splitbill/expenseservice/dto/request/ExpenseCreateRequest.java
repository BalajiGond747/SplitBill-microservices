package com.splitbill.expenseservice.dto.request;

import com.splitbill.expenseservice.entity.Expense;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ExpenseCreateRequest {

    @NotNull(message = "Group id is required")
    @Positive(message = "Group id must be positive")
    private Long groupId;

    @NotNull(message = "Paid by is required")
    @Positive(message = "Paid by must be positive")
    private Long paidBy;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 17, fraction = 2, message = "Amount must have at most 17 integer digits and 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    @NotNull(message = "Split type is required")
    private Expense.SplitType splitType;

    @NotEmpty(message = "At least one split is required")
    @Valid
    private List<ExpenseSplitRequest> splits;
}