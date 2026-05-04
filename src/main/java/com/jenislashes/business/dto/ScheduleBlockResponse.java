package com.jenislashes.business.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ScheduleBlockResponse(
        UUID id,
        LocalDate blockDate,
        LocalTime startTime,
        LocalTime endTime,
        String reason,
        boolean isFullDay,
        OffsetDateTime createdAt
) {
}
