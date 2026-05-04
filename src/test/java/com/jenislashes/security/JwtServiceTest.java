package com.jenislashes.security;

import com.jenislashes.config.SecurityProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("JwtService")
class JwtServiceTest {

    @Test
    void generateAccessToken_should_embed_subject_and_respect_expiration_minutes() {
        SecurityProperties securityProperties = new SecurityProperties();
        securityProperties.getJwt().setIssuer("jeni-test");
        securityProperties.getJwt().setSecret("01234567890123456789012345678901");
        securityProperties.getJwt().setAccessTokenMinutes(15);
        JwtService jwtService = new JwtService(securityProperties);
        AdminPrincipal principal = new AdminPrincipal(
                UUID.randomUUID(),
                "admin@jeni.com",
                "hash",
                "Jeni",
                "ADMIN",
                true
        );

        String token = jwtService.generateAccessToken(principal);

        assertEquals(principal.getId().toString(), jwtService.extractSubject(token));
        assertEquals(900L, jwtService.getAccessTokenExpiresInSeconds());
    }

    @Test
    void constructor_should_accept_base64_secret_when_raw_secret_is_short() {
        SecurityProperties securityProperties = new SecurityProperties();
        securityProperties.getJwt().setIssuer("jeni-test");
        securityProperties.getJwt().setSecret(Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes()));
        securityProperties.getJwt().setAccessTokenMinutes(10);
        JwtService jwtService = new JwtService(securityProperties);
        AdminPrincipal principal = new AdminPrincipal(
                UUID.randomUUID(),
                "admin@jeni.com",
                "hash",
                "Jeni",
                "ADMIN",
                true
        );

        String token = jwtService.generateAccessToken(principal);

        assertEquals(principal.getId().toString(), jwtService.extractSubject(token));
    }
}
