package com.jenislashes.content.repository;

import com.jenislashes.content.model.LandingContentRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LandingContentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<LandingContentRecord> rowMapper = (rs, rowNum) -> new LandingContentRecord(
            rs.getObject("id", UUID.class),
            rs.getString("content_key"),
            rs.getString("title"),
            rs.getString("subtitle"),
            rs.getString("body"),
            rs.getString("json_value"),
            rs.getObject("updated_at", OffsetDateTime.class)
    );

    public LandingContentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<LandingContentRecord> findAll() {
        return jdbcTemplate.query(
                "select id, content_key, title, subtitle, body, json_value::text as json_value, updated_at from landing_content order by content_key asc",
                rowMapper
        );
    }

    public Optional<LandingContentRecord> findByContentKey(String contentKey) {
        return jdbcTemplate.query(
                "select id, content_key, title, subtitle, body, json_value::text as json_value, updated_at from landing_content where content_key = ? limit 1",
                rowMapper,
                contentKey
        ).stream().findFirst();
    }

    public void insert(LandingContentRecord record) {
        jdbcTemplate.update(
                """
                insert into landing_content (id, content_key, title, subtitle, body, json_value, updated_at)
                values (?, ?, ?, ?, ?, cast(? as jsonb), ?)
                """,
                record.id(),
                record.contentKey(),
                record.title(),
                record.subtitle(),
                record.body(),
                record.jsonValue(),
                record.updatedAt()
        );
    }

    public void update(LandingContentRecord record) {
        jdbcTemplate.update(
                """
                update landing_content
                set title = ?, subtitle = ?, body = ?, json_value = cast(? as jsonb), updated_at = ?
                where id = ?
                """,
                record.title(),
                record.subtitle(),
                record.body(),
                record.jsonValue(),
                record.updatedAt(),
                record.id()
        );
    }
}
