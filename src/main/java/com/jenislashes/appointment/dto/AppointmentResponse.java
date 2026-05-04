package com.jenislashes.appointment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID clientId,
        String clientName,
        String status,
        String appointmentMode,
        OffsetDateTime scheduledStart,
        OffsetDateTime scheduledEnd,
        String addressSnapshot,
        String notes,
        BigDecimal subtotalAmount,
        BigDecimal travelFee,
        BigDecimal totalAmount,
        OffsetDateTime completedAt,
        OffsetDateTime cancelledAt,
        String cancelReason,
        List<AppointmentItemResponse> items
) {
}
