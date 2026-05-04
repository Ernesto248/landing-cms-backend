package com.jenislashes.appointment.model;

import java.math.BigDecimal;
import java.util.UUID;

public record AppointmentItemRecord(
        UUID id,
        UUID appointmentId,
        UUID serviceId,
        String serviceNameSnapshot,
        BigDecimal unitPriceSnapshot,
        int durationSnapshotMinutes,
        boolean isTouchUp,
        BigDecimal discountAmount,
        BigDecimal finalPrice
) {
}
