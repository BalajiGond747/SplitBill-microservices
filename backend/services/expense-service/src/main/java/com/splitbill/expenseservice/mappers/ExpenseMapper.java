package com.splitbill.expenseservice.mappers;

import com.splitbill.expenseservice.dto.request.ExpenseCreateRequest;
import com.splitbill.expenseservice.dto.request.ExpenseSplitRequest;
import com.splitbill.expenseservice.dto.request.ExpenseUpdateRequest;
import com.splitbill.expenseservice.dto.response.ExpenseResponse;
import com.splitbill.expenseservice.dto.response.ExpenseSplitResponse;
import com.splitbill.expenseservice.entity.Expense;
import com.splitbill.expenseservice.entity.ExpenseSplit;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExpenseMapper {

    public Expense toEntity(ExpenseCreateRequest request) {
        return Expense.builder()
                .groupId(request.getGroupId())
                .paidBy(request.getPaidBy())
                .amount(request.getAmount())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .splitType(request.getSplitType())
                .build();
    }

    public void updateEntity(Expense expense, ExpenseUpdateRequest request) {
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setSplitType(request.getSplitType());
    }

    public ExpenseSplit toSplitEntity(ExpenseSplitRequest request, Long expenseId) {
        return ExpenseSplit.builder()
                .expenseId(expenseId)
                .userId(request.getUserId())
                .amount(request.getAmount())
                .percentage(request.getPercentage())
                .build();
    }

    public ExpenseResponse toResponse(Expense expense, List<ExpenseSplit> splits) {
        ExpenseResponse response = new ExpenseResponse();

        response.setId(expense.getId());
        response.setGroupId(expense.getGroupId());
        response.setPaidBy(expense.getPaidBy());
        response.setAmount(expense.getAmount());
        response.setDescription(expense.getDescription());
        response.setExpenseDate(expense.getExpenseDate());
        response.setSplitType(expense.getSplitType());
        response.setCreatedAt(expense.getCreatedAt());
        response.setUpdatedAt(expense.getUpdatedAt());

        response.setSplits(splits.stream()
                .map(this::toSplitResponse)
                .toList());

        return response;
    }

    public ExpenseSplitResponse toSplitResponse(ExpenseSplit split) {
        return new ExpenseSplitResponse(split.getId(), split.getUserId(), split.getAmount(), split.getPercentage());
    }
}