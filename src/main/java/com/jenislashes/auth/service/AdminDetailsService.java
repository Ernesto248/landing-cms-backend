package com.jenislashes.auth.service;

import com.jenislashes.auth.repository.AdminUserRepository;
import com.jenislashes.security.AdminPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class AdminDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public AdminDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return loadByEmail(username);
    }

    public AdminPrincipal loadByEmail(String email) {
        return adminUserRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .filter(adminUser -> adminUser.isActive())
                .map(AdminPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("Admin user not found"));
    }

    public AdminPrincipal loadById(UUID userId) {
        return adminUserRepository.findById(userId)
                .filter(adminUser -> adminUser.isActive())
                .map(AdminPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("Admin user not found"));
    }
}
