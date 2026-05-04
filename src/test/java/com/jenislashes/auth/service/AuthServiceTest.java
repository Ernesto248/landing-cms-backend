package com.jenislashes.auth.service;

import com.jenislashes.auth.dto.LoginRequest;
import com.jenislashes.auth.exception.UnauthorizedException;
import com.jenislashes.auth.model.RefreshTokenRecord;
import com.jenislashes.auth.repository.RefreshTokenRepository;
import com.jenislashes.config.SecurityProperties;
import com.jenislashes.security.AdminPrincipal;
import com.jenislashes.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AdminDetailsService adminDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Authentication authentication;

    private SecurityProperties securityProperties;

    @InjectMocks
    private AuthService authService;

    private AdminPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties();
        securityProperties.getRefresh().setCookieName("jeni_refresh_token");
        securityProperties.getRefresh().setTokenDays(14);
        authService = new AuthService(
                authenticationManager,
                refreshTokenRepository,
                adminDetailsService,
                jwtService,
                passwordEncoder,
                securityProperties
        );

        adminPrincipal = new AdminPrincipal(
                UUID.randomUUID(),
                "admin@jenilashes.local",
                "$2a$10$hash",
                "Jeni Admin",
                "ADMIN",
                true
        );
    }

    @Test
    void login_should_issue_session_when_credentials_are_valid() {
        LoginRequest request = new LoginRequest("Admin@JeniLashes.local", "secret");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminPrincipal);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-refresh-secret");
        when(jwtService.generateAccessToken(adminPrincipal)).thenReturn("jwt-token");
        when(jwtService.getAccessTokenExpiresInSeconds()).thenReturn(900L);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader(eq("User-Agent"))).thenReturn("JUnit");
        when(httpServletRequest.getHeader(eq("X-Forwarded-For"))).thenReturn(null);

        AuthService.AuthSession authSession = authService.login(request, httpServletRequest);

        ArgumentCaptor<RefreshTokenRecord> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshTokenRecord.class);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

        RefreshTokenRecord savedToken = refreshTokenCaptor.getValue();
        assertAll(
                () -> assertEquals("jwt-token", authSession.response().accessToken()),
                () -> assertEquals("Bearer", authSession.response().tokenType()),
                () -> assertEquals(900L, authSession.response().expiresInSeconds()),
                () -> assertEquals(adminPrincipal.getEmail(), authSession.response().user().email()),
                () -> assertTrue(authSession.refreshToken().startsWith(savedToken.id().toString() + ".")),
                () -> assertEquals(adminPrincipal.getId(), savedToken.userId()),
                () -> assertEquals("encoded-refresh-secret", savedToken.tokenHash()),
                () -> assertEquals("127.0.0.1", savedToken.ipAddress()),
                () -> assertEquals("JUnit", savedToken.userAgent())
        );
    }

    @Test
    void login_should_throw_unauthorized_when_credentials_are_invalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThrows(UnauthorizedException.class, () -> authService.login(new LoginRequest("admin@test.com", "wrong"), httpServletRequest));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refresh_should_issue_new_session_when_refresh_token_is_valid() {
        UUID tokenId = UUID.randomUUID();
        String rawRefreshToken = tokenId + ".refresh-secret";
        RefreshTokenRecord storedToken = new RefreshTokenRecord(
                tokenId,
                adminPrincipal.getId(),
                "stored-hash",
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(2),
                null,
                OffsetDateTime.now(ZoneOffset.UTC),
                "127.0.0.1",
                "JUnit"
        );

        when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(storedToken));
        when(passwordEncoder.matches("refresh-secret", "stored-hash")).thenReturn(true);
        when(adminDetailsService.loadById(adminPrincipal.getId())).thenReturn(adminPrincipal);
        when(passwordEncoder.encode(anyString())).thenReturn("new-encoded-refresh-secret");
        when(jwtService.generateAccessToken(adminPrincipal)).thenReturn("new-jwt-token");
        when(jwtService.getAccessTokenExpiresInSeconds()).thenReturn(900L);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader(eq("User-Agent"))).thenReturn("JUnit");
        when(httpServletRequest.getHeader(eq("X-Forwarded-For"))).thenReturn(null);

        AuthService.AuthSession authSession = authService.refresh(rawRefreshToken, httpServletRequest);

        verify(refreshTokenRepository).revokeById(eq(tokenId), any(OffsetDateTime.class));
        verify(refreshTokenRepository).save(any(RefreshTokenRecord.class));
        assertEquals("new-jwt-token", authSession.response().accessToken());
    }

    @Test
    void requireRefreshToken_should_return_cookie_value_when_cookie_exists() {
        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{new Cookie("jeni_refresh_token", "token-value")});

        String refreshToken = authService.requireRefreshToken(httpServletRequest);

        assertEquals("token-value", refreshToken);
    }

    @Test
    void buildRefreshCookie_should_mark_cookie_as_secure_when_forwarded_proto_is_https() {
        when(httpServletRequest.getHeader("X-Forwarded-Proto")).thenReturn("https");

        String cookie = authService.buildRefreshCookie("refresh-token", httpServletRequest).toString();

        assertAll(
                () -> assertTrue(cookie.contains("HttpOnly")),
                () -> assertTrue(cookie.contains("Secure")),
                () -> assertTrue(cookie.contains("jeni_refresh_token=refresh-token"))
        );
    }
}
