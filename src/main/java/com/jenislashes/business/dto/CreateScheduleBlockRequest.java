package com.jenislashes.business.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateScheduleBlockRequest(
        @NotNull LocalDate blockDate,
        LocalTime startTime,
        LocalTime endTime,
        String reason,
        boolean isFullDay
) {
}
