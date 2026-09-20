package com.splitbill.settlementservice.service;

import com.splitbill.settlementservice.dto.request.SettlementCreateRequest;
import com.splitbill.settlementservice.dto.response.SettlementResponse;

import java.util.List;

public interface SettlementService {

    SettlementResponse createSettlement(SettlementCreateRequest request);

    SettlementResponse getSettlementById(Long id);

    List<SettlementResponse> getSettlementsByGroup(Long groupId);

    List<SettlementResponse> getSettlementsByUser(Long userId);
}