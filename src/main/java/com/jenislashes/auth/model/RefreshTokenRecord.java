package com.jenislashes.auth.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RefreshTokenRecord(
        UUID id,
        UUID userId,
        String tokenHash,
        OffsetDateTime expiresAt,
        OffsetDateTime revokedAt,
        OffsetDateTime createdAt,
        String ipAddress,
        String userAgent
) {
}
