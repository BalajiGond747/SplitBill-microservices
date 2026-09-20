package com.splitbill.settlementservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementCreateRequest {

    @NotNull(message = "Group id is required")
    @Positive(message = "Group id must be positive")
    private Long groupId;

    @NotNull(message = "From user id is required")
    @Positive(message = "From user id must be positive")
    private Long fromUserId;

    @NotNull(message = "To user id is required")
    @Positive(message = "To user id must be positive")
    private Long toUserId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 17, fraction = 2, message = "Amount must have at most 17 integer digits and 2 decimal places")
    private BigDecimal amount;



    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}