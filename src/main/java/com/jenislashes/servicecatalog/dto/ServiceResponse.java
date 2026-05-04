package com.jenislashes.servicecatalog.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String category,
        String name,
        String slug,
        String description,
        BigDecimal basePrice,
        int durationMinutes,
        boolean supportsTouchUp,
        BigDecimal touchUpDiscount,
        boolean isActive,
        int sortOrder
) {
}
