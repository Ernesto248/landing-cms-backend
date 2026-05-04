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
            rs.getObject("created_at", OffsetDateTime.class)
    );

    public GalleryItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<GalleryItemRecord> findAll() {
        return jdbcTemplate.query(
                "select id, file_key, public_url, alt_text, caption, sort_order, is_active, created_at from gallery_items order by sort_order asc, created_at desc",
                rowMapper
        );
    }

    public List<GalleryItemRecord> findPublic() {
        return jdbcTemplate.query(
                "select id, file_key, public_url, alt_text, caption, sort_order, is_active, created_at from gallery_items where is_active = true order by sort_order asc, created_at desc",
                rowMapper
        );
    }

    public Optional<GalleryItemRecord> findById(UUID galleryItemId) {
        return jdbcTemplate.query(
                "select id, file_key, public_url, alt_text, caption, sort_order, is_active, created_at from gallery_items where id = ? limit 1",
                rowMapper,
                galleryItemId
        ).stream().findFirst();
    }

    public void insert(GalleryItemRecord record) {
        jdbcTemplate.update(
                "insert into gallery_items (id, file_key, public_url, alt_text, caption, sort_order, is_active, created_at) values (?, ?, ?, ?, ?, ?, ?, ?)",
                record.id(),
                record.fileKey(),
                record.publicUrl(),
                record.altText(),
                record.caption(),
                record.sortOrder(),
                record.isActive(),
                record.createdAt()
        );
    }

    public void update(GalleryItemRecord record) {
        jdbcTemplate.update(
                "update gallery_items set alt_text = ?, caption = ?, sort_order = ?, is_active = ? where id = ?",
                record.altText(),
                record.caption(),
                record.sortOrder(),
                record.isActive(),
                record.id()
        );
    }

    public void delete(UUID galleryItemId) {
        jdbcTemplate.update("delete from gallery_items where id = ?", galleryItemId);
    }
}
