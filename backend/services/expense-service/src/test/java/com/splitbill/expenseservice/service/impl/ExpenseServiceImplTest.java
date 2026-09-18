package com.splitbill.expenseservice.service.impl;

import com.splitbill.expenseservice.dto.request.ExpenseCreateRequest;
import com.splitbill.expenseservice.dto.request.ExpenseSplitRequest;
import com.splitbill.expenseservice.dto.request.ExpenseUpdateRequest;
import com.splitbill.expenseservice.dto.response.ExpenseResponse;
import com.splitbill.expenseservice.entity.Expense;
import com.splitbill.expenseservice.entity.ExpenseSplit;
import com.splitbill.expenseservice.exception.ResourceNotFoundException;
import com.splitbill.expenseservice.mappers.ExpenseMapper;
import com.splitbill.expenseservice.messaging.ExpenseEventProducer;
import com.splitbill.expenseservice.repository.ExpenseRepository;
import com.splitbill.expenseservice.repository.ExpenseSplitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import com.splitbill.expenseservice.messaging.ExpenseEventProducer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseSplitRepository expenseSplitRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Mock
    private ExpenseEventProducer expenseEventProducer;

    private Expense expense;
    private ExpenseResponse expenseResponse;

    @BeforeEach
    void setUp() {
        expense = Expense.builder()
                .id(1L)
                .groupId(10L)
                .paidBy(100L)
                .amount(new BigDecimal("1000.00"))
                .description("Dinner")
                .expenseDate(LocalDate.of(2026, 9, 16))
                .splitType(Expense.SplitType.EQUAL)
                .build();

        expenseResponse = new ExpenseResponse();
        expenseResponse.setId(1L);
        expenseResponse.setGroupId(10L);
        expenseResponse.setPaidBy(100L);
        expenseResponse.setAmount(new BigDecimal("1000.00"));
    }

    @Test
    void createExpense_shouldCreateExpense() {

        ExpenseCreateRequest request = new ExpenseCreateRequest();
        request.setGroupId(10L);
        request.setPaidBy(100L);
        request.setAmount(new BigDecimal("1000.00"));
        request.setDescription("Dinner");
        request.setExpenseDate(LocalDate.of(2026, 9, 16));
        request.setSplitType(Expense.SplitType.EQUAL);

        ExpenseSplitRequest split1 = new ExpenseSplitRequest();
        split1.setUserId(100L);

        ExpenseSplitRequest split2 = new ExpenseSplitRequest();
        split2.setUserId(101L);

        request.setSplits(List.of(split1, split2));

        when(expenseMapper.toEntity(request)).thenReturn(expense);
        when(expenseRepository.save(expense)).thenReturn(expense);
        when(expenseMapper.toSplitEntity(any(), eq(1L))).thenReturn(new ExpenseSplit());
        when(expenseSplitRepository.saveAll(anyList())).thenReturn(List.of());
        when(expenseSplitRepository.findByExpenseId(1L)).thenReturn(List.of());
        when(expenseMapper.toResponse(eq(expense), anyList())).thenReturn(expenseResponse);

        ExpenseResponse result = expenseService.createExpense(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(expenseRepository).save(expense);
        verify(expenseSplitRepository).saveAll(anyList());
    }

    @Test
    void getExpenseById_shouldReturnExpense() {

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        when(expenseSplitRepository.findByExpenseId(1L)).thenReturn(List.of());

        when(expenseMapper.toResponse(eq(expense), anyList())).thenReturn(expenseResponse);

        ExpenseResponse result = expenseService.getExpenseById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getExpenseById_shouldThrowWhenNotFound() {

        when(expenseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> expenseService.getExpenseById(1L));
    }

    @Test
    void getExpensesByGroup_shouldReturnPage() {

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "expenseDate"));

        Page<Expense> page = new PageImpl<>(List.of(expense), pageable, 1);

        when(expenseRepository.findByGroupId(10L, pageable)).thenReturn(page);

        when(expenseSplitRepository.findByExpenseId(1L)).thenReturn(List.of());

        when(expenseMapper.toResponse(eq(expense), anyList())).thenReturn(expenseResponse);

        var result = expenseService.getExpensesByGroup(10L, 0, 10, "expenseDate", "DESC");

        assertEquals(1, result.getContent()
                .size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateExpense_shouldUpdateExpense() {

        ExpenseUpdateRequest request = new ExpenseUpdateRequest();

        request.setAmount(new BigDecimal("1200.00"));
        request.setDescription("Updated Dinner");
        request.setExpenseDate(LocalDate.of(2026, 9, 16));
        request.setSplitType(Expense.SplitType.EQUAL);

        ExpenseSplitRequest split = new ExpenseSplitRequest();

        split.setUserId(100L);

        request.setSplits(List.of(split));

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        when(expenseRepository.save(expense)).thenReturn(expense);

        when(expenseMapper.toSplitEntity(any(), eq(1L))).thenReturn(new ExpenseSplit());

        when(expenseSplitRepository.saveAll(anyList())).thenReturn(List.of());

        when(expenseSplitRepository.findByExpenseId(1L)).thenReturn(List.of());

        when(expenseMapper.toResponse(eq(expense), anyList())).thenReturn(expenseResponse);

        ExpenseResponse result = expenseService.updateExpense(1L, request);

        assertNotNull(result);

        verify(expenseRepository).save(expense);
        verify(expenseSplitRepository).deleteByExpenseId(1L);
    }

    @Test
    void deleteExpense_shouldDeleteExpense() {

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(1L);

        verify(expenseRepository).delete(expense);
    }

    @Test
    void deleteExpense_shouldThrowWhenNotFound() {

        when(expenseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> expenseService.deleteExpense(1L));
    }

    @Test
    void exactSplit_shouldRejectIncorrectTotal() {

        ExpenseCreateRequest request = new ExpenseCreateRequest();

        request.setAmount(new BigDecimal("1000.00"));
        request.setSplitType(Expense.SplitType.EXACT);

        ExpenseSplitRequest split = new ExpenseSplitRequest();

        split.setUserId(100L);
        split.setAmount(new BigDecimal("500.00"));

        request.setSplits(List.of(split));

        assertThrows(IllegalArgumentException.class, () -> expenseService.createExpense(request));

        verify(expenseRepository, never()).save(any());
    }

    @Test
    void percentageSplit_shouldRejectIncorrectTotal() {

        ExpenseCreateRequest request = new ExpenseCreateRequest();

        request.setAmount(new BigDecimal("1000.00"));
        request.setSplitType(Expense.SplitType.PERCENTAGE);

        ExpenseSplitRequest split = new ExpenseSplitRequest();

        split.setUserId(100L);
        split.setAmount(new BigDecimal("500.00"));
        split.setPercentage(new BigDecimal("50"));

        request.setSplits(List.of(split));

        assertThrows(IllegalArgumentException.class, () -> expenseService.createExpense(request));

        verify(expenseRepository, never()).save(any());
    }
}