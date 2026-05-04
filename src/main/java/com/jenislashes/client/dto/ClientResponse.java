package com.jenislashes.client.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClientResponse(
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
