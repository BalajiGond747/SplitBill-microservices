package com.splitbill.balanceservice.controller;

import com.splitbill.balanceservice.dto.response.ApiResponse;
import com.splitbill.balanceservice.dto.response.BalanceResponse;
import com.splitbill.balanceservice.service.BalanceService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/balances")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<List<BalanceResponse>>> getBalancesByGroup(@PathVariable @Positive(message = "Group id must be positive") Long groupId) {
        List<BalanceResponse> response = balanceService.getBalancesByGroup(groupId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BalanceResponse>>> getBalancesByUser(@PathVariable @Positive(message = "User id must be positive") Long userId) {
        List<BalanceResponse> response = balanceService.getBalancesByUser(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}