package com.splitbill.balanceservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "balances", indexes = {@Index(name = "idx_balances_group_id", columnList = "group_id"), @Index(name = "idx_balances_from_user_id", columnList = "from_user_id"), @Index(name = "idx_balances_to_user_id", columnList = "to_user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;

    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "settled_amount", precision = 19, scale = 2)
    private BigDecimal settledAmount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public BigDecimal getSettledAmountOrZero() {
        return settledAmount == null ? BigDecimal.ZERO : settledAmount;
    }

    public BigDecimal getOutstandingAmount() {

        BigDecimal gross = amount == null ? BigDecimal.ZERO : amount;

        return gross.subtract(getSettledAmountOrZero())
                .max(BigDecimal.ZERO);
    }
}