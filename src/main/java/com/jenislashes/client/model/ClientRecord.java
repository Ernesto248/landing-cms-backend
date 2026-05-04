package com.jenislashes.client.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClientRecord(
        UUID id,
        String fullName,
        String phone,
        String whatsapp,
        String notes,
        OffsetDateTime lastVisitAt,
        int totalAppointments,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
