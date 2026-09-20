package com.splitbill.settlementservice.controller;

import com.splitbill.settlementservice.dto.request.SettlementCreateRequest;
import com.splitbill.settlementservice.dto.response.ApiResponse;
import com.splitbill.settlementservice.dto.response.SettlementResponse;
import com.splitbill.settlementservice.service.SettlementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping
    public ResponseEntity<ApiResponse<SettlementResponse>> createSettlement(@Valid @RequestBody SettlementCreateRequest request) {

        SettlementResponse response = settlementService.createSettlement(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Settlement created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SettlementResponse>> getSettlementById(@PathVariable @Positive(message = "Settlement id must be positive") Long id) {

        return ResponseEntity.ok(ApiResponse.success(settlementService.getSettlementById(id)));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getSettlementsByGroup(@PathVariable @Positive(message = "Group id must be positive") Long groupId) {

        return ResponseEntity.ok(ApiResponse.success(settlementService.getSettlementsByGroup(groupId)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getSettlementsByUser(@PathVariable @Positive(message = "User id must be positive") Long userId) {

        return ResponseEntity.ok(ApiResponse.success(settlementService.getSettlementsByUser(userId)));
    }
}