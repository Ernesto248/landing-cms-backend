package com.jenislashes.finance.dto;

import java.math.BigDecimal;

public record FinanceHistoryMonthResponse(
        int year,
        int month,
        BigDecimal income,
        BigDecimal expenses
) {
}
