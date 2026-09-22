package com.splitbill.paymentservice.controller;

import com.splitbill.paymentservice.dto.request.CreatePaymentRequest;
import com.splitbill.paymentservice.dto.request.VerifyPaymentRequest;
import com.splitbill.paymentservice.dto.response.ApiResponse;
import com.splitbill.paymentservice.dto.response.PaymentOrderResponse;
import com.splitbill.paymentservice.dto.response.PaymentResponse;
import com.splitbill.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(@Valid @RequestBody CreatePaymentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment order created successfully", paymentService.createPaymentOrder(request)));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", paymentService.verifyPayment(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable @Positive(message = "Payment id must be positive") Long id) {

        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentById(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByUser(@PathVariable @Positive(message = "User id must be positive") Long userId) {

        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsByUser(userId)));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByGroup(@PathVariable @Positive(message = "Group id must be positive") Long groupId) {

        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsByGroup(groupId)));
    }
}