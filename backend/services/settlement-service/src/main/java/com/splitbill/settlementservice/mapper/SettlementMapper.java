package com.splitbill.settlementservice.mapper;

import com.splitbill.settlementservice.dto.response.SettlementResponse;
import com.splitbill.settlementservice.entity.Settlement;
import org.springframework.stereotype.Component;

@Component
public class SettlementMapper {

    public SettlementResponse toResponse(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .groupId(settlement.getGroupId())
                .fromUserId(settlement.getFromUserId())
                .toUserId(settlement.getToUserId())
                .amount(settlement.getAmount())
                .settlementDate(settlement.getSettlementDate())
                .note(settlement.getNote())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .build();
    }
}