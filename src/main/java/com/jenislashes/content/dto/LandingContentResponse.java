package com.jenislashes.content.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LandingContentResponse(
        UUID id,
        String contentKey,
        String title,
        String subtitle,
        String body,
        JsonNode jsonValue,
        OffsetDateTime updatedAt
) {
}
