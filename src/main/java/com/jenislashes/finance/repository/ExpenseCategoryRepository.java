package com.jenislashes.finance.repository;

import com.jenislashes.finance.model.ExpenseCategoryRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ExpenseCategoryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<ExpenseCategoryRecord> rowMapper = (rs, rowNum) -> new ExpenseCategoryRecord(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getBoolean("is_active"),
            rs.getObject("created_at", OffsetDateTime.class)
    );

    public ExpenseCategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ExpenseCategoryRecord> findAll() {
        return jdbcTemplate.query(
                "select id, name, is_active, created_at from expense_categories order by name asc",
                rowMapper
        );
    }

    public Optional<ExpenseCategoryRecord> findById(UUID categoryId) {
        return jdbcTemplate.query(
                "select id, name, is_active, created_at from expense_categories where id = ? limit 1",
                rowMapper,
                categoryId
        ).stream().findFirst();
    }

    public void insert(ExpenseCategoryRecord record) {
        jdbcTemplate.update(
                "insert into expense_categories (id, name, is_active, created_at) values (?, ?, ?, ?)",
                record.id(),
                record.name(),
                record.isActive(),
                record.createdAt()
        );
    }

    public void deleteById(UUID categoryId) {
        jdbcTemplate.update("delete from expense_categories where id = ?", categoryId);
    }

    public Optional<ExpenseCategoryRecord> findByName(String name) {
        return jdbcTemplate.query(
                "select id, name, is_active, created_at from expense_categories where name = ? limit 1",
                rowMapper,
                name
        ).stream().findFirst();
    }
}
