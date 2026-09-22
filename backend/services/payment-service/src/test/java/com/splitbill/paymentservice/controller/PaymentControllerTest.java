package com.splitbill.paymentservice.controller;

import com.splitbill.paymentservice.dto.response.PaymentResponse;
import com.splitbill.paymentservice.entity.Payment.PaymentStatus;
import com.splitbill.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void getPaymentById_shouldReturn200() throws Exception {

        PaymentResponse response = PaymentResponse.builder()
                .id(1L)
                .userId(2L)
                .amount(new BigDecimal("500.00"))
                .currency("INR")
                .status(PaymentStatus.CREATED)
                .build();

        when(paymentService.getPaymentById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentsByUser_shouldReturn200() throws Exception {

        when(paymentService.getPaymentsByUser(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/payments/user/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentsByGroup_shouldReturn200() throws Exception {

        when(paymentService.getPaymentsByGroup(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/payments/group/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentById_withInvalidId_shouldReturn400() throws Exception {

        mockMvc.perform(get("/api/v1/payments/0"))
                .andExpect(status().isBadRequest());
    }
}