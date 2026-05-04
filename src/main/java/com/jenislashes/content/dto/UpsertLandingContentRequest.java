package com.jenislashes.content.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;

public record UpsertLandingContentRequest(
        @NotBlank String contentKey,
        String title,
        String subtitle,
        String body,
        JsonNode jsonValue
) {
}
