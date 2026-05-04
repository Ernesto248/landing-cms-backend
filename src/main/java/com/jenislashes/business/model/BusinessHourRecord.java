package com.jenislashes.business.model;

import java.time.LocalTime;
import java.util.UUID;

public record BusinessHourRecord(
        UUID id,
        short dayOfWeek,
        LocalTime openTime,
        LocalTime closeTime,
        boolean isClosed
) {
}
