package com.splitbill.balanceservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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