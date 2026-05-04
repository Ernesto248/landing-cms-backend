package com.jenislashes.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        UUID expenseCategoryId,
        String expenseCategoryName,
        LocalDate expenseDate,
        String description,
        BigDecimal amount,
        String notes,
        OffsetDateTime createdAt
) {
}
