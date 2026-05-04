package com.jenislashes.auth.service;

import com.jenislashes.auth.model.AdminUser;
import com.jenislashes.auth.repository.AdminUserRepository;
import com.jenislashes.config.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdminBootstrapService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrapService.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    public AdminBootstrapService(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            SecurityProperties securityProperties
    ) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityProperties = securityProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserRepository.count() > 0) {
            return;
        }

        SecurityProperties.Admin adminProperties = securityProperties.getAdmin();
        if (!StringUtils.hasText(adminProperties.getEmail()) || !StringUtils.hasText(adminProperties.getPassword())) {
            logger.warn("No admin user exists and ADMIN_EMAIL / ADMIN_PASSWORD are not configured.");
            return;
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        AdminUser adminUser = new AdminUser(
                UUID.randomUUID(),
                adminProperties.getEmail().trim().toLowerCase(Locale.ROOT),
                passwordEncoder.encode(adminProperties.getPassword()),
                StringUtils.hasText(adminProperties.getFullName()) ? adminProperties.getFullName().trim() : "Jeni Admin",
                "ADMIN",
                true,
                now,
                now
        );

        adminUserRepository.insert(adminUser);
        logger.info("Bootstrapped initial admin user '{}'.", adminUser.email());
    }
}
