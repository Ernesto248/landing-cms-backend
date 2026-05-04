package com.jenislashes.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        AuthUserResponse user
) {
}
