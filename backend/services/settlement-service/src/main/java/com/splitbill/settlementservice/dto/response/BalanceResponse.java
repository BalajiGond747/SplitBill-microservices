package com.splitbill.settlementservice.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponse {

    private Long id;

    private Long groupId;

    private Long fromUserId;

    private Long toUserId;

    private BigDecimal amount;
}