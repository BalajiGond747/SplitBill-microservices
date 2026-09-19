package com.splitbill.balanceservice.mapper;

import com.splitbill.balanceservice.dto.response.BalanceResponse;
import com.splitbill.balanceservice.entity.Balance;
import org.springframework.stereotype.Component;

@Component
public class BalanceMapper {

    public BalanceResponse toResponse(Balance balance) {
        return BalanceResponse.builder()
                .id(balance.getId())
                .groupId(balance.getGroupId())
                .fromUserId(balance.getFromUserId())
                .toUserId(balance.getToUserId())
                .amount(balance.getAmount())
                .build();
    }
}