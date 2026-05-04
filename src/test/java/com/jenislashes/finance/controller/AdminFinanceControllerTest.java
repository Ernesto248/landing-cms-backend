package com.jenislashes.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jenislashes.finance.dto.DailyFinanceEntry;
import com.jenislashes.finance.dto.FinanceHistoryMonthResponse;
import com.jenislashes.finance.dto.FinanceHistoryResponse;
import com.jenislashes.finance.dto.RangeFinanceResponse;
import com.jenislashes.finance.service.FinanceSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminFinanceControllerTest {

    @Mock
    private FinanceSummaryService financeSummaryService;

    private MockMvc mockMvc;

    private static ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminFinanceController(financeSummaryService))
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper()))
                .build();
    }

    @Test
    void history_should_return_finance_history_payload() throws Exception {
        when(financeSummaryService.getFinanceHistory(6)).thenReturn(new FinanceHistoryResponse(List.of(
                new FinanceHistoryMonthResponse(2026, 5, new BigDecimal("3600.00"), new BigDecimal("800.00")),
                new FinanceHistoryMonthResponse(2026, 4, new BigDecimal("2400.00"), new BigDecimal("500.00"))
        )));

        mockMvc.perform(get("/api/v1/admin/finance/history").queryParam("months", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.months.length()").value(2))
                .andExpect(jsonPath("$.months[0].year").value(2026))
                .andExpect(jsonPath("$.months[0].month").value(5))
                .andExpect(jsonPath("$.months[0].income").value(3600.00))
                .andExpect(jsonPath("$.months[0].expenses").value(800.00));
    }

    @Test
    void rangeSummary_should_return_range_finance_payload() throws Exception {
        when(financeSummaryService.getRangeSummary(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3)))
                .thenReturn(new RangeFinanceResponse(
                        new BigDecimal("4500.00"),
                        new BigDecimal("900.00"),
                        new BigDecimal("3600.00"),
                        List.of(),
                        List.of(
                                new DailyFinanceEntry(LocalDate.of(2026, 5, 1), new BigDecimal("1500.00"), new BigDecimal("300.00")),
                                new DailyFinanceEntry(LocalDate.of(2026, 5, 2), new BigDecimal("1800.00"), new BigDecimal("400.00")),
                                new DailyFinanceEntry(LocalDate.of(2026, 5, 3), new BigDecimal("1200.00"), new BigDecimal("200.00"))
                        )
                ));

        mockMvc.perform(get("/api/v1/admin/finance/range-summary")
                        .queryParam("from", "2026-05-01")
                        .queryParam("to", "2026-05-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedIncome").value(4500.00))
                .andExpect(jsonPath("$.recordedExpenses").value(900.00))
                .andExpect(jsonPath("$.balance").value(3600.00))
                .andExpect(jsonPath("$.days.length()").value(3))
                .andExpect(jsonPath("$.days[0].date").value("2026-05-01"))
                .andExpect(jsonPath("$.days[0].income").value(1500.00))
                .andExpect(jsonPath("$.days[0].expenses").value(300.00));
    }
}
