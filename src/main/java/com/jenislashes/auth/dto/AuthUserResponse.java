package com.jenislashes.auth.dto;

import java.util.UUID;

public record AuthUserResponse(
        UUID id,
        String email,
        String fullName,
        String role
) {
}
