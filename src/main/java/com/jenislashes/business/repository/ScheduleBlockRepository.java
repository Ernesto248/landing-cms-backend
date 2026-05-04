package com.jenislashes.business.repository;

import com.jenislashes.business.model.ScheduleBlockRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class ScheduleBlockRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<ScheduleBlockRecord> rowMapper = (rs, rowNum) -> new ScheduleBlockRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("block_date", LocalDate.class),
            rs.getObject("start_time", LocalTime.class),
            rs.getObject("end_time", LocalTime.class),
            rs.getString("reason"),
            rs.getBoolean("is_full_day"),
            rs.getObject("created_at", OffsetDateTime.class)
    );

    public ScheduleBlockRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ScheduleBlockRecord> findBetween(LocalDate from, LocalDate toInclusive) {
        return jdbcTemplate.query(
                """
                select id, block_date, start_time, end_time, reason, is_full_day, created_at
                from schedule_blocks
                where block_date >= ? and block_date <= ?
                order by block_date asc, start_time asc nulls first
                """,
                rowMapper,
                from,
                toInclusive
        );
    }

    public void insert(ScheduleBlockRecord record) {
        jdbcTemplate.update(
                """
                insert into schedule_blocks (id, block_date, start_time, end_time, reason, is_full_day, created_at)
                values (?, ?, ?, ?, ?, ?, ?)
                """,
                record.id(),
                record.blockDate(),
                record.startTime(),
                record.endTime(),
                record.reason(),
                record.isFullDay(),
                record.createdAt()
        );
    }

    public void delete(UUID scheduleBlockId) {
        jdbcTemplate.update("delete from schedule_blocks where id = ?", scheduleBlockId);
    }
}
