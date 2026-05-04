package com.jenislashes.business.repository;

import com.jenislashes.business.model.BusinessProfileRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BusinessProfileRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<BusinessProfileRecord> rowMapper = (rs, rowNum) -> new BusinessProfileRecord(
            rs.getObject("id", UUID.class),
            rs.getString("brand_name"),
            rs.getString("tagline"),
            rs.getString("description"),
            rs.getString("phone_whatsapp"),
            rs.getString("address_line"),
            rs.getString("city"),
            rs.getString("country"),
            rs.getString("currency_code"),
            rs.getString("timezone"),
            rs.getString("instagram_url"),
            rs.getString("facebook_url"),
            rs.getBoolean("booking_enabled"),
            rs.getBoolean("supports_home_service"),
            rs.getBoolean("supports_studio_service"),
            rs.getObject("updated_at", OffsetDateTime.class)
    );

    public BusinessProfileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<BusinessProfileRecord> findCurrent() {
        return jdbcTemplate.query(
                """
                select id, brand_name, tagline, description, phone_whatsapp, address_line, city, country,
                       currency_code, timezone, instagram_url, facebook_url, booking_enabled,
                       supports_home_service, supports_studio_service, updated_at
                from business_profile
                limit 1
                """,
                rowMapper
        ).stream().findFirst();
    }

    public void update(BusinessProfileRecord record) {
        jdbcTemplate.update(
                """
                update business_profile
                set brand_name = ?, tagline = ?, description = ?, phone_whatsapp = ?, address_line = ?,
                    city = ?, country = ?, currency_code = ?, timezone = ?, instagram_url = ?,
                    facebook_url = ?, booking_enabled = ?, supports_home_service = ?,
                    supports_studio_service = ?, updated_at = ?
                where id = ?
                """,
                record.brandName(),
                record.tagline(),
                record.description(),
                record.phoneWhatsapp(),
                record.addressLine(),
                record.city(),
                record.country(),
                record.currencyCode(),
                record.timezone(),
                record.instagramUrl(),
                record.facebookUrl(),
                record.bookingEnabled(),
                record.supportsHomeService(),
                record.supportsStudioService(),
                record.updatedAt(),
                record.id()
        );
    }
}
