package com.jenislashes.business.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BusinessProfileResponse(
        UUID id,
        String brandName,
        String tagline,
        String description,
        String phoneWhatsapp,
        String addressLine,
        String city,
        String country,
        String currencyCode,
        String timezone,
        String instagramUrl,
        String facebookUrl,
        boolean bookingEnabled,
        boolean supportsHomeService,
        boolean supportsStudioService,
        OffsetDateTime updatedAt
) {
}
