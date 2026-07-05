package com.jenislashes.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinanceExportEntry(
        LocalDate date,
        String type,
        String category,
        String description,
        BigDecimal amount
) {
}
