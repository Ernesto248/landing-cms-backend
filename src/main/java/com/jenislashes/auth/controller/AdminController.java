package com.jenislashes.auth.controller;

import com.jenislashes.auth.dto.AuthUserResponse;
import com.jenislashes.security.AdminPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> me(@AuthenticationPrincipal AdminPrincipal adminPrincipal) {
        return ResponseEntity.ok(new AuthUserResponse(
                adminPrincipal.getId(),
                adminPrincipal.getEmail(),
                adminPrincipal.getFullName(),
                adminPrincipal.getRole()
        ));
    }
}
