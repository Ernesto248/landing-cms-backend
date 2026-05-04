package com.jenislashes.appointment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull UUID clientId,
        @NotNull OffsetDateTime scheduledStart,
        @NotEmpty List<@Valid AppointmentItemRequest> items,
        @NotNull String mode,
        @NotNull @DecimalMin("0.00") BigDecimal travelFee,
        String addressSnapshot,
        String notes
) {
}
