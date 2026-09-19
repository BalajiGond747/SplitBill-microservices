package com.splitbill.balanceservice.service.impl;

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
                .map(balanceMapper::toResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "userBalances", key = "#userId")
    public List<BalanceResponse> getBalancesByUser(Long userId) {

        List<Balance> balances = new ArrayList<>();

        balances.addAll(balanceRepository.findByFromUserId(userId));

        balances.addAll(balanceRepository.findByToUserId(userId));

        return balances.stream()
                .map(balanceMapper::toResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = {"groupBalances", "userBalances"}, allEntries = true)
    public void handleExpenseCreated(ExpenseEvent event) {
        createBalancesForExpense(event);
    }

    @Transactional
    @CacheEvict(value = {"groupBalances", "userBalances"}, allEntries = true)
    public void handleExpenseUpdated(ExpenseEvent event) {
        removeBalances(event.getGroupId(), event.getPaidBy(), event.getPreviousSplits());

        createBalancesForExpense(event);
    }

    @Transactional
    @CacheEvict(value = {"groupBalances", "userBalances"}, allEntries = true)
    public void handleExpenseDeleted(ExpenseEvent event) {
        removeBalances(event.getGroupId(), event.getPaidBy(), event.getSplits());
    }

    private void createBalancesForExpense(ExpenseEvent event) {

        for (ExpenseSplitEvent split : event.getSplits()) {

            if (split.getUserId()
                    .equals(event.getPaidBy())) {
                continue;
            }

            if (split.getAmount() == null || split.getAmount()
                    .compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            Balance balance = balanceRepository.findByGroupIdAndFromUserIdAndToUserId(event.getGroupId(), split.getUserId(), event.getPaidBy())
                    .orElseGet(() -> Balance.builder()
                            .groupId(event.getGroupId())
                            .fromUserId(split.getUserId())
                            .toUserId(event.getPaidBy())
                            .amount(BigDecimal.ZERO)
                            .build());

            balance.setAmount(balance.getAmount()
                    .add(split.getAmount()));

            balanceRepository.save(balance);
        }
    }

    private void removeBalances(Long groupId, Long paidBy, List<ExpenseSplitEvent> splits) {

        if (splits == null) {
            return;
        }

        for (ExpenseSplitEvent split : splits) {

            if (split.getUserId()
                    .equals(paidBy)) {
                continue;
            }

            if (split.getAmount() == null || split.getAmount()
                    .compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            balanceRepository.findByGroupIdAndFromUserIdAndToUserId(groupId, split.getUserId(), paidBy)
                    .ifPresent(balance -> {

                        BigDecimal updatedAmount = balance.getAmount()
                                .subtract(split.getAmount());

                        if (updatedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                            balanceRepository.delete(balance);
                        } else {
                            balance.setAmount(updatedAmount);
                            balanceRepository.save(balance);
                        }
                    });
        }
    }
}