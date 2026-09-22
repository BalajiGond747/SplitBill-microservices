package com.splitbill.paymentservice.dto.response;

import com.splitbill.paymentservice.entity.Payment.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;

    private Long userId;

    private Long groupId;

    private Long settlementId;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private BigDecimal amount;

    private String currency;

    private PaymentStatus status;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}