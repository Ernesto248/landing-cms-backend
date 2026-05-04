package com.jenislashes.servicecatalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpsertServiceRequest(
        @NotBlank String category,
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin("0.00") BigDecimal basePrice,
        @Min(1) int durationMinutes,
        boolean supportsTouchUp,
        @NotNull @DecimalMin("0.00") BigDecimal touchUpDiscount,
        boolean isActive,
        @Min(0) int sortOrder
) {
}
