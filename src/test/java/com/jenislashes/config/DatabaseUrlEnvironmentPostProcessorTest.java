package com.jenislashes.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

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

    @Test
    void postProcessEnvironment_shouldExposeJdbcDatasourceProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgres://postgres:password@postgres.railway.internal:5432/railway");

        new DatabaseUrlEnvironmentPostProcessor().postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://postgres.railway.internal:5432/railway");
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("postgres");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("password");
    }
}
