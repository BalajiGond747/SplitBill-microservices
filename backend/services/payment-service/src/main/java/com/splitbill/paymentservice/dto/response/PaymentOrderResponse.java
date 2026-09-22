package com.splitbill.paymentservice.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrderResponse {

    private Long paymentId;

    private String razorpayOrderId;

    private BigDecimal amount;

    private String currency;

    private String razorpayKeyId;
}