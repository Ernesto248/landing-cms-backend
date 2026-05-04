package com.jenislashes.auth.controller;

import com.jenislashes.auth.dto.AuthResponse;
import com.jenislashes.auth.dto.LoginRequest;
import com.jenislashes.auth.service.AuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AuthService.AuthSession authSession = authService.login(request, httpServletRequest);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authService.buildRefreshCookie(authSession.refreshToken(), httpServletRequest).toString())
                .body(authSession.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest httpServletRequest) {
        AuthService.AuthSession authSession = authService.refresh(
                authService.requireRefreshToken(httpServletRequest),
                httpServletRequest
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authService.buildRefreshCookie(authSession.refreshToken(), httpServletRequest).toString())
                .body(authSession.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest httpServletRequest) {
        authService.logout(authService.findRefreshToken(httpServletRequest).orElse(null));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authService.clearRefreshCookie(httpServletRequest).toString())
                .body(Map.of("message", "Logged out."));
    }
}
