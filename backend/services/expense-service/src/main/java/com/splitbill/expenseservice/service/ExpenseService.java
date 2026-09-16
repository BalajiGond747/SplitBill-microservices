package com.splitbill.expenseservice.service;

import com.splitbill.expenseservice.dto.request.ExpenseCreateRequest;
import com.splitbill.expenseservice.dto.request.ExpenseUpdateRequest;
import com.splitbill.expenseservice.dto.response.ExpenseResponse;
import com.splitbill.expenseservice.dto.response.PageResponse;

public interface ExpenseService {

    ExpenseResponse createExpense(ExpenseCreateRequest request);

    ExpenseResponse getExpenseById(Long id);

    PageResponse<ExpenseResponse> getExpensesByGroup(Long groupId, int page, int size, String sortBy, String sortDirection);

    ExpenseResponse updateExpense(Long id, ExpenseUpdateRequest request);

    void deleteExpense(Long id);
}