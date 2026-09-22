package com.splitbill.paymentservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {

    @NotNull(message = "User id is required")
    @Positive(message = "User id must be positive")
    private Long userId;

    @Positive(message = "Group id must be positive")
    private Long groupId;

    @Positive(message = "Settlement id must be positive")
    private Long settlementId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 17, fraction = 2, message = "Amount must have at most 17 integer digits and 2 decimal places")
    private BigDecimal amount;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}