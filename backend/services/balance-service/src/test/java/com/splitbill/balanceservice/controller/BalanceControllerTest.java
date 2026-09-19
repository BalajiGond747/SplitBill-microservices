package com.splitbill.balanceservice.controller;

import com.splitbill.balanceservice.dto.response.BalanceResponse;
import com.splitbill.balanceservice.service.BalanceService;
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

@WebMvcTest(BalanceController.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BalanceService balanceService;

    @Test
    void shouldGetBalancesByGroup() throws Exception {

        BalanceResponse response = BalanceResponse.builder()
                .id(1L)
                .groupId(10L)
                .fromUserId(2L)
                .toUserId(1L)
                .amount(new BigDecimal("500.00"))
                .build();

        when(balanceService.getBalancesByGroup(10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/balances/group/10"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetBalancesByUser() throws Exception {

        BalanceResponse response = BalanceResponse.builder()
                .id(1L)
                .groupId(10L)
                .fromUserId(2L)
                .toUserId(1L)
                .amount(new BigDecimal("500.00"))
                .build();

        when(balanceService.getBalancesByUser(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/balances/user/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectInvalidGroupId() throws Exception {

        mockMvc.perform(get("/api/v1/balances/group/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidUserId() throws Exception {

        mockMvc.perform(get("/api/v1/balances/user/0"))
                .andExpect(status().isBadRequest());
    }
}