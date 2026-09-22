package com.splitbill.paymentservice.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.splitbill.paymentservice.dto.request.CreatePaymentRequest;
import com.splitbill.paymentservice.dto.request.VerifyPaymentRequest;
import com.splitbill.paymentservice.dto.response.PaymentOrderResponse;
import com.splitbill.paymentservice.dto.response.PaymentResponse;
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

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Override
    @Transactional
    public PaymentOrderResponse createPaymentOrder(CreatePaymentRequest request) {

        long amountInPaise = request.getAmount()
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "splitbill_" + System.currentTimeMillis());

        Order razorpayOrder;

        try {
            razorpayOrder = razorpayClient.orders.create(orderRequest);
        } catch (RazorpayException e) {
            throw new PaymentProcessingException("Failed to create Razorpay order");
        }

        LocalDateTime now = LocalDateTime.now();

        Payment payment = Payment.builder()
                .userId(request.getUserId())
                .groupId(request.getGroupId())
                .settlementId(request.getSettlementId())
                .razorpayOrderId(razorpayOrder.get("id"))
                .amount(request.getAmount())
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

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());

        payment.setRazorpaySignature(request.getRazorpaySignature());

        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setUpdatedAt(LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(payment);

        return paymentMapper.toResponse(updatedPayment);
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