package com.jenislashes.finance.service;

import com.jenislashes.common.exception.BadRequestException;
import com.jenislashes.finance.dto.DailyFinanceEntry;
import com.jenislashes.finance.dto.FinanceHistoryResponse;
import com.jenislashes.finance.dto.MonthlyFinanceSummaryResponse;
import com.jenislashes.finance.dto.RangeFinanceResponse;
import com.jenislashes.finance.model.ExpenseRecord;
import com.jenislashes.finance.repository.ExpenseRepository;
import com.jenislashes.finance.repository.FinanceSummaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinanceSummaryService")
class FinanceSummaryServiceTest {

    private static final ZoneId HAVANA = ZoneId.of("America/Havana");

    @Mock
    private FinanceSummaryRepository financeSummaryRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private FinanceSummaryService financeSummaryService;

    @Test
    void getMonthlySummary_should_return_balance_from_completed_income_minus_expenses() {
        when(financeSummaryRepository.sumCompletedIncomeBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(new BigDecimal("3600.00"));
        when(expenseRepository.sumBetween(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)))
                .thenReturn(new BigDecimal("800.00"));

        MonthlyFinanceSummaryResponse summary = financeSummaryService.getMonthlySummary(2026, 5);

        assertAll(
                () -> assertEquals(2026, summary.year()),
                () -> assertEquals(5, summary.month()),
                () -> assertEquals(new BigDecimal("3600.00"), summary.completedIncome()),
                () -> assertEquals(new BigDecimal("800.00"), summary.recordedExpenses()),
                () -> assertEquals(new BigDecimal("2800.00"), summary.balance())
        );

        verify(financeSummaryRepository).sumCompletedIncomeBetween(any(OffsetDateTime.class), any(OffsetDateTime.class));
        verify(expenseRepository).sumBetween(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));
    }

    @Test
    void getFinanceHistory_should_return_requested_months_in_descending_order() {
        when(financeSummaryRepository.sumCompletedIncomeBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(new BigDecimal("3600.00"), new BigDecimal("2400.00"), BigDecimal.ZERO);
        when(expenseRepository.sumBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("800.00"), new BigDecimal("500.00"), new BigDecimal("100.00"));

        FinanceHistoryResponse history = financeSummaryService.getFinanceHistory(3);

        LocalDate currentMonthStart = LocalDate.now(HAVANA).withDayOfMonth(1);

        assertAll(
                () -> assertEquals(3, history.months().size()),
                () -> assertEquals(currentMonthStart.getYear(), history.months().get(0).year()),
                () -> assertEquals(currentMonthStart.getMonthValue(), history.months().get(0).month()),
                () -> assertEquals(new BigDecimal("3600.00"), history.months().get(0).income()),
                () -> assertEquals(new BigDecimal("800.00"), history.months().get(0).expenses()),
                () -> assertEquals(currentMonthStart.minusMonths(1).getYear(), history.months().get(1).year()),
                () -> assertEquals(currentMonthStart.minusMonths(1).getMonthValue(), history.months().get(1).month()),
                () -> assertEquals(new BigDecimal("2400.00"), history.months().get(1).income()),
                () -> assertEquals(new BigDecimal("500.00"), history.months().get(1).expenses()),
                () -> assertEquals(currentMonthStart.minusMonths(2).getYear(), history.months().get(2).year()),
                () -> assertEquals(currentMonthStart.minusMonths(2).getMonthValue(), history.months().get(2).month()),
                () -> assertEquals(BigDecimal.ZERO, history.months().get(2).income()),
                () -> assertEquals(new BigDecimal("100.00"), history.months().get(2).expenses())
        );
    }

    @Test
    void getFinanceHistory_should_reject_invalid_month_count() {
        assertThrows(BadRequestException.class, () -> financeSummaryService.getFinanceHistory(0));
        assertThrows(BadRequestException.class, () -> financeSummaryService.getFinanceHistory(25));
    }

    @Test
    void getRangeSummary_should_fill_missing_days_with_zero_values() {
        LocalDate from = LocalDate.of(2026, 4, 5);
        LocalDate to = LocalDate.of(2026, 4, 7);

        when(financeSummaryRepository.sumCompletedIncomeBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(new BigDecimal("4500.00"));
        when(expenseRepository.sumBetween(from, to)).thenReturn(new BigDecimal("900.00"));
        when(expenseRepository.findBetween(from, to)).thenReturn(List.of(
                new ExpenseRecord(
                        UUID.randomUUID(),
                        null,
                        null,
                        LocalDate.of(2026, 4, 6),
                        "Insumos",
                        new BigDecimal("400.00"),
                        null,
                        OffsetDateTime.parse("2026-04-06T10:00:00Z")
                ),
                new ExpenseRecord(
                        UUID.randomUUID(),
                        null,
                        null,
                        LocalDate.of(2026, 4, 7),
                        "Taxi",
                        new BigDecimal("500.00"),
                        null,
                        OffsetDateTime.parse("2026-04-07T11:00:00Z")
                )
        ));
        when(financeSummaryRepository.dailyIncomeBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(
                        new DailyFinanceEntry(LocalDate.of(2026, 4, 5), new BigDecimal("1500.00"), BigDecimal.ZERO),
                        new DailyFinanceEntry(LocalDate.of(2026, 4, 7), new BigDecimal("3000.00"), BigDecimal.ZERO)
                ));

        RangeFinanceResponse response = financeSummaryService.getRangeSummary(from, to);

        assertAll(
                () -> assertEquals(new BigDecimal("4500.00"), response.completedIncome()),
                () -> assertEquals(new BigDecimal("900.00"), response.recordedExpenses()),
                () -> assertEquals(new BigDecimal("3600.00"), response.balance()),
                () -> assertEquals(2, response.expenses().size()),
                () -> assertEquals(3, response.days().size()),
                () -> assertEquals(LocalDate.of(2026, 4, 5), response.days().get(0).date()),
                () -> assertEquals(new BigDecimal("1500.00"), response.days().get(0).income()),
                () -> assertEquals(BigDecimal.ZERO, response.days().get(0).expenses()),
                () -> assertEquals(LocalDate.of(2026, 4, 6), response.days().get(1).date()),
                () -> assertEquals(BigDecimal.ZERO, response.days().get(1).income()),
                () -> assertEquals(new BigDecimal("400.00"), response.days().get(1).expenses()),
                () -> assertEquals(LocalDate.of(2026, 4, 7), response.days().get(2).date()),
                () -> assertEquals(new BigDecimal("3000.00"), response.days().get(2).income()),
                () -> assertEquals(new BigDecimal("500.00"), response.days().get(2).expenses())
        );
    }

    @Test
    void getRangeSummary_should_reject_invalid_range() {
        assertThrows(BadRequestException.class, () -> financeSummaryService.getRangeSummary(
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 1)
        ));
    }
}
