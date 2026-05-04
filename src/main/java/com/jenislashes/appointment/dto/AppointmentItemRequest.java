package com.jenislashes.appointment.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AppointmentItemRequest(
        @NotNull UUID serviceId,
        boolean touchUp
) {
}
