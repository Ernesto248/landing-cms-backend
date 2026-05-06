package com.jenislashes.finance.repository;

import com.jenislashes.finance.dto.CategoryEntry;
import com.jenislashes.finance.dto.DailyFinanceEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class FinanceSummaryRepository {

    private final JdbcTemplate jdbcTemplate;

    public FinanceSummaryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BigDecimal sumCompletedIncomeBetween(OffsetDateTime from, OffsetDateTime toExclusive) {
        BigDecimal total = jdbcTemplate.queryForObject(
                """
                select coalesce(sum(total_amount), 0)
                from appointments
                where status = 'COMPLETED'
                  and completed_at >= ?
                  and completed_at < ?
                """,
                BigDecimal.class,
                from,
                toExclusive
        );
        return total == null ? BigDecimal.ZERO : total;
    }

    public List<DailyFinanceEntry> dailyIncomeBetween(OffsetDateTime from, OffsetDateTime toExclusive) {
        RowMapper<DailyFinanceEntry> mapper = (rs, rowNum) -> new DailyFinanceEntry(
                rs.getObject("day", LocalDate.class),
                rs.getBigDecimal("income"),
                BigDecimal.ZERO
        );

        return jdbcTemplate.query(
                """
                select (a.completed_at at time zone 'America/Havana')::date as day,
                       coalesce(sum(a.total_amount), 0) as income
                from appointments a
                where a.status = 'COMPLETED'
                  and a.completed_at >= ?
                  and a.completed_at < ?
                group by (a.completed_at at time zone 'America/Havana')::date
                order by day asc
                """,
                mapper,
                from,
                toExclusive
        );
    }

    public List<CategoryEntry> incomeBreakdownByServiceCategory(OffsetDateTime from, OffsetDateTime toExclusive) {
        RowMapper<CategoryEntry> mapper = (rs, rowNum) -> new CategoryEntry(
                rs.getString("category"),
                rs.getBigDecimal("amount")
        );

        return jdbcTemplate.query(
                """
                select s.category, coalesce(sum(ai.final_price), 0) as amount
                from appointment_items ai
                join appointments a on a.id = ai.appointment_id
                join services s on s.id = ai.service_id
                where a.status = 'COMPLETED'
                  and a.completed_at >= ?
                  and a.completed_at < ?
                group by s.category
                order by amount desc
                """,
                mapper,
                from,
                toExclusive
        );
    }
}
