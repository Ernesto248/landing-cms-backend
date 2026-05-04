package com.jenislashes.business.dto;

import java.time.LocalTime;
import java.util.UUID;

public record BusinessHourResponse(
        UUID id,
        short dayOfWeek,
        LocalTime openTime,
        LocalTime closeTime,
        boolean isClosed
) {
}
