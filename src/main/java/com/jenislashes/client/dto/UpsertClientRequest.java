package com.jenislashes.client.dto;

import jakarta.validation.constraints.NotBlank;

public record UpsertClientRequest(
        @NotBlank String fullName,
        String phone,
        String whatsapp,
        String notes
) {
}
