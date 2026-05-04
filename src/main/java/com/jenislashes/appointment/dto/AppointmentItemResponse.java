package com.jenislashes.appointment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AppointmentItemResponse(
        UUID id,
        UUID serviceId,
        String serviceNameSnapshot,
        BigDecimal unitPriceSnapshot,
        int durationSnapshotMinutes,
        boolean isTouchUp,
        BigDecimal discountAmount,
        BigDecimal finalPrice
) {
}
