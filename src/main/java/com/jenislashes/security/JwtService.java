package com.jenislashes.security;

import com.jenislashes.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecurityProperties securityProperties;
    private final SecretKey secretKey;

    public JwtService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        this.secretKey = buildSecretKey(securityProperties.getJwt().getSecret());
    }

    public String generateAccessToken(AdminPrincipal adminPrincipal) {
        Instant now = Instant.now();
        Instant expiration = now.plus(Duration.ofMinutes(securityProperties.getJwt().getAccessTokenMinutes()));

        return Jwts.builder()
                .issuer(securityProperties.getJwt().getIssuer())
                .subject(adminPrincipal.getId().toString())
                .claim("email", adminPrincipal.getEmail())
                .claim("role", adminPrincipal.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public long getAccessTokenExpiresInSeconds() {
        return Duration.ofMinutes(securityProperties.getJwt().getAccessTokenMinutes()).toSeconds();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(securityProperties.getJwt().getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey buildSecretKey(String secret) {
        byte[] rawSecret = secret.getBytes(StandardCharsets.UTF_8);
        if (rawSecret.length >= 32) {
            return Keys.hmacShaKeyFor(rawSecret);
        }

        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
