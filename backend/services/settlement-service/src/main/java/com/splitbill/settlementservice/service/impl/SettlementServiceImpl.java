package com.splitbill.settlementservice.service.impl;

import com.splitbill.settlementservice.dto.request.SettlementCreateRequest;
import com.splitbill.settlementservice.dto.response.SettlementResponse;
import com.splitbill.settlementservice.entity.Settlement;
import com.splitbill.settlementservice.exception.ResourceNotFoundException;
import com.splitbill.settlementservice.mapper.SettlementMapper;
import com.splitbill.settlementservice.repository.SettlementRepository;
import com.splitbill.settlementservice.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementMapper settlementMapper;

    @Override
    @Transactional
    public SettlementResponse createSettlement(SettlementCreateRequest request) {

        validateUsers(request);

        Settlement settlement = Settlement.builder()
                .groupId(request.getGroupId())
                .fromUserId(request.getFromUserId())
                .toUserId(request.getToUserId())
                .amount(request.getAmount())
                .settlementDate(LocalDateTime.now())
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Settlement savedSettlement = settlementRepository.save(settlement);

        return settlementMapper.toResponse(savedSettlement);
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementResponse getSettlementById(Long id) {

        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Settlement not found with id: " + id
                        )
                );

        return settlementMapper.toResponse(settlement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlementsByGroup(Long groupId) {

        return settlementRepository
                .findByGroupIdOrderBySettlementDateDesc(groupId)
                .stream()
                .map(settlementMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlementsByUser(Long userId) {

        return settlementRepository
                .findByFromUserIdOrToUserIdOrderBySettlementDateDesc(
                        userId,
                        userId
                )
                .stream()
                .map(settlementMapper::toResponse)
                .toList();
    }

    private void validateUsers(SettlementCreateRequest request) {

        if (request.getFromUserId().equals(request.getToUserId())) {
            throw new IllegalArgumentException(
                    "From user and to user must be different"
            );
        }
    }
}