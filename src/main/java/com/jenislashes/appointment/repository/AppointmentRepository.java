package com.jenislashes.appointment.repository;

import com.jenislashes.appointment.model.AppointmentItemRecord;
import com.jenislashes.appointment.model.AppointmentRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AppointmentRepository {

    private static final String APPOINTMENT_SELECT = """
            select a.id, a.client_id, c.full_name as client_name, a.status, a.appointment_mode,
                   a.scheduled_start, a.scheduled_end, a.address_snapshot, a.notes,
                   a.subtotal_amount, a.travel_fee, a.total_amount, a.completed_at,
                   a.cancelled_at, a.cancel_reason, a.created_at, a.updated_at
            from appointments a
            join clients c on c.id = a.client_id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<AppointmentRecord> appointmentRowMapper = (rs, rowNum) -> new AppointmentRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("client_id", UUID.class),
            rs.getString("client_name"),
            rs.getString("status"),
            rs.getString("appointment_mode"),
            rs.getObject("scheduled_start", OffsetDateTime.class),
            rs.getObject("scheduled_end", OffsetDateTime.class),
            rs.getString("address_snapshot"),
            rs.getString("notes"),
            rs.getBigDecimal("subtotal_amount"),
            rs.getBigDecimal("travel_fee"),
            rs.getBigDecimal("total_amount"),
            rs.getObject("completed_at", OffsetDateTime.class),
            rs.getObject("cancelled_at", OffsetDateTime.class),
            rs.getString("cancel_reason"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("updated_at", OffsetDateTime.class)
    );

    private final RowMapper<AppointmentItemRecord> itemRowMapper = (rs, rowNum) -> new AppointmentItemRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("appointment_id", UUID.class),
            rs.getObject("service_id", UUID.class),
            rs.getString("service_name_snapshot"),
            rs.getBigDecimal("unit_price_snapshot"),
            rs.getInt("duration_snapshot_minutes"),
            rs.getBoolean("is_touch_up"),
            rs.getBigDecimal("discount_amount"),
            rs.getBigDecimal("final_price")
    );

    public AppointmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AppointmentRecord> findBetween(OffsetDateTime from, OffsetDateTime to) {
        return jdbcTemplate.query(
                APPOINTMENT_SELECT + " where a.scheduled_start >= ? and a.scheduled_start < ? order by a.scheduled_start asc",
                appointmentRowMapper,
                from,
                to
        );
    }

    public Optional<AppointmentRecord> findById(UUID appointmentId) {
        return jdbcTemplate.query(
                APPOINTMENT_SELECT + " where a.id = ? limit 1",
                appointmentRowMapper,
                appointmentId
        ).stream().findFirst();
    }

    public List<AppointmentItemRecord> findItemsByAppointmentId(UUID appointmentId) {
        return jdbcTemplate.query(
                """
                select id, appointment_id, service_id, service_name_snapshot, unit_price_snapshot,
                       duration_snapshot_minutes, is_touch_up, discount_amount, final_price
                from appointment_items
                where appointment_id = ?
                order by service_name_snapshot asc
                """,
                itemRowMapper,
                appointmentId
        );
    }

    public List<AppointmentItemRecord> findItemsByAppointmentIds(List<UUID> appointmentIds) {
        if (appointmentIds.isEmpty()) {
            return List.of();
        }

        String placeholders = appointmentIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        List<Object> params = new ArrayList<>(appointmentIds);
        return jdbcTemplate.query(
                """
                select id, appointment_id, service_id, service_name_snapshot, unit_price_snapshot,
                       duration_snapshot_minutes, is_touch_up, discount_amount, final_price
                from appointment_items
                where appointment_id in (
                """ + placeholders + ") order by appointment_id, service_name_snapshot asc",
                itemRowMapper,
                params.toArray()
        );
    }

    public void insertAppointment(AppointmentRecord appointmentRecord) {
        jdbcTemplate.update(
                """
                insert into appointments (
                    id, client_id, status, appointment_mode, scheduled_start, scheduled_end,
                    address_snapshot, notes, subtotal_amount, travel_fee, total_amount,
                    completed_at, cancelled_at, cancel_reason, created_at, updated_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                appointmentRecord.id(),
                appointmentRecord.clientId(),
                appointmentRecord.status(),
                appointmentRecord.appointmentMode(),
                appointmentRecord.scheduledStart(),
                appointmentRecord.scheduledEnd(),
                appointmentRecord.addressSnapshot(),
                appointmentRecord.notes(),
                appointmentRecord.subtotalAmount(),
                appointmentRecord.travelFee(),
                appointmentRecord.totalAmount(),
                appointmentRecord.completedAt(),
                appointmentRecord.cancelledAt(),
                appointmentRecord.cancelReason(),
                appointmentRecord.createdAt(),
                appointmentRecord.updatedAt()
        );
    }

    public void insertItems(List<AppointmentItemRecord> items) {
        for (AppointmentItemRecord item : items) {
            jdbcTemplate.update(
                    """
                    insert into appointment_items (
                        id, appointment_id, service_id, service_name_snapshot, unit_price_snapshot,
                        duration_snapshot_minutes, is_touch_up, discount_amount, final_price
                    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    item.id(),
                    item.appointmentId(),
                    item.serviceId(),
                    item.serviceNameSnapshot(),
                    item.unitPriceSnapshot(),
                    item.durationSnapshotMinutes(),
                    item.isTouchUp(),
                    item.discountAmount(),
                    item.finalPrice()
            );
        }
    }

    public void updateAppointment(AppointmentRecord appointmentRecord) {
        jdbcTemplate.update(
                """
                update appointments
                set client_id = ?, status = ?, appointment_mode = ?, scheduled_start = ?, scheduled_end = ?,
                    address_snapshot = ?, notes = ?, subtotal_amount = ?, travel_fee = ?, total_amount = ?,
                    completed_at = ?, cancelled_at = ?, cancel_reason = ?, updated_at = ?
                where id = ?
                """,
                appointmentRecord.clientId(),
                appointmentRecord.status(),
                appointmentRecord.appointmentMode(),
                appointmentRecord.scheduledStart(),
                appointmentRecord.scheduledEnd(),
                appointmentRecord.addressSnapshot(),
                appointmentRecord.notes(),
                appointmentRecord.subtotalAmount(),
                appointmentRecord.travelFee(),
                appointmentRecord.totalAmount(),
                appointmentRecord.completedAt(),
                appointmentRecord.cancelledAt(),
                appointmentRecord.cancelReason(),
                appointmentRecord.updatedAt(),
                appointmentRecord.id()
        );
    }

    public void deleteItemsByAppointmentId(UUID appointmentId) {
        jdbcTemplate.update("delete from appointment_items where appointment_id = ?", appointmentId);
    }

    public void updateStatus(
            UUID appointmentId,
            String status,
            OffsetDateTime completedAt,
            OffsetDateTime cancelledAt,
            String cancelReason,
            OffsetDateTime updatedAt
    ) {
        jdbcTemplate.update(
                """
                update appointments
                set status = ?, completed_at = ?, cancelled_at = ?, cancel_reason = ?, updated_at = ?
                where id = ?
                """,
                status,
                completedAt,
                cancelledAt,
                cancelReason,
                updatedAt,
                appointmentId
        );
    }

    public void deleteAppointment(UUID appointmentId) {
        jdbcTemplate.update("delete from appointments where id = ?", appointmentId);
    }

    public boolean existsOverlap(OffsetDateTime start, OffsetDateTime end, UUID excludedAppointmentId) {
        String sql = """
                select exists(
                    select 1
                    from appointments
                    where status = 'CONFIRMED'
                      and scheduled_start < ?
                      and scheduled_end > ?
                """;

        List<Object> params = new ArrayList<>();
        params.add(end);
        params.add(start);

        if (excludedAppointmentId != null) {
            sql += " and id <> ?";
            params.add(excludedAppointmentId);
        }

        sql += ")";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, params.toArray());
        return Boolean.TRUE.equals(exists);
    }
}
