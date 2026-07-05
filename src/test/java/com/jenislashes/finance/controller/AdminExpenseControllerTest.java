package com.jenislashes.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jenislashes.finance.dto.CreateExpenseRequest;
import com.jenislashes.finance.dto.ExpenseResponse;
import com.jenislashes.finance.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminExpenseControllerTest {

    @Mock
    private ExpenseService expenseService;

    private MockMvc mockMvc;

    private static ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminExpenseController(expenseService))
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper()))
                .build();
    }

    @Test
    void delete_should_return_no_content() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/expenses/00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isNoContent());

        verify(expenseService).deleteExpense(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }

    @Test
    void update_should_return_updated_expense() throws Exception {
        UUID expenseId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID categoryId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        CreateExpenseRequest request = new CreateExpenseRequest(
                categoryId,
                LocalDate.of(2026, 5, 12),
                "Taxi",
                new BigDecimal("350.00"),
                "ida y vuelta"
        );

        when(expenseService.updateExpense(eq(expenseId), eq(request))).thenReturn(new ExpenseResponse(
                expenseId,
                categoryId,
                "Transporte",
                LocalDate.of(2026, 5, 12),
                "Taxi",
                new BigDecimal("350.00"),
                "ida y vuelta",
                OffsetDateTime.parse("2026-05-01T10:00:00Z")
        ));

        mockMvc.perform(put("/api/v1/admin/expenses/{expenseId}", expenseId)
                        .contentType("application/json")
                        .content(objectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId.toString()))
                .andExpect(jsonPath("$.expenseCategoryName").value("Transporte"))
                .andExpect(jsonPath("$.description").value("Taxi"))
                .andExpect(jsonPath("$.amount").value(350.00));

        verify(expenseService).updateExpense(expenseId, request);
    }
}
