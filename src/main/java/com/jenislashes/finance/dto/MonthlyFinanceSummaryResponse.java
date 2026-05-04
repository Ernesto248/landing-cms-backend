package com.jenislashes.finance.dto;

import java.math.BigDecimal;

public record MonthlyFinanceSummaryResponse(
        int year,
        int month,
        BigDecimal completedIncome,
        BigDecimal recordedExpenses,
        BigDecimal balance
) {
}
