package com.jenislashes.client.repository;

import com.jenislashes.client.model.ClientRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ClientRepository {

    private static final String BASE_SELECT = """
            select id, full_name, phone, whatsapp, notes, last_visit_at, total_appointments, created_at, updated_at
            from clients
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<ClientRecord> rowMapper = (rs, rowNum) -> new ClientRecord(
            rs.getObject("id", UUID.class),
            rs.getString("full_name"),
            rs.getString("phone"),
            rs.getString("whatsapp"),
            rs.getString("notes"),
            rs.getObject("last_visit_at", OffsetDateTime.class),
            rs.getInt("total_appointments"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("updated_at", OffsetDateTime.class)
    );

    public ClientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ClientRecord> findAll() {
        return jdbcTemplate.query(BASE_SELECT + " order by full_name asc", rowMapper);
    }

    public Optional<ClientRecord> findById(UUID id) {
        return jdbcTemplate.query(BASE_SELECT + " where id = ? limit 1", rowMapper, id).stream().findFirst();
    }

    public void insert(ClientRecord clientRecord) {
        jdbcTemplate.update(
                """
                insert into clients (id, full_name, phone, whatsapp, notes, last_visit_at, total_appointments, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                clientRecord.id(),
                clientRecord.fullName(),
                clientRecord.phone(),
                clientRecord.whatsapp(),
                clientRecord.notes(),
                clientRecord.lastVisitAt(),
                clientRecord.totalAppointments(),
                clientRecord.createdAt(),
                clientRecord.updatedAt()
        );
    }

    public void update(ClientRecord clientRecord) {
        jdbcTemplate.update(
                """
                update clients
                set full_name = ?, phone = ?, whatsapp = ?, notes = ?, updated_at = ?
                where id = ?
                """,
                clientRecord.fullName(),
                clientRecord.phone(),
                clientRecord.whatsapp(),
                clientRecord.notes(),
                clientRecord.updatedAt(),
                clientRecord.id()
        );
    }

    public void refreshAppointmentStats(UUID clientId) {
        jdbcTemplate.update(
                """
                update clients
                set total_appointments = (
                        select count(*)
                        from appointments
                        where client_id = ?
                    ),
                    last_visit_at = (
                        select max(completed_at)
                        from appointments
                        where client_id = ?
                          and status = 'COMPLETED'
                    ),
                    updated_at = now()
                where id = ?
                """,
                clientId,
                clientId,
                clientId
        );
    }
}
