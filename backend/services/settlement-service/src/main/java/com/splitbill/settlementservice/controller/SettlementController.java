package com.splitbill.settlementservice.controller;

import com.splitbill.settlementservice.dto.request.SettlementCreateRequest;
import com.splitbill.settlementservice.dto.response.SettlementResponse;
import com.splitbill.settlementservice.service.SettlementService;
import jakarta.validation.Valid;
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
    public ResponseEntity<SettlementResponse> createSettlement(@Valid @RequestBody SettlementCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(settlementService.createSettlement(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SettlementResponse> getSettlement(@PathVariable Long id) {
        return ResponseEntity.ok(settlementService.getSettlement(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SettlementResponse>> getUserSettlements(@PathVariable Long userId) {
        return ResponseEntity.ok(settlementService.getUserSettlements(userId));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<SettlementResponse>> getGroupSettlements(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.getGroupSettlements(groupId));
    }


    @PostMapping("/{id}/complete")
    public ResponseEntity<SettlementResponse> completeSettlement(@PathVariable Long id) {
        return ResponseEntity.ok(settlementService.completeSettlement(id));
    }
}