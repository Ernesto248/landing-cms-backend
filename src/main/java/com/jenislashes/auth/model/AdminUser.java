package com.jenislashes.auth.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminUser(
        UUID id,
        String email,
        String passwordHash,
        String fullName,
        String role,
        boolean isActive,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
