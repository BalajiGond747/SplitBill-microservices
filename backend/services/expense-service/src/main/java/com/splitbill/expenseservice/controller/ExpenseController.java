package com.splitbill.expenseservice.controller;

import com.splitbill.expenseservice.dto.request.ExpenseCreateRequest;
import com.splitbill.expenseservice.dto.request.ExpenseUpdateRequest;
import com.splitbill.expenseservice.dto.response.ApiResponse;
import com.splitbill.expenseservice.dto.response.ExpenseResponse;
import com.splitbill.expenseservice.dto.response.PageResponse;
import com.splitbill.expenseservice.service.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(@Valid @RequestBody ExpenseCreateRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(@PathVariable @Positive(message = "Expense id must be positive") Long id) {
        ExpenseResponse response = expenseService.getExpenseById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<PageResponse<ExpenseResponse>>> getExpensesByGroup(
            @PathVariable @Positive(message = "Group id must be positive") Long groupId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must not be negative") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be at least 1") @Max(value = 100, message = "Size must not exceed 100") int size,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        PageResponse<ExpenseResponse> response = expenseService.getExpensesByGroup(groupId, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(@PathVariable @Positive(message = "Expense id must be positive") Long id,
                                                                      @Valid @RequestBody ExpenseUpdateRequest request) {
        ExpenseResponse response = expenseService.updateExpense(id, request);

        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable @Positive(message = "Expense id must be positive") Long id) {
        expenseService.deleteExpense(id);

        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully", null));
    }
}