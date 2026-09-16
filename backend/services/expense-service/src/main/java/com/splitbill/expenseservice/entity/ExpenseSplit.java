package com.splitbill.expenseservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "expense_splits",
        indexes = {
                @Index(name = "idx_expense_splits_expense_id", columnList = "expense_id"),
                @Index(name = "idx_expense_splits_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "expense_id", nullable = false)
    private Long expenseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(precision = 7, scale = 4)
    private BigDecimal percentage;
}