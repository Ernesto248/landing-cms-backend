package com.jenislashes.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public record RangeFinanceResponse(
        BigDecimal completedIncome,
        BigDecimal recordedExpenses,
        BigDecimal balance,
        List<ExpenseResponse> expenses,
        List<DailyFinanceEntry> days
) {
}
