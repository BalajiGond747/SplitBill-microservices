package com.splitbill.paymentservice.service;

import com.razorpay.RazorpayClient;
import com.splitbill.paymentservice.dto.response.PaymentResponse;
import com.splitbill.paymentservice.entity.Payment;
import com.splitbill.paymentservice.mapper.PaymentMapper;
import com.splitbill.paymentservice.repository.PaymentRepository;
import com.splitbill.paymentservice.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private RazorpayClient razorpayClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {

        payment = Payment.builder()
                .id(1L)
                .userId(2L)
                .groupId(1L)
                .settlementId(1L)
                .razorpayOrderId("order_test123")
                .amount(new BigDecimal("500.00"))
                .currency("INR")
                .status(Payment.PaymentStatus.CREATED)
                .description("Test payment")
                .build();
    }

    @Test
    void getPaymentById_shouldReturnPayment() {

        PaymentResponse response = PaymentResponse.builder()
                .id(1L)
                .userId(2L)
                .groupId(1L)
                .settlementId(1L)
                .razorpayOrderId("order_test123")
                .amount(new BigDecimal("500.00"))
                .currency("INR")
                .status(Payment.PaymentStatus.CREATED)
                .description("Test payment")
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        when(paymentMapper.toResponse(payment)).thenReturn(response);

        PaymentResponse result = paymentService.getPaymentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2L, result.getUserId());
        assertEquals(new BigDecimal("500.00"), result.getAmount());
        assertEquals(Payment.PaymentStatus.CREATED, result.getStatus());

        verify(paymentRepository).findById(1L);
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    void getPaymentById_whenPaymentDoesNotExist_shouldThrowException() {

        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.getPaymentById(999L));

        verify(paymentRepository).findById(999L);
        verifyNoInteractions(paymentMapper);
    }
}