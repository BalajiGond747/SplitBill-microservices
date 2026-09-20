package com.splitbill.settlementservice.service;

import com.splitbill.settlementservice.dto.request.SettlementCreateRequest;
import com.splitbill.settlementservice.dto.response.SettlementResponse;
import com.splitbill.settlementservice.entity.Settlement;
import com.splitbill.settlementservice.mapper.SettlementMapper;
import com.splitbill.settlementservice.repository.SettlementRepository;
import com.splitbill.settlementservice.service.impl.SettlementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceImplTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private SettlementMapper settlementMapper;

    @InjectMocks
    private SettlementServiceImpl settlementService;

    private SettlementCreateRequest request;

    @BeforeEach
    void setUp() {

        request = SettlementCreateRequest.builder()
                .groupId(1L)
                .fromUserId(2L)
                .toUserId(1L)
                .amount(new BigDecimal("500.00"))
                .note("Paid via cash")
                .build();
    }

    @Test
    void createSettlement_shouldSaveSettlement() {

        Settlement savedSettlement = Settlement.builder()
                .id(1L)
                .groupId(1L)
                .fromUserId(2L)
                .toUserId(1L)
                .amount(new BigDecimal("500.00"))
                .note("Paid via cash")
                .build();

        SettlementResponse response = SettlementResponse.builder()
                .id(1L)
                .groupId(1L)
                .fromUserId(2L)
                .toUserId(1L)
                .amount(new BigDecimal("500.00"))
                .note("Paid via cash")
                .build();

        when(settlementRepository.save(any(Settlement.class)))
                .thenReturn(savedSettlement);

        when(settlementMapper.toResponse(savedSettlement))
                .thenReturn(response);

        SettlementResponse result =
                settlementService.createSettlement(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(
                new BigDecimal("500.00"),
                result.getAmount()
        );

        verify(settlementRepository).save(any(Settlement.class));
        verify(settlementMapper).toResponse(savedSettlement);
    }

    @Test
    void createSettlement_shouldRejectSameUsers() {

        request.setToUserId(request.getFromUserId());

        assertThrows(
                IllegalArgumentException.class,
                () -> settlementService.createSettlement(request)
        );

        verify(settlementRepository, never())
                .save(any(Settlement.class));
    }
}