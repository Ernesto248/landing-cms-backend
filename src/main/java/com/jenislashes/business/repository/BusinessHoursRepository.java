package com.jenislashes.business.repository;

import com.jenislashes.business.model.BusinessHourRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BusinessHoursRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<BusinessHourRecord> rowMapper = (rs, rowNum) -> new BusinessHourRecord(
            rs.getObject("id", UUID.class),
            rs.getShort("day_of_week"),
            rs.getObject("open_time", LocalTime.class),
            rs.getObject("close_time", LocalTime.class),
            rs.getBoolean("is_closed")
    );

    public BusinessHoursRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BusinessHourRecord> findAll() {
        return jdbcTemplate.query(
                "select id, day_of_week, open_time, close_time, is_closed from business_hours order by day_of_week asc",
                rowMapper
        );
    }

    public Optional<BusinessHourRecord> findByDayOfWeek(short dayOfWeek) {
        return jdbcTemplate.query(
                "select id, day_of_week, open_time, close_time, is_closed from business_hours where day_of_week = ? limit 1",
                rowMapper,
                dayOfWeek
        ).stream().findFirst();
    }

    public void insert(BusinessHourRecord record) {
        jdbcTemplate.update(
                "insert into business_hours (id, day_of_week, open_time, close_time, is_closed) values (?, ?, ?, ?, ?)",
                record.id(),
                record.dayOfWeek(),
                record.openTime(),
                record.closeTime(),
                record.isClosed()
        );
    }

    public void update(BusinessHourRecord record) {
        jdbcTemplate.update(
                "update business_hours set open_time = ?, close_time = ?, is_closed = ? where id = ?",
                record.openTime(),
                record.closeTime(),
                record.isClosed(),
                record.id()
        );
    }
}
