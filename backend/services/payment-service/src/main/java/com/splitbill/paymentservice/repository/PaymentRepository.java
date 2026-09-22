package com.splitbill.paymentservice.repository;

import com.splitbill.paymentservice.entity.Payment;
import com.splitbill.paymentservice.entity.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Payment> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);
}