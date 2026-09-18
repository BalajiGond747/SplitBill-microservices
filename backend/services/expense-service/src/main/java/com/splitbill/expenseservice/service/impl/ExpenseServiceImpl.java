package com.splitbill.expenseservice.service.impl;

import com.splitbill.expenseservice.dto.request.ExpenseCreateRequest;
import com.splitbill.expenseservice.dto.request.ExpenseSplitRequest;
import com.splitbill.expenseservice.dto.request.ExpenseUpdateRequest;
import com.splitbill.expenseservice.dto.response.ExpenseResponse;
import com.splitbill.expenseservice.dto.response.PageResponse;
import com.splitbill.expenseservice.entity.Expense;
import com.splitbill.expenseservice.entity.ExpenseSplit;
import com.splitbill.expenseservice.event.ExpenseEvent;
import com.splitbill.expenseservice.event.ExpenseSplitEvent;
import com.splitbill.expenseservice.exception.ResourceNotFoundException;
import com.splitbill.expenseservice.mappers.ExpenseMapper;
import com.splitbill.expenseservice.messaging.ExpenseEventProducer;
import com.splitbill.expenseservice.repository.ExpenseRepository;
import com.splitbill.expenseservice.repository.ExpenseSplitRepository;
import com.splitbill.expenseservice.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final ExpenseMapper expenseMapper;
    private final ExpenseEventProducer expenseEventProducer;

    @Override
    @Transactional
    public ExpenseResponse createExpense(ExpenseCreateRequest request) {

        validateSplits(request.getAmount(), request.getSplitType(), request.getSplits());

        Expense expense = expenseMapper.toEntity(request);
        Expense savedExpense = expenseRepository.save(expense);
        saveSplits(savedExpense.getId(), request.getSplits());

        expenseEventProducer.publish(buildExpenseEvent(ExpenseEvent.EventType.EXPENSE_CREATED, savedExpense));

        return buildResponse(savedExpense);
    }

    @Override
    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = findExpenseById(id);
        return buildResponse(expense);
    }

    @Override
    public PageResponse<ExpenseResponse> getExpensesByGroup(Long groupId, int page, int size, String sortBy, String sortDirection) {
        Pageable pageable = createPageable(page, size, sortBy, sortDirection);

        Page<Expense> expensePage = expenseRepository.findByGroupId(groupId, pageable);

        return toPageResponse(expensePage);
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseUpdateRequest request) {
        validateSplits(request.getAmount(), request.getSplitType(), request.getSplits());

        Expense expense = findExpenseById(id);

        expenseMapper.updateEntity(expense, request);

        Expense updatedExpense = expenseRepository.save(expense);
        expenseSplitRepository.deleteByExpenseId(id);
        saveSplits(id, request.getSplits());

        expenseEventProducer.publish(buildExpenseEvent(ExpenseEvent.EventType.EXPENSE_UPDATED, updatedExpense));

        return buildResponse(updatedExpense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        Expense expense = findExpenseById(id);

        expenseEventProducer.publish(buildExpenseEvent(ExpenseEvent.EventType.EXPENSE_DELETED, expense));

        expenseRepository.delete(expense);
    }

    private Expense findExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    private void saveSplits(Long expenseId, List<ExpenseSplitRequest> splitRequests) {
        List<ExpenseSplit> splits = splitRequests.stream()
                .map(request -> expenseMapper.toSplitEntity(request, expenseId))
                .toList();

        expenseSplitRepository.saveAll(splits);
    }

    private ExpenseResponse buildResponse(Expense expense) {
        List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseId(expense.getId());

        return expenseMapper.toResponse(expense, splits);
    }

    private void validateSplits(BigDecimal expenseAmount, Expense.SplitType splitType, List<ExpenseSplitRequest> splits) {
        if (splits == null || splits.isEmpty()) {
            throw new IllegalArgumentException("At least one split is required");
        }

        if (splitType == Expense.SplitType.EXACT) {

            BigDecimal splitTotal = splits.stream()
                    .map(ExpenseSplitRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal expected = expenseAmount.setScale(2, RoundingMode.HALF_UP);

            if (splitTotal.compareTo(expected) != 0) {
                throw new IllegalArgumentException("Exact split amounts must equal the expense amount");
            }
        }

        if (splitType == Expense.SplitType.PERCENTAGE) {

            BigDecimal percentageTotal = splits.stream()
                    .map(ExpenseSplitRequest::getPercentage)
                    .filter(value -> value != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (percentageTotal.compareTo(new BigDecimal("100")) != 0) {
                throw new IllegalArgumentException("Percentage splits must total 100");
            }

            for (ExpenseSplitRequest split : splits) {

                BigDecimal splitAmount = expenseAmount.multiply(split.getPercentage())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                split.setAmount(splitAmount);
            }
        }

        if (splitType == Expense.SplitType.EQUAL) {

            BigDecimal expectedPerPerson = expenseAmount.divide(BigDecimal.valueOf(splits.size()), 2, RoundingMode.DOWN);

            BigDecimal allocatedAmount = BigDecimal.ZERO;

            for (int i = 0; i < splits.size(); i++) {

                ExpenseSplitRequest split = splits.get(i);

                BigDecimal splitAmount;

                if (i == splits.size() - 1) {
                    splitAmount = expenseAmount.subtract(allocatedAmount);
                } else {
                    splitAmount = expectedPerPerson;
                    allocatedAmount = allocatedAmount.add(splitAmount);
                }

                split.setAmount(splitAmount);
            }
        }
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);

        Sort sort = Sort.by(direction, sortBy);

        return PageRequest.of(page, size, sort);
    }

    private PageResponse<ExpenseResponse> toPageResponse(Page<Expense> expensePage) {
        List<ExpenseResponse> content = expensePage.getContent()
                .stream()
                .map(this::buildResponse)
                .toList();

        return new PageResponse<>(content, expensePage.getNumber(), expensePage.getSize(), expensePage.getTotalElements(), expensePage.getTotalPages(), expensePage.isFirst(), expensePage.isLast());
    }

    private ExpenseEvent buildExpenseEvent(ExpenseEvent.EventType eventType, Expense expense) {
        List<ExpenseSplitEvent> splitEvents = expenseSplitRepository.findByExpenseId(expense.getId())
                .stream()
                .map(split -> ExpenseSplitEvent.builder()
                        .userId(split.getUserId())
                        .amount(split.getAmount())
                        .percentage(split.getPercentage())
                        .build())
                .toList();

        return ExpenseEvent.builder()
                .eventType(eventType)
                .expenseId(expense.getId())
                .groupId(expense.getGroupId())
                .paidBy(expense.getPaidBy())
                .amount(expense.getAmount())
                .splitType(expense.getSplitType())
                .splits(splitEvents)
                .build();
    }
}