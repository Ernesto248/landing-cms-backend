package com.jenislashes.content.testimonial.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpsertTestimonialRequest(
        @NotBlank String clientName,
        @NotBlank String text,
        @Min(1) @Max(5) Short rating,
        boolean isFeatured,
        @Min(0) int sortOrder
) {
}
