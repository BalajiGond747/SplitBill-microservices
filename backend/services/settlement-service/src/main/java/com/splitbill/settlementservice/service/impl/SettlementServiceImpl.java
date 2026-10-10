package com.splitbill.settlementservice.service.impl;

import com.splitbill.settlementservice.client.BalanceClient;
import com.splitbill.settlementservice.dto.request.BalanceSettlementRequest;
import com.splitbill.settlementservice.dto.request.SettlementCreateRequest;
import com.splitbill.settlementservice.dto.response.BalanceResponse;
import com.splitbill.settlementservice.dto.response.SettlementResponse;
import com.splitbill.settlementservice.entity.Settlement;
import com.splitbill.settlementservice.entity.SettlementStatus;
import com.splitbill.settlementservice.mapper.SettlementMapper;
import com.splitbill.settlementservice.repository.SettlementRepository;
import com.splitbill.settlementservice.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementMapper settlementMapper;
    private final BalanceClient balanceClient;

    @Override
    public SettlementResponse createSettlement(SettlementCreateRequest request) {

        if (request.getFromUserId()
                .equals(request.getToUserId())) {

            throw new IllegalArgumentException("A user cannot settle with themselves");
        }

        if (request.getAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException("Settlement amount must be greater than zero");
        }


        BalanceResponse balance = balanceClient.getBalance(request.getGroupId(), request.getFromUserId(), request.getToUserId());

        if (balance == null || balance.getAmount() == null || balance.getAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException("No outstanding balance exists");
        }

        if (request.getAmount()
                .compareTo(balance.getAmount()) > 0) {

            throw new IllegalArgumentException("Settlement amount exceeds outstanding balance");
        }


        Settlement existing = settlementRepository.findFirstByGroupIdAndFromUserIdAndToUserIdAndStatusOrderByIdDesc(request.getGroupId(), request.getFromUserId(), request.getToUserId(), SettlementStatus.PENDING)
                .orElse(null);

        if (existing != null) {

            return settlementMapper.toResponse(existing);
        }

        Settlement settlement = Settlement.builder()
                .groupId(request.getGroupId())
                .fromUserId(request.getFromUserId())
                .toUserId(request.getToUserId())
                .amount(request.getAmount())
                .status(SettlementStatus.PENDING)
                .build();

        Settlement saved = settlementRepository.save(settlement);

        return settlementMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementResponse getSettlement(Long id) {

        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found"));

        return settlementMapper.toResponse(settlement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getUserSettlements(Long userId) {

        return settlementRepository.findByFromUserIdOrToUserIdOrderByCreatedAtDesc(userId, userId)
                .stream()
                .map(settlementMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getGroupSettlements(Long groupId) {

        return settlementRepository.findByGroupIdOrderByCreatedAtDesc(groupId)
                .stream()
                .map(settlementMapper::toResponse)
                .toList();
    }

    @Override
    public SettlementResponse completeSettlement(Long id) {

        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found"));


        if (settlement.getStatus() == SettlementStatus.PAID) {

            return settlementMapper.toResponse(settlement);
        }

        if (settlement.getStatus() != SettlementStatus.PENDING) {

            throw new IllegalStateException("Settlement cannot be completed from status " + settlement.getStatus());
        }

        BalanceResponse currentBalance = balanceClient.getBalance(settlement.getGroupId(), settlement.getFromUserId(), settlement.getToUserId());

        if (currentBalance == null || currentBalance.getAmount() == null || currentBalance.getAmount()
                .compareTo(settlement.getAmount()) < 0) {

            throw new IllegalStateException("Outstanding balance is lower than settlement amount");
        }

        balanceClient.settleBalance(BalanceSettlementRequest.builder()
                .groupId(settlement.getGroupId())
                .fromUserId(settlement.getFromUserId())
                .toUserId(settlement.getToUserId())
                .amount(settlement.getAmount())
                .build());

        settlement.setStatus(SettlementStatus.PAID);

        Settlement saved = settlementRepository.save(settlement);

        return settlementMapper.toResponse(saved);
    }
}