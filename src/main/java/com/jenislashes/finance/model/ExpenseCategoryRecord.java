package com.jenislashes.finance.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ExpenseCategoryRecord(
        UUID id,
        String name,
        boolean isActive,
        OffsetDateTime createdAt
) {
}
