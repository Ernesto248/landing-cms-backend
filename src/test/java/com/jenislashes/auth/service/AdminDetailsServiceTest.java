package com.jenislashes.auth.service;

import com.jenislashes.auth.model.AdminUser;
import com.jenislashes.auth.repository.AdminUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminDetailsService")
class AdminDetailsServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @InjectMocks
    private AdminDetailsService adminDetailsService;

    @Test
    void loadByEmail_should_normalize_email_and_return_active_admin() {
        UUID adminId = UUID.randomUUID();
        AdminUser adminUser = new AdminUser(
                adminId,
                "admin@jeni.com",
                "hash",
                "Jeni",
                "ADMIN",
                true,
                OffsetDateTime.parse("2026-05-01T10:00:00Z"),
                OffsetDateTime.parse("2026-05-01T10:00:00Z")
        );
        when(adminUserRepository.findByEmail("admin@jeni.com")).thenReturn(Optional.of(adminUser));

        var principal = adminDetailsService.loadByEmail("  ADMIN@JENI.COM  ");

        assertEquals(adminId, principal.getId());
    }

    @Test
    void loadById_should_throw_when_admin_is_inactive() {
        UUID adminId = UUID.randomUUID();
        when(adminUserRepository.findById(adminId)).thenReturn(Optional.of(new AdminUser(
                adminId,
                "admin@jeni.com",
                "hash",
                "Jeni",
                "ADMIN",
                false,
                OffsetDateTime.parse("2026-05-01T10:00:00Z"),
                OffsetDateTime.parse("2026-05-01T10:00:00Z")
        )));

        assertThrows(UsernameNotFoundException.class, () -> adminDetailsService.loadById(adminId));
    }
}
