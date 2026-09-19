package com.splitbill.balanceservice.service;

import com.splitbill.balanceservice.dto.response.BalanceResponse;

import java.util.List;

public interface BalanceService {

    List<BalanceResponse> getBalancesByGroup(Long groupId);

    List<BalanceResponse> getBalancesByUser(Long userId);
}