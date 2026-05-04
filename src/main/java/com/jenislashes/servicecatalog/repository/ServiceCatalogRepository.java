package com.jenislashes.servicecatalog.repository;

import com.jenislashes.servicecatalog.model.ServiceCatalogItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ServiceCatalogRepository {

    private static final String BASE_SELECT = """
            select id, category, name, slug, description, base_price, duration_minutes,
                   supports_touch_up, touch_up_discount, is_active, sort_order, created_at, updated_at
            from services
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<ServiceCatalogItem> rowMapper = (rs, rowNum) -> new ServiceCatalogItem(
            rs.getObject("id", UUID.class),
            rs.getString("category"),
            rs.getString("name"),
            rs.getString("slug"),
            rs.getString("description"),
            rs.getBigDecimal("base_price"),
            rs.getInt("duration_minutes"),
            rs.getBoolean("supports_touch_up"),
            rs.getBigDecimal("touch_up_discount"),
            rs.getBoolean("is_active"),
            rs.getInt("sort_order"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("updated_at", OffsetDateTime.class)
    );

    public ServiceCatalogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ServiceCatalogItem> findPublicActive() {
        return jdbcTemplate.query(
                BASE_SELECT + " where is_active = true order by sort_order asc, name asc",
                rowMapper
        );
    }

    public List<ServiceCatalogItem> findAll() {
        return jdbcTemplate.query(
                BASE_SELECT + " order by sort_order asc, name asc",
                rowMapper
        );
    }

    public Optional<ServiceCatalogItem> findById(UUID id) {
        return jdbcTemplate.query(
                BASE_SELECT + " where id = ? limit 1",
                rowMapper,
                id
        ).stream().findFirst();
    }

    public Optional<ServiceCatalogItem> findBySlug(String slug) {
        return jdbcTemplate.query(
                BASE_SELECT + " where slug = ? limit 1",
                rowMapper,
                slug
        ).stream().findFirst();
    }

    public void insert(ServiceCatalogItem item) {
        jdbcTemplate.update(
                """
                insert into services (
                    id, category, name, slug, description, base_price, duration_minutes,
                    supports_touch_up, touch_up_discount, is_active, sort_order, created_at, updated_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                item.id(),
                item.category(),
                item.name(),
                item.slug(),
                item.description(),
                item.basePrice(),
                item.durationMinutes(),
                item.supportsTouchUp(),
                item.touchUpDiscount(),
                item.isActive(),
                item.sortOrder(),
                item.createdAt(),
                item.updatedAt()
        );
    }

    public void update(ServiceCatalogItem item) {
        jdbcTemplate.update(
                """
                update services
                set category = ?,
                    name = ?,
                    slug = ?,
                    description = ?,
                    base_price = ?,
                    duration_minutes = ?,
                    supports_touch_up = ?,
                    touch_up_discount = ?,
                    is_active = ?,
                    sort_order = ?,
                    updated_at = ?
                where id = ?
                """,
                item.category(),
                item.name(),
                item.slug(),
                item.description(),
                item.basePrice(),
                item.durationMinutes(),
                item.supportsTouchUp(),
                item.touchUpDiscount(),
                item.isActive(),
                item.sortOrder(),
                item.updatedAt(),
                item.id()
        );
    }
}
