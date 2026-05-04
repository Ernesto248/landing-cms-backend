package com.jenislashes.servicecatalog.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ServiceCatalogItem(
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
        int sortOrder,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
