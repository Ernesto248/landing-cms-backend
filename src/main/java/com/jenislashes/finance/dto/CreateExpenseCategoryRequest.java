package com.jenislashes.finance.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateExpenseCategoryRequest(
        @NotBlank String name,
        boolean isActive
) {
}
