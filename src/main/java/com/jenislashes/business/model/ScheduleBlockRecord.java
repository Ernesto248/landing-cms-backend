package com.jenislashes.business.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ScheduleBlockRecord(
        UUID id,
        LocalDate blockDate,
        LocalTime startTime,
        LocalTime endTime,
        String reason,
        boolean isFullDay,
        OffsetDateTime createdAt
) {
}
