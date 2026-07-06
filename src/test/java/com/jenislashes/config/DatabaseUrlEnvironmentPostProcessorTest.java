package com.jenislashes.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseUrlEnvironmentPostProcessorTest {

    @Test
    void normalize_shouldConvertPostgresUrlToJdbcUrlAndCredentials() {
        DatabaseUrlEnvironmentPostProcessor.NormalizedDatabaseUrl normalized =
                DatabaseUrlEnvironmentPostProcessor.normalize(
                        "postgresql://jeni_user:secret@ep-small-field.us-east-1.aws.neon.tech/neondb?sslmode=require"
                );

        assertThat(normalized.jdbcUrl())
                .isEqualTo("jdbc:postgresql://ep-small-field.us-east-1.aws.neon.tech/neondb?sslmode=require");
        assertThat(normalized.username()).isEqualTo("jeni_user");
        assertThat(normalized.password()).isEqualTo("secret");
    }

    @Test
    void normalize_shouldSupportRailwayPostgresScheme() {
        DatabaseUrlEnvironmentPostProcessor.NormalizedDatabaseUrl normalized =
                DatabaseUrlEnvironmentPostProcessor.normalize(
                        "postgres://postgres:password@postgres.railway.internal:5432/railway"
                );

        assertThat(normalized.jdbcUrl())
                .isEqualTo("jdbc:postgresql://postgres.railway.internal:5432/railway");
        assertThat(normalized.username()).isEqualTo("postgres");
        assertThat(normalized.password()).isEqualTo("password");
    }
}
