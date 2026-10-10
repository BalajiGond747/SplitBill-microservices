package com.splitbill.balanceservice.controller;

import com.splitbill.balanceservice.dto.request.BalanceSettlementRequest;
import com.splitbill.balanceservice.dto.response.ApiResponse;
import com.splitbill.balanceservice.dto.response.BalanceResponse;
import com.splitbill.balanceservice.service.BalanceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/balances")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<List<BalanceResponse>>> getBalancesByGroup(@PathVariable @Positive(message = "Group id must be positive") Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(balanceService.getBalancesByGroup(groupId)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BalanceResponse>>> getBalancesByUser(@PathVariable @Positive(message = "User id must be positive") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(balanceService.getBalancesByUser(userId)));
    }

    @GetMapping("/internal/group/{groupId}/from/{fromUserId}/to/{toUserId}")
    public BalanceResponse getBalanceForSettlement(@PathVariable Long groupId, @PathVariable Long fromUserId, @PathVariable Long toUserId) {
        return balanceService.getBalance(groupId, fromUserId, toUserId);
    }

    @PostMapping("/internal/settle")
    public BalanceResponse settleBalance(@Valid @RequestBody BalanceSettlementRequest request) {
        return balanceService.settleBalance(request);
    }
}