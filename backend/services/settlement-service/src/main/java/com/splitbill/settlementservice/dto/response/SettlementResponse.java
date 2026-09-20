package com.splitbill.settlementservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementResponse {

    private Long id;

    private Long groupId;

    private Long fromUserId;

    private Long toUserId;

    private BigDecimal amount;

    private LocalDateTime settlementDate;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}