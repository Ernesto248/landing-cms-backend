package com.jenislashes.content.testimonial.repository;

import com.jenislashes.content.testimonial.model.TestimonialRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TestimonialRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<TestimonialRecord> rowMapper = (rs, rowNum) -> new TestimonialRecord(
            rs.getObject("id", UUID.class),
            rs.getString("client_name"),
            rs.getString("text"),
            rs.getObject("rating", Short.class),
            rs.getBoolean("is_featured"),
            rs.getInt("sort_order"),
            rs.getObject("created_at", OffsetDateTime.class)
    );

    public TestimonialRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TestimonialRecord> findAll() {
        return jdbcTemplate.query(
                "select id, client_name, text, rating, is_featured, sort_order, created_at from testimonials order by sort_order asc, created_at desc",
                rowMapper
        );
    }

    public List<TestimonialRecord> findPublic() {
        return jdbcTemplate.query(
                "select id, client_name, text, rating, is_featured, sort_order, created_at from testimonials where is_featured = true order by sort_order asc, created_at desc",
                rowMapper
        );
    }

    public Optional<TestimonialRecord> findById(UUID testimonialId) {
        return jdbcTemplate.query(
                "select id, client_name, text, rating, is_featured, sort_order, created_at from testimonials where id = ? limit 1",
                rowMapper,
                testimonialId
        ).stream().findFirst();
    }

    public void insert(TestimonialRecord record) {
        jdbcTemplate.update(
                "insert into testimonials (id, client_name, text, rating, is_featured, sort_order, created_at) values (?, ?, ?, ?, ?, ?, ?)",
                record.id(),
                record.clientName(),
                record.text(),
                record.rating(),
                record.isFeatured(),
                record.sortOrder(),
                record.createdAt()
        );
    }

    public void update(TestimonialRecord record) {
        jdbcTemplate.update(
                "update testimonials set client_name = ?, text = ?, rating = ?, is_featured = ?, sort_order = ? where id = ?",
                record.clientName(),
                record.text(),
                record.rating(),
                record.isFeatured(),
                record.sortOrder(),
                record.id()
        );
    }
}
