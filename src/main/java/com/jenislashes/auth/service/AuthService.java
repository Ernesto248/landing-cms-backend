package com.jenislashes.auth.service;

import com.jenislashes.auth.dto.AuthResponse;
import com.jenislashes.auth.dto.AuthUserResponse;
import com.jenislashes.auth.dto.LoginRequest;
import com.jenislashes.auth.exception.UnauthorizedException;
import com.jenislashes.auth.model.RefreshTokenRecord;
import com.jenislashes.auth.repository.RefreshTokenRepository;
import com.jenislashes.config.SecurityProperties;
import com.jenislashes.security.AdminPrincipal;
import com.jenislashes.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AdminDetailsService adminDetailsService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    public AuthService(
            AuthenticationManager authenticationManager,
            RefreshTokenRepository refreshTokenRepository,
            AdminDetailsService adminDetailsService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            SecurityProperties securityProperties
    ) {
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
        this.adminDetailsService = adminDetailsService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.securityProperties = securityProperties;
    }

    @Transactional
    public AuthSession login(LoginRequest request, HttpServletRequest httpServletRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email().trim().toLowerCase(Locale.ROOT),
                            request.password()
                    )
            );
        } catch (AuthenticationException exception) {
            throw new UnauthorizedException("Invalid email or password");
        }

        AdminPrincipal adminPrincipal = (AdminPrincipal) authentication.getPrincipal();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        refreshTokenRepository.revokeActiveByUserId(adminPrincipal.getId(), now);

        return issueSession(adminPrincipal, httpServletRequest, now);
    }

    @Transactional
    public AuthSession refresh(String rawRefreshToken, HttpServletRequest httpServletRequest) {
        RefreshTokenValue refreshTokenValue = parseRefreshToken(rawRefreshToken);
        RefreshTokenRecord storedToken = refreshTokenRepository.findById(refreshTokenValue.tokenId())
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid or expired"));

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (storedToken.revokedAt() != null || storedToken.expiresAt().isBefore(now)) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        if (!passwordEncoder.matches(refreshTokenValue.secret(), storedToken.tokenHash())) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        AdminPrincipal adminPrincipal;
        try {
            adminPrincipal = adminDetailsService.loadById(storedToken.userId());
        } catch (RuntimeException exception) {
            throw new UnauthorizedException("Admin user is not available");
        }

        refreshTokenRepository.revokeById(storedToken.id(), now);
        return issueSession(adminPrincipal, httpServletRequest, now);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        try {
            RefreshTokenValue refreshTokenValue = parseRefreshToken(rawRefreshToken);
            refreshTokenRepository.revokeById(refreshTokenValue.tokenId(), OffsetDateTime.now(ZoneOffset.UTC));
        } catch (UnauthorizedException ignored) {
            // Clearing the cookie client-side is enough when the token is already missing or malformed.
        }
    }

    public ResponseCookie buildRefreshCookie(String refreshToken, HttpServletRequest httpServletRequest) {
        boolean secureCookie = resolveCookieSecure(httpServletRequest);

        return ResponseCookie.from(securityProperties.getRefresh().getCookieName(), refreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(resolveCookieSameSite(secureCookie))
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(securityProperties.getRefresh().getTokenDays()))
                .build();
    }

    public ResponseCookie clearRefreshCookie(HttpServletRequest httpServletRequest) {
        boolean secureCookie = resolveCookieSecure(httpServletRequest);

        return ResponseCookie.from(securityProperties.getRefresh().getCookieName(), "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(resolveCookieSameSite(secureCookie))
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();
    }

    public String requireRefreshToken(HttpServletRequest httpServletRequest) {
        return findRefreshToken(httpServletRequest)
                .orElseThrow(() -> new UnauthorizedException("Refresh token is missing"));
    }

    public Optional<String> findRefreshToken(HttpServletRequest httpServletRequest) {
        Cookie cookie = WebUtils.getCookie(httpServletRequest, securityProperties.getRefresh().getCookieName());
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            return Optional.empty();
        }

        return Optional.of(cookie.getValue());
    }

    private AuthSession issueSession(
            AdminPrincipal adminPrincipal,
            HttpServletRequest httpServletRequest,
            OffsetDateTime now
    ) {
        RefreshTokenValue refreshTokenValue = generateRefreshToken();
        OffsetDateTime expiresAt = now.plusDays(securityProperties.getRefresh().getTokenDays());

        refreshTokenRepository.save(new RefreshTokenRecord(
                refreshTokenValue.tokenId(),
                adminPrincipal.getId(),
                passwordEncoder.encode(refreshTokenValue.secret()),
                expiresAt,
                null,
                now,
                resolveIpAddress(httpServletRequest),
                truncateUserAgent(httpServletRequest.getHeader("User-Agent"))
        ));

        AuthResponse authResponse = new AuthResponse(
                jwtService.generateAccessToken(adminPrincipal),
                "Bearer",
                jwtService.getAccessTokenExpiresInSeconds(),
                new AuthUserResponse(
                        adminPrincipal.getId(),
                        adminPrincipal.getEmail(),
                        adminPrincipal.getFullName(),
                        adminPrincipal.getRole()
                )
        );

        return new AuthSession(authResponse, refreshTokenValue.rawToken());
    }

    private RefreshTokenValue generateRefreshToken() {
        UUID tokenId = UUID.randomUUID();
        byte[] secretBytes = new byte[32];
        SECURE_RANDOM.nextBytes(secretBytes);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);
        return new RefreshTokenValue(tokenId, secret, tokenId + "." + secret);
    }

    private RefreshTokenValue parseRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is missing");
        }

        String[] segments = rawRefreshToken.split("\\.", 2);
        if (segments.length != 2 || segments[1].isBlank()) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        try {
            UUID tokenId = UUID.fromString(segments[0]);
            return new RefreshTokenValue(tokenId, segments[1], rawRefreshToken);
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }
    }

    private boolean isSecureRequest(HttpServletRequest httpServletRequest) {
        String forwarded = httpServletRequest.getHeader("Forwarded");
        if (forwarded != null && forwarded.toLowerCase(Locale.ROOT).contains("proto=https")) {
            return true;
        }

        String forwardedProto = httpServletRequest.getHeader("X-Forwarded-Proto");
        String forwardedScheme = httpServletRequest.getHeader("X-Forwarded-Scheme");
        String forwardedSsl = httpServletRequest.getHeader("X-Forwarded-Ssl");

        return httpServletRequest.isSecure()
                || "https".equalsIgnoreCase(forwardedProto)
                || "https".equalsIgnoreCase(forwardedScheme)
                || "on".equalsIgnoreCase(forwardedSsl);
    }

    private boolean resolveCookieSecure(HttpServletRequest httpServletRequest) {
        String configuredSecure = securityProperties.getRefresh().getCookieSecure();
        if ("true".equalsIgnoreCase(configuredSecure)) {
            return true;
        }
        if ("false".equalsIgnoreCase(configuredSecure)) {
            return false;
        }

        return isSecureRequest(httpServletRequest);
    }

    private String resolveCookieSameSite(boolean secureCookie) {
        String configuredSameSite = securityProperties.getRefresh().getCookieSameSite();
        if (configuredSameSite != null && !configuredSameSite.isBlank() && !"auto".equalsIgnoreCase(configuredSameSite)) {
            return configuredSameSite;
        }

        return secureCookie ? "None" : "Lax";
    }

    private String resolveIpAddress(HttpServletRequest httpServletRequest) {
        String forwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return httpServletRequest.getRemoteAddr();
    }

    private String truncateUserAgent(String userAgent) {
        if (userAgent == null || userAgent.length() <= 1000) {
            return userAgent;
        }

        return userAgent.substring(0, 1000);
    }

    public record AuthSession(AuthResponse response, String refreshToken) {
    }

    private record RefreshTokenValue(UUID tokenId, String secret, String rawToken) {
    }
}
