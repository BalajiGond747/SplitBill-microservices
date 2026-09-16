package com.splitbill.expenseservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ExpenseSplitRequest {

    @NotNull(message = "User id is required")
    @Positive(message = "User id must be positive")
    private Long userId;

    @DecimalMin(value = "0.01", message = "Split amount must be greater than zero")
    @Digits(integer = 17, fraction = 2, message = "Split amount must have at most 17 integer digits and 2 decimal places")
    private BigDecimal amount;

    @DecimalMin(value = "0.0000", message = "Percentage cannot be negative")
    @DecimalMax(value = "100.0000", message = "Percentage cannot exceed 100")
    @Digits(integer = 3, fraction = 4, message = "Percentage must have at most 3 integer digits and 4 decimal places")
    private BigDecimal percentage;
}