package com.splitbill.settlementservice.service;

import com.splitbill.settlementservice.dto.request.SettlementCreateRequest;
import com.splitbill.settlementservice.dto.response.SettlementResponse;

import java.util.List;

public interface SettlementService {

    SettlementResponse createSettlement(SettlementCreateRequest request);

    SettlementResponse getSettlement(Long id);

    List<SettlementResponse> getUserSettlements(Long userId);

    List<SettlementResponse> getGroupSettlements(Long groupId);

    SettlementResponse completeSettlement(Long id);
}