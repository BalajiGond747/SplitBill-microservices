package com.splitbill.settlementservice.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceSettlementRequest {

    private Long groupId;

    private Long fromUserId;

    private Long toUserId;

    private BigDecimal amount;
}