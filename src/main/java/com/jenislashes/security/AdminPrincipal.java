package com.jenislashes.security;

import com.jenislashes.auth.model.AdminUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AdminPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private final String role;
    private final boolean active;

    public AdminPrincipal(
            UUID id,
            String email,
            String passwordHash,
            String fullName,
            String role,
            boolean active
    ) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    public static AdminPrincipal from(AdminUser adminUser) {
        return new AdminPrincipal(
                adminUser.id(),
                adminUser.email(),
                adminUser.passwordHash(),
                adminUser.fullName(),
                adminUser.role(),
                adminUser.isActive()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
