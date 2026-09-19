package com.splitbill.balanceservice.service.impl;

import com.splitbill.balanceservice.entity.Balance;
import com.splitbill.balanceservice.event.ExpenseEvent;
import com.splitbill.balanceservice.event.ExpenseSplitEvent;
import com.splitbill.balanceservice.mapper.BalanceMapper;
import com.splitbill.balanceservice.repository.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceImplTest {

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private BalanceMapper balanceMapper;

    @InjectMocks
    private BalanceServiceImpl balanceService;

    private ExpenseEvent createEvent;

    @BeforeEach
    void setUp() {

        ExpenseSplitEvent split1 = new ExpenseSplitEvent(1L, new BigDecimal("500.00"), null);

        ExpenseSplitEvent split2 = new ExpenseSplitEvent(2L, new BigDecimal("500.00"), null);

        createEvent = new ExpenseEvent(ExpenseEvent.EventType.EXPENSE_CREATED, 100L, 10L, 1L, new BigDecimal("1000.00"), ExpenseEvent.SplitType.EQUAL, List.of(split1, split2), null);
    }

    @Test
    void shouldCreateBalancesForExpense() {

        when(balanceRepository.findByGroupIdAndFromUserIdAndToUserId(10L, 2L, 1L)).thenReturn(Optional.empty());

        balanceService.handleExpenseCreated(createEvent);

        verify(balanceRepository).save(any(Balance.class));
    }

    @Test
    void shouldNotCreateBalanceForPayer() {

        ExpenseSplitEvent payerSplit = new ExpenseSplitEvent(1L, new BigDecimal("1000.00"), null);

        ExpenseEvent event = new ExpenseEvent(ExpenseEvent.EventType.EXPENSE_CREATED, 101L, 10L, 1L, new BigDecimal("1000.00"), ExpenseEvent.SplitType.EQUAL, List.of(payerSplit), null);

        balanceService.handleExpenseCreated(event);

        verify(balanceRepository, never()).save(any(Balance.class));
    }

    @Test
    void shouldUpdateExistingBalance() {

        Balance existingBalance = Balance.builder()
                .id(1L)
                .groupId(10L)
                .fromUserId(2L)
                .toUserId(1L)
                .amount(new BigDecimal("200.00"))
                .build();

        when(balanceRepository.findByGroupIdAndFromUserIdAndToUserId(10L, 2L, 1L)).thenReturn(Optional.of(existingBalance));

        balanceService.handleExpenseCreated(createEvent);

        verify(balanceRepository).save(existingBalance);

        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("700.00"), existingBalance.getAmount());
    }

    @Test
    void shouldDeleteBalanceWhenAmountBecomesZero() {

        Balance existingBalance = Balance.builder()
                .id(1L)
                .groupId(10L)
                .fromUserId(2L)
                .toUserId(1L)
                .amount(new BigDecimal("500.00"))
                .build();

        when(balanceRepository.findByGroupIdAndFromUserIdAndToUserId(10L, 2L, 1L)).thenReturn(Optional.of(existingBalance));

        balanceService.handleExpenseDeleted(createEvent);

        verify(balanceRepository).delete(existingBalance);
    }
}