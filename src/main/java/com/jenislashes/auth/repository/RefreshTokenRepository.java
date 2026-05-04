package com.jenislashes.auth.repository;

import com.jenislashes.auth.model.RefreshTokenRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<RefreshTokenRecord> rowMapper = (rs, rowNum) -> new RefreshTokenRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("user_id", UUID.class),
            rs.getString("token_hash"),
            rs.getObject("expires_at", OffsetDateTime.class),
            rs.getObject("revoked_at", OffsetDateTime.class),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getString("ip_address"),
            rs.getString("user_agent")
    );

    public RefreshTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<RefreshTokenRecord> findById(UUID id) {
        return jdbcTemplate.query(
                """
                select id, user_id, token_hash, expires_at, revoked_at, created_at, ip_address, user_agent
                from refresh_tokens
                where id = ?
                limit 1
                """,
                rowMapper,
                id
        ).stream().findFirst();
    }

    public void save(RefreshTokenRecord refreshTokenRecord) {
        jdbcTemplate.update(
                """
                insert into refresh_tokens (id, user_id, token_hash, expires_at, revoked_at, created_at, ip_address, user_agent)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                refreshTokenRecord.id(),
                refreshTokenRecord.userId(),
                refreshTokenRecord.tokenHash(),
                refreshTokenRecord.expiresAt(),
                refreshTokenRecord.revokedAt(),
                refreshTokenRecord.createdAt(),
                refreshTokenRecord.ipAddress(),
                refreshTokenRecord.userAgent()
        );
    }

    public void revokeById(UUID id, OffsetDateTime revokedAt) {
        jdbcTemplate.update(
                "update refresh_tokens set revoked_at = ? where id = ? and revoked_at is null",
                revokedAt,
                id
        );
    }

    public void revokeActiveByUserId(UUID userId, OffsetDateTime revokedAt) {
        jdbcTemplate.update(
                """
                update refresh_tokens
                set revoked_at = ?
                where user_id = ?
                  and revoked_at is null
                  and expires_at > ?
                """,
                revokedAt,
                userId,
                revokedAt
        );
    }
}
