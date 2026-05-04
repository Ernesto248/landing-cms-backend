package com.jenislashes.business.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateBusinessProfileRequest(
        @NotBlank String brandName,
        String tagline,
        String description,
        @NotBlank String phoneWhatsapp,
        String addressLine,
        @NotBlank String city,
        @NotBlank String country,
        @NotBlank String currencyCode,
        @NotBlank String timezone,
        String instagramUrl,
        String facebookUrl,
        boolean bookingEnabled,
        boolean supportsHomeService,
        boolean supportsStudioService
) {
}
