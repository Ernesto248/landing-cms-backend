package com.jenislashes.content.testimonial.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TestimonialResponse(
        UUID id,
        String clientName,
        String text,
        Short rating,
        boolean isFeatured,
        int sortOrder,
        OffsetDateTime createdAt
) {
}
