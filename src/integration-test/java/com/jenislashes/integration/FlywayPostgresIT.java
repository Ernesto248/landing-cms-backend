package com.jenislashes.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class FlywayPostgresIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flyway_should_create_default_business_hours() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from business_hours",
                Integer.class
        );

        Boolean sundayClosed = jdbcTemplate.queryForObject(
                "select is_closed from business_hours where day_of_week = 7",
                Boolean.class
        );

        assertThat(count).isEqualTo(7);
        assertThat(sundayClosed).isTrue();
    }

    @Test
    void database_should_reject_overlapping_active_appointments() {
        UUID clientId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                insert into clients (id, full_name, created_at, updated_at)
                values (?, ?, now(), now())
                """,
                clientId,
                "Maria Perez"
        );

        insertAppointment(
                UUID.randomUUID(),
                clientId,
                OffsetDateTime.parse("2026-05-11T14:00:00Z"),
                OffsetDateTime.parse("2026-05-11T15:00:00Z")
        );

        assertThatThrownBy(() -> insertAppointment(
                UUID.randomUUID(),
                clientId,
                OffsetDateTime.parse("2026-05-11T14:30:00Z"),
                OffsetDateTime.parse("2026-05-11T15:30:00Z")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertAppointment(UUID appointmentId, UUID clientId, OffsetDateTime start, OffsetDateTime end) {
        jdbcTemplate.update(
                """
                insert into appointments (
                    id, client_id, status, appointment_mode, scheduled_start, scheduled_end,
                    subtotal_amount, travel_fee, total_amount, created_at, updated_at
                ) values (?, ?, 'CONFIRMED', 'STUDIO', ?, ?, ?, ?, ?, now(), now())
                """,
                appointmentId,
                clientId,
                start,
                end,
                new BigDecimal("300.00"),
                BigDecimal.ZERO,
                new BigDecimal("300.00")
        );
    }
}
