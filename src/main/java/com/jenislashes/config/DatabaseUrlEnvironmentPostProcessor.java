package com.jenislashes.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "databaseUrlNormalizer";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = firstTextValue(
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("DATABASE_PRIVATE_URL")
        );

        if (!StringUtils.hasText(databaseUrl) || databaseUrl.startsWith("jdbc:postgresql://")) {
            return;
        }

        if (!databaseUrl.startsWith("postgres://") && !databaseUrl.startsWith("postgresql://")) {
            return;
        }

        NormalizedDatabaseUrl normalized = normalize(databaseUrl);
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.url", normalized.jdbcUrl());

        if (StringUtils.hasText(normalized.username())
                && !hasAnyProperty(environment, "SPRING_DATASOURCE_USERNAME", "DATABASE_USERNAME")) {
            properties.put("spring.datasource.username", normalized.username());
        }

        if (StringUtils.hasText(normalized.password())
                && !hasAnyProperty(environment, "SPRING_DATASOURCE_PASSWORD", "DATABASE_PASSWORD")) {
            properties.put("spring.datasource.password", normalized.password());
        }

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    static NormalizedDatabaseUrl normalize(String databaseUrl) {
        URI uri = URI.create(databaseUrl);
        String schemeSpecificPart = databaseUrl.substring(databaseUrl.indexOf("://") + 3);
        String jdbcUrl = "jdbc:postgresql://" + stripUserInfo(schemeSpecificPart);
        String username = null;
        String password = null;
        String userInfo = uri.getUserInfo();

        if (StringUtils.hasText(userInfo)) {
            String[] parts = userInfo.split(":", 2);
            username = parts[0];
            if (parts.length > 1) {
                password = parts[1];
            }
        }

        return new NormalizedDatabaseUrl(jdbcUrl, username, password);
    }

    private static String stripUserInfo(String schemeSpecificPart) {
        int atIndex = schemeSpecificPart.indexOf('@');
        if (atIndex < 0) {
            return schemeSpecificPart;
        }
        return schemeSpecificPart.substring(atIndex + 1);
    }

    private static boolean hasAnyProperty(ConfigurableEnvironment environment, String... names) {
        for (String name : names) {
            if (StringUtils.hasText(environment.getProperty(name))) {
                return true;
            }
        }
        return false;
    }

    private static String firstTextValue(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    record NormalizedDatabaseUrl(String jdbcUrl, String username, String password) {
    }
}
