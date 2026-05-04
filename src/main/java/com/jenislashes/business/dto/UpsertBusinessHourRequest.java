package com.jenislashes.business.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalTime;

public record UpsertBusinessHourRequest(
        @Min(1) @Max(7) short dayOfWeek,
        LocalTime openTime,
        LocalTime closeTime,
        boolean isClosed
) {
}
