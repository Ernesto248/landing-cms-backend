package com.jenislashes.finance.dto;

import java.util.UUID;

public record ExpenseCategoryResponse(
        UUID id,
        String name,
        boolean isActive
) {
}
