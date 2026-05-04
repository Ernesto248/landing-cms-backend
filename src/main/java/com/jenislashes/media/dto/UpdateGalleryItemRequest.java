package com.jenislashes.media.dto;

import jakarta.validation.constraints.Min;

public record UpdateGalleryItemRequest(
        String altText,
        String caption,
        @Min(0) int sortOrder,
        boolean isActive
) {
}
