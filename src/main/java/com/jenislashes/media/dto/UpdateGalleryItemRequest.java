package com.jenislashes.media.dto;

import jakarta.validation.constraints.Min;

import java.util.UUID;

public record UpdateGalleryItemRequest(
        String altText,
        String caption,
        @Min(0) int sortOrder,
        boolean isActive,
        UUID serviceId
) {
}
