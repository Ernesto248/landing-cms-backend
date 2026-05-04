package com.jenislashes.content.testimonial.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TestimonialRecord(
        UUID id,
        String clientName,
        String text,
        Short rating,
        boolean isFeatured,
        int sortOrder,
        OffsetDateTime createdAt
) {
}
