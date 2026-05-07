package com.jenislashes.media.repository;

import com.jenislashes.media.model.GalleryItemRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GalleryItemRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<GalleryItemRecord> rowMapper = (rs, rowNum) -> new GalleryItemRecord(
            rs.getObject("id", UUID.class),
            rs.getString("file_key"),
            rs.getString("public_url"),
            rs.getString("alt_text"),
            rs.getString("caption"),
            rs.getInt("sort_order"),
            rs.getBoolean("is_active"),
            rs.getObject("service_id", UUID.class),
            rs.getString("service_name"),
            rs.getString("service_category"),
            rs.getObject("created_at", OffsetDateTime.class)
    );

    private static final String SELECT_JOIN = """
            select gi.id, gi.file_key, gi.public_url, gi.alt_text, gi.caption,
                   gi.sort_order, gi.is_active, gi.service_id,
                   s.name as service_name, s.category as service_category,
                   gi.created_at
            from gallery_items gi
            left join services s on s.id = gi.service_id
            """;

    public GalleryItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<GalleryItemRecord> findAll() {
        return jdbcTemplate.query(
                SELECT_JOIN + " order by gi.sort_order asc, gi.created_at desc",
                rowMapper
        );
    }

    public List<GalleryItemRecord> findPublic() {
        return jdbcTemplate.query(
                SELECT_JOIN + " where gi.is_active = true and gi.service_id is not null order by gi.sort_order asc, gi.created_at desc",
                rowMapper
        );
    }

    public Optional<GalleryItemRecord> findById(UUID galleryItemId) {
        return jdbcTemplate.query(
                SELECT_JOIN + " where gi.id = ? limit 1",
                rowMapper,
                galleryItemId
        ).stream().findFirst();
    }

    public void insert(GalleryItemRecord record) {
        jdbcTemplate.update(
                "insert into gallery_items (id, file_key, public_url, alt_text, caption, sort_order, is_active, service_id, created_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                record.id(),
                record.fileKey(),
                record.publicUrl(),
                record.altText(),
                record.caption(),
                record.sortOrder(),
                record.isActive(),
                record.serviceId(),
                record.createdAt()
        );
    }

    public void update(GalleryItemRecord record) {
        jdbcTemplate.update(
                "update gallery_items set alt_text = ?, caption = ?, sort_order = ?, is_active = ?, service_id = ? where id = ?",
                record.altText(),
                record.caption(),
                record.sortOrder(),
                record.isActive(),
                record.serviceId(),
                record.id()
        );
    }

    public void delete(UUID galleryItemId) {
        jdbcTemplate.update("delete from gallery_items where id = ?", galleryItemId);
    }
}
