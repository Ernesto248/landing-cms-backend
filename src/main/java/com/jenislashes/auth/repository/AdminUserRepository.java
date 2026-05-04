package com.jenislashes.auth.repository;

import com.jenislashes.auth.model.AdminUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AdminUserRepository {

    private static final String BASE_SELECT = """
            select id, email, password_hash, full_name, role, is_active, created_at, updated_at
            from admin_users
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<AdminUser> rowMapper = (rs, rowNum) -> new AdminUser(
            rs.getObject("id", UUID.class),
            rs.getString("email"),
            rs.getString("password_hash"),
            rs.getString("full_name"),
            rs.getString("role"),
            rs.getBoolean("is_active"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("updated_at", OffsetDateTime.class)
    );

    public AdminUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AdminUser> findByEmail(String email) {
        return jdbcTemplate.query(
                BASE_SELECT + " where lower(email) = lower(?) limit 1",
                rowMapper,
                email
        ).stream().findFirst();
    }

    public Optional<AdminUser> findById(UUID id) {
        return jdbcTemplate.query(
                BASE_SELECT + " where id = ? limit 1",
                rowMapper,
                id
        ).stream().findFirst();
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("select count(*) from admin_users", Long.class);
        return count == null ? 0 : count;
    }

    public void insert(AdminUser adminUser) {
        jdbcTemplate.update(
                """
                insert into admin_users (id, email, password_hash, full_name, role, is_active, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                adminUser.id(),
                adminUser.email(),
                adminUser.passwordHash(),
                adminUser.fullName(),
                adminUser.role(),
                adminUser.isActive(),
                adminUser.createdAt(),
                adminUser.updatedAt()
        );
    }
}
