package com.jenislashes.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyFinanceEntry(
        LocalDate date,
        BigDecimal income,
        BigDecimal expenses
) {
}
