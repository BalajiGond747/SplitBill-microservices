package com.splitbill.paymentservice.service;

import com.splitbill.paymentservice.dto.request.CreatePaymentRequest;
import com.splitbill.paymentservice.dto.request.VerifyPaymentRequest;
import com.splitbill.paymentservice.dto.response.PaymentOrderResponse;
import com.splitbill.paymentservice.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentOrderResponse createPaymentOrder(CreatePaymentRequest request);

    PaymentResponse verifyPayment(VerifyPaymentRequest request);

    PaymentResponse getPaymentById(Long id);

    List<PaymentResponse> getPaymentsByUser(Long userId);

    List<PaymentResponse> getPaymentsByGroup(Long groupId);
}