package com.splitbill.balanceservice.service.impl;

import com.splitbill.balanceservice.dto.request.BalanceSettlementRequest;
import com.splitbill.balanceservice.dto.response.BalanceResponse;
import com.splitbill.balanceservice.entity.Balance;
import com.splitbill.balanceservice.event.ExpenseEvent;
import com.splitbill.balanceservice.event.ExpenseSplitEvent;
import com.splitbill.balanceservice.mapper.BalanceMapper;
import com.splitbill.balanceservice.repository.BalanceRepository;
import com.splitbill.balanceservice.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BalanceServiceImpl implements BalanceService {

    private final BalanceRepository balanceRepository;
    private final BalanceMapper balanceMapper;

    @Override
    @Cacheable(value = "groupBalances", key = "#groupId")
    public List<BalanceResponse> getBalancesByGroup(Long groupId) {
        return balanceRepository.findByGroupId(groupId)
                .stream()
                .map(this::toOutstandingResponse)
                .filter(balance -> balance.getAmount()
                        .compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }

    @Override
    @Cacheable(value = "userBalances", key = "#userId")
    public List<BalanceResponse> getBalancesByUser(Long userId) {
        List<Balance> balances = new ArrayList<>();

        balances.addAll(balanceRepository.findByFromUserId(userId));

        balances.addAll(balanceRepository.findByToUserId(userId));

        return balances.stream()
                .map(this::toOutstandingResponse)
                .filter(balance -> balance.getAmount()
                        .compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }

    @Override
    public BalanceResponse getBalance(Long groupId, Long fromUserId, Long toUserId) {

        Balance balance = balanceRepository.findByGroupIdAndFromUserIdAndToUserId(groupId, fromUserId, toUserId)
                .orElseThrow(() -> new IllegalArgumentException("Outstanding balance not found"));

        BalanceResponse response = toOutstandingResponse(balance);

        if (response.getAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException("No outstanding balance exists");
        }

        return response;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"groupBalances", "userBalances"}, allEntries = true)
    public BalanceResponse settleBalance(BalanceSettlementRequest request) {

        if (request.getFromUserId()
                .equals(request.getToUserId())) {

            throw new IllegalArgumentException("A user cannot settle with themselves");
        }

        if (request.getAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException("Settlement amount must be greater than zero");
        }

        Balance balance = balanceRepository.findByGroupIdAndFromUserIdAndToUserId(request.getGroupId(), request.getFromUserId(), request.getToUserId())
                .orElseThrow(() -> new IllegalArgumentException("Outstanding balance not found"));

        BigDecimal outstanding = balance.getOutstandingAmount();

        if (request.getAmount()
                .compareTo(outstanding) > 0) {

            throw new IllegalArgumentException("Settlement amount cannot exceed outstanding balance");
        }

        BigDecimal currentSettled = balance.getSettledAmountOrZero();

        balance.setSettledAmount(currentSettled.add(request.getAmount()));

        Balance saved = balanceRepository.save(balance);

        return toOutstandingResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"groupBalances", "userBalances"}, allEntries = true)
    public void processExpense(ExpenseEvent event) {

        if (event == null) {
            return;
        }

        List<ExpenseSplitEvent> splits = event.getSplits() == null ? new ArrayList<>() : event.getSplits();

        for (ExpenseSplitEvent split : splits) {

            if (split == null) {
                continue;
            }

            Long fromUserId = split.getUserId();
            Long toUserId = event.getPaidBy();

            if (fromUserId == null || toUserId == null || fromUserId.equals(toUserId)) {
                continue;
            }

            BigDecimal splitAmount = split.getAmount();

            if (splitAmount == null || splitAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            Balance balance = balanceRepository.findByGroupIdAndFromUserIdAndToUserId(event.getGroupId(), fromUserId, toUserId)
                    .orElseGet(() -> Balance.builder()
                            .groupId(event.getGroupId())
                            .fromUserId(fromUserId)
                            .toUserId(toUserId)
                            .amount(BigDecimal.ZERO)
                            .settledAmount(BigDecimal.ZERO)
                            .build());

            balance.setAmount(balance.getAmount()
                    .add(splitAmount));

            balanceRepository.save(balance);
        }
    }

    private BalanceResponse toOutstandingResponse(Balance balance) {

        BalanceResponse response = balanceMapper.toResponse(balance);

        response.setAmount(balance.getOutstandingAmount());

        return response;
    }

    @Transactional
    public void handleExpenseCreated(ExpenseEvent event) {
        processExpense(event);
    }

    @Transactional
    public void handleExpenseUpdated(ExpenseEvent event) {
        processExpense(event);
    }

    public void handleExpenseDeleted(ExpenseEvent event) {

    }
}