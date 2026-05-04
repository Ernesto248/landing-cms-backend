package com.jenislashes.auth.service;

import com.jenislashes.auth.model.AdminUser;
import com.jenislashes.auth.repository.AdminUserRepository;
import com.jenislashes.config.SecurityProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminBootstrapService")
class AdminBootstrapServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final SecurityProperties securityProperties = new SecurityProperties();

    private AdminBootstrapService adminBootstrapService() {
        return new AdminBootstrapService(adminUserRepository, passwordEncoder, securityProperties);
    }

    @Test
    void run_should_skip_when_admin_already_exists() throws Exception {
        when(adminUserRepository.count()).thenReturn(1L);

        adminBootstrapService().run(null);

        verify(adminUserRepository, never()).insert(any(AdminUser.class));
    }

    @Test
    void run_should_skip_when_admin_credentials_are_missing() throws Exception {
        when(adminUserRepository.count()).thenReturn(0L);

        adminBootstrapService().run(null);

        verify(adminUserRepository, never()).insert(any(AdminUser.class));
    }

    @Test
    void run_should_insert_normalized_admin_when_configuration_exists() throws Exception {
        when(adminUserRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-secret");

        securityProperties.getAdmin().setEmail("  ADMIN@JENI.COM  ");
        securityProperties.getAdmin().setPassword("secret123");
        securityProperties.getAdmin().setFullName("  Jeni Admin  ");

        adminBootstrapService().run(null);

        ArgumentCaptor<AdminUser> adminCaptor = ArgumentCaptor.forClass(AdminUser.class);
        verify(adminUserRepository).insert(adminCaptor.capture());

        AdminUser savedAdmin = adminCaptor.getValue();

        assertAll(
                () -> assertEquals("admin@jeni.com", savedAdmin.email()),
                () -> assertEquals("encoded-secret", savedAdmin.passwordHash()),
                () -> assertEquals("Jeni Admin", savedAdmin.fullName()),
                () -> assertEquals("ADMIN", savedAdmin.role()),
                () -> assertEquals(true, savedAdmin.isActive())
        );
    }
}
