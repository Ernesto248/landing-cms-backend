package com.jenislashes.finance.repository;

import com.jenislashes.finance.model.ExpenseRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class ExpenseRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<ExpenseRecord> rowMapper = (rs, rowNum) -> new ExpenseRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("expense_category_id", UUID.class),
            rs.getString("expense_category_name"),
            rs.getObject("expense_date", LocalDate.class),
            rs.getString("description"),
            rs.getBigDecimal("amount"),
            rs.getString("notes"),
            rs.getObject("created_at", OffsetDateTime.class)
    );

    public ExpenseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ExpenseRecord> findBetween(LocalDate from, LocalDate toInclusive) {
        return jdbcTemplate.query(
                """
                select e.id, e.expense_category_id, ec.name as expense_category_name, e.expense_date,
                       e.description, e.amount, e.notes, e.created_at
                from expenses e
                left join expense_categories ec on ec.id = e.expense_category_id
                where e.expense_date >= ? and e.expense_date <= ?
                order by e.expense_date desc, e.created_at desc
                """,
                rowMapper,
                from,
                toInclusive
        );
    }

    public void insert(ExpenseRecord expenseRecord) {
        jdbcTemplate.update(
                """
                insert into expenses (id, expense_category_id, expense_date, description, amount, notes, created_at)
                values (?, ?, ?, ?, ?, ?, ?)
                """,
                expenseRecord.id(),
                expenseRecord.expenseCategoryId(),
                expenseRecord.expenseDate(),
                expenseRecord.description(),
                expenseRecord.amount(),
                expenseRecord.notes(),
                expenseRecord.createdAt()
        );
    }

    public BigDecimal sumBetween(LocalDate from, LocalDate toInclusive) {
        BigDecimal total = jdbcTemplate.queryForObject(
                "select coalesce(sum(amount), 0) from expenses where expense_date >= ? and expense_date <= ?",
                BigDecimal.class,
                from,
                toInclusive
        );
        return total == null ? BigDecimal.ZERO : total;
    }
}
