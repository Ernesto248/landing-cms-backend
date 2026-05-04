package com.jenislashes.media.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GalleryItemRecord(
        UUID id,
        String fileKey,
        String publicUrl,
        String altText,
        String caption,
        int sortOrder,
        boolean isActive,
        OffsetDateTime createdAt
) {
}
