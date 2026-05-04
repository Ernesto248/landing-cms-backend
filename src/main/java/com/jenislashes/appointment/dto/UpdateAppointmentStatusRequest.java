package com.jenislashes.appointment.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateAppointmentStatusRequest(
        @NotBlank String status,
        String cancelReason
) {
}
