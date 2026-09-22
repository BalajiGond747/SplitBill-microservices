package com.splitbill.paymentservice.mapper;

import com.splitbill.paymentservice.dto.response.PaymentResponse;
import com.splitbill.paymentservice.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {

        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .groupId(payment.getGroupId())
                .settlementId(payment.getSettlementId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}