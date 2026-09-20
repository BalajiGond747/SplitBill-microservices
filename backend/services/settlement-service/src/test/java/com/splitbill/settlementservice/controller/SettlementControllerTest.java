package com.splitbill.settlementservice.controller;

import com.splitbill.settlementservice.dto.response.SettlementResponse;
import com.splitbill.settlementservice.service.SettlementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SettlementController.class)
class SettlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SettlementService settlementService;

    @Test
    void getSettlementById_shouldReturn200() throws Exception {

        SettlementResponse response = SettlementResponse.builder()
                .id(1L)
                .groupId(1L)
                .fromUserId(2L)
                .toUserId(1L)
                .amount(new BigDecimal("500.00"))
                .build();

        when(settlementService.getSettlementById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/settlements/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getSettlementsByGroup_shouldReturn200() throws Exception {

        when(settlementService.getSettlementsByGroup(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/settlements/group/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getSettlementsByUser_shouldReturn200() throws Exception {

        when(settlementService.getSettlementsByUser(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/settlements/user/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getSettlementById_withInvalidId_shouldReturn400() throws Exception {

        mockMvc.perform(get("/api/v1/settlements/0"))
                .andExpect(status().isBadRequest());
    }
}