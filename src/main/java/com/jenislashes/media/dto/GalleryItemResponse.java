package com.jenislashes.media.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GalleryItemResponse(
        UUID id,
        String fileKey,
        String publicUrl,
        String altText,
        String caption,
        int sortOrder,
        boolean isActive,
        UUID serviceId,
        String serviceName,
        String serviceCategory,
        OffsetDateTime createdAt
) {
}
