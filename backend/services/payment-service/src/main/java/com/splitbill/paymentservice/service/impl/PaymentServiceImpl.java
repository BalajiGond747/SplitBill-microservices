package com.splitbill.paymentservice.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.splitbill.paymentservice.client.SettlementClient;
import com.splitbill.paymentservice.dto.request.CreatePaymentRequest;
import com.splitbill.paymentservice.dto.request.VerifyPaymentRequest;
import com.splitbill.paymentservice.dto.response.PaymentOrderResponse;
import com.splitbill.paymentservice.dto.response.PaymentResponse;
import com.splitbill.paymentservice.dto.response.SettlementResponse;
import com.splitbill.paymentservice.entity.Payment;
import com.splitbill.paymentservice.exception.PaymentProcessingException;
import com.splitbill.paymentservice.exception.ResourceNotFoundException;
import com.splitbill.paymentservice.mapper.PaymentMapper;
import com.splitbill.paymentservice.repository.PaymentRepository;
import com.splitbill.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RazorpayClient razorpayClient;
    private final SettlementClient settlementClient;
    private final RestClient.Builder restClientBuilder;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${services.settlement.url:http://localhost:8085}")
    private String settlementServiceUrl;

    @Override
    @Transactional
    public PaymentOrderResponse createPaymentOrder(CreatePaymentRequest request) {

        if (request.getSettlementId() == null) {
            throw new IllegalArgumentException("Settlement id is required");
        }


        SettlementResponse settlement;

        try {
            settlement = settlementClient.getSettlement(request.getSettlementId());
        } catch (Exception e) {
            throw new PaymentProcessingException("Unable to retrieve settlement");
        }

        if (settlement == null) {
            throw new ResourceNotFoundException("Settlement not found");
        }


        if (!"PENDING".equalsIgnoreCase(settlement.getStatus())) {
            throw new IllegalArgumentException("Settlement is not pending");
        }


        if (!settlement.getFromUserId()
                .equals(request.getUserId())) {

            throw new IllegalArgumentException("Payment user does not match settlement payer");
        }


        if (request.getGroupId() != null && !request.getGroupId()
                .equals(settlement.getGroupId())) {

            throw new IllegalArgumentException("Payment group does not match settlement group");
        }

        BigDecimal settlementAmount = settlement.getAmount();

        if (settlementAmount == null || settlementAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException("Settlement amount must be greater than zero");
        }


        if (request.getAmount() != null && request.getAmount()
                .compareTo(settlementAmount) != 0) {

            throw new IllegalArgumentException("Payment amount does not match settlement amount");
        }


        Payment existingPayment = paymentRepository.findFirstBySettlementIdOrderByCreatedAtDesc(request.getSettlementId())
                .orElse(null);

        if (existingPayment != null && existingPayment.getStatus() == Payment.PaymentStatus.CREATED) {

            return PaymentOrderResponse.builder()
                    .paymentId(existingPayment.getId())
                    .razorpayOrderId(existingPayment.getRazorpayOrderId())
                    .amount(existingPayment.getAmount())
                    .currency(existingPayment.getCurrency())
                    .razorpayKeyId(razorpayKeyId)
                    .build();
        }

        long amountInPaise = settlementAmount.setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", amountInPaise);

        orderRequest.put("currency", "INR");

        orderRequest.put("receipt", "splitbill_" + request.getSettlementId() + "_" + System.currentTimeMillis());

        Order razorpayOrder;

        try {

            razorpayOrder = razorpayClient.orders.create(orderRequest);

        } catch (RazorpayException e) {

            throw new PaymentProcessingException("Failed to create Razorpay order");
        }

        LocalDateTime now = LocalDateTime.now();

        Payment payment = Payment.builder()
                .userId(settlement.getFromUserId())
                .groupId(settlement.getGroupId())
                .settlementId(settlement.getId())
                .razorpayOrderId(razorpayOrder.get("id"))
                .amount(settlementAmount)
                .currency("INR")
                .status(Payment.PaymentStatus.CREATED)
                .description(request.getDescription())
                .createdAt(now)
                .updatedAt(now)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return PaymentOrderResponse.builder()
                .paymentId(savedPayment.getId())
                .razorpayOrderId(savedPayment.getRazorpayOrderId())
                .amount(savedPayment.getAmount())
                .currency(savedPayment.getCurrency())
                .razorpayKeyId(razorpayKeyId)
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + request.getPaymentId()));


        if (payment.getStatus() == Payment.PaymentStatus.PAID) {

            return paymentMapper.toResponse(payment);
        }

        if (!payment.getRazorpayOrderId()
                .equals(request.getRazorpayOrderId())) {

            throw new IllegalArgumentException("Razorpay order id does not match payment record");
        }

        String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();


        try {

            Utils.verifySignature(payload, request.getRazorpaySignature(), razorpayKeySecret);

        } catch (RazorpayException e) {

            throw new PaymentProcessingException("Failed to verify Razorpay payment signature");
        }


        completeSettlement(payment.getSettlementId());

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());

        payment.setRazorpaySignature(request.getRazorpaySignature());

        payment.setStatus(Payment.PaymentStatus.PAID);

        payment.setUpdatedAt(LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(payment);

        return paymentMapper.toResponse(updatedPayment);
    }

    private void completeSettlement(Long settlementId) {

        if (settlementId == null) {
            throw new PaymentProcessingException("Settlement id is missing for payment");
        }

        try {

            restClientBuilder.baseUrl(settlementServiceUrl)
                    .build()
                    .post()
                    .uri("/api/v1/settlements/{id}/complete", settlementId)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {

            throw new PaymentProcessingException("Payment verified, but settlement could not be completed");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUser(Long userId) {

        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByGroup(Long groupId) {

        return paymentRepository.findByGroupIdOrderByCreatedAtDesc(groupId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }
}