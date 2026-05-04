package com.jenislashes.content.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LandingContentRecord(
        UUID id,
        String contentKey,
        String title,
        String subtitle,
        String body,
        String jsonValue,
        OffsetDateTime updatedAt
) {
}
