package com.jenislashes.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateExpenseRequest(
        UUID expenseCategoryId,
        @NotNull LocalDate expenseDate,
        @NotBlank String description,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String notes
) {
}
