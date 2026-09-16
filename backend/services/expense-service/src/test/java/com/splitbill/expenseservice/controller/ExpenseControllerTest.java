package com.splitbill.expenseservice.controller;

import com.splitbill.expenseservice.dto.request.ExpenseCreateRequest;
import com.splitbill.expenseservice.dto.request.ExpenseSplitRequest;
import com.splitbill.expenseservice.dto.response.ExpenseResponse;
import com.splitbill.expenseservice.dto.response.PageResponse;
import com.splitbill.expenseservice.entity.Expense;
import com.splitbill.expenseservice.exception.ResourceNotFoundException;
import com.splitbill.expenseservice.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    @Test
    void createExpense_shouldReturn201() throws Exception {

        ExpenseCreateRequest request = createValidRequest();

        ExpenseResponse response = new ExpenseResponse();

        response.setId(1L);

        when(expenseService.createExpense(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/expenses").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void createExpense_withInvalidRequest_shouldReturn400() throws Exception {

        ExpenseCreateRequest request = new ExpenseCreateRequest();

        mockMvc.perform(post("/api/v1/expenses").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void getExpenseById_shouldReturn200() throws Exception {

        ExpenseResponse response = new ExpenseResponse();

        response.setId(1L);

        when(expenseService.getExpenseById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/expenses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getExpenseById_whenNotFound_shouldReturn404() throws Exception {

        when(expenseService.getExpenseById(999L)).thenThrow(new ResourceNotFoundException("Expense not found with id: 999"));

        mockMvc.perform(get("/api/v1/expenses/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Expense not found with id: 999"));
    }

    @Test
    void getExpensesByGroup_shouldReturnPage() throws Exception {

        ExpenseResponse response = new ExpenseResponse();

        response.setId(1L);

        PageResponse<ExpenseResponse> page = new PageResponse<>(List.of(response), 0, 10, 1, 1, true, true);

        when(expenseService.getExpensesByGroup(eq(10L), eq(0), eq(10), eq("expenseDate"), eq("DESC"))).thenReturn(page);

        mockMvc.perform(get("/api/v1/expenses/group/10").param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "expenseDate")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    void getExpenses_withInvalidPage_shouldReturn400() throws Exception {

        mockMvc.perform(get("/api/v1/expenses/group/1").param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getExpenses_withInvalidSize_shouldReturn400() throws Exception {

        mockMvc.perform(get("/api/v1/expenses/group/1").param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateExpense_shouldReturn200() throws Exception {

        ExpenseCreateRequest createRequest = createValidRequest();

        com.splitbill.expenseservice.dto.request.ExpenseUpdateRequest updateRequest = new com.splitbill.expenseservice.dto.request.ExpenseUpdateRequest();

        updateRequest.setAmount(createRequest.getAmount());
        updateRequest.setDescription("Updated Dinner");
        updateRequest.setExpenseDate(createRequest.getExpenseDate());
        updateRequest.setSplitType(createRequest.getSplitType());
        updateRequest.setSplits(createRequest.getSplits());

        ExpenseResponse response = new ExpenseResponse();

        response.setId(1L);

        when(expenseService.updateExpense(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/expenses/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void deleteExpense_shouldReturn200() throws Exception {

        mockMvc.perform(delete("/api/v1/expenses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Expense deleted successfully"));
    }

    private ExpenseCreateRequest createValidRequest() {

        ExpenseCreateRequest request = new ExpenseCreateRequest();

        request.setGroupId(10L);
        request.setPaidBy(100L);
        request.setAmount(new BigDecimal("1000.00"));
        request.setDescription("Dinner");
        request.setExpenseDate(LocalDate.of(2026, 9, 16));
        request.setSplitType(Expense.SplitType.EQUAL);

        ExpenseSplitRequest split1 = new ExpenseSplitRequest();

        split1.setUserId(100L);

        ExpenseSplitRequest split2 = new ExpenseSplitRequest();

        split2.setUserId(101L);

        request.setSplits(List.of(split1, split2));

        return request;
    }
}