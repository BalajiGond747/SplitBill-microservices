package com.splitbill.paymentservice.dto.response;

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

    private String status;

    private String paymentId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}