package com.kpnquest.shared;

import org.testcontainers.containers.MSSQLServerContainer;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.Map;

public class MssqlContainerExtension {

    private static final String[] MIGRATION_FILES = {
        "db/migration/V1__create_players_table.sql",
        "db/migration/V2__create_missions_table.sql",
        "db/migration/V3__seed_missions.sql",
        "db/migration/V4__create_mission_completions_table.sql",
        "db/migration/V5__create_photos_table.sql",
        "db/migration/V6__create_game_states_table.sql"
    };

    public static final MSSQLServerContainer<?> CONTAINER =
        new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense()
            .withStartupTimeout(Duration.ofSeconds(90))
            .withReuse(true);

    static {
        CONTAINER.start();
        createDatabase();
        runMigrations();

        System.setProperty("datasources.default.url", jdbcUrl());
        System.setProperty("datasources.default.username", CONTAINER.getUsername());
        System.setProperty("datasources.default.password", CONTAINER.getPassword());
        System.setProperty("datasources.default.driver-class-name", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

    private static void createDatabase() {
        try (Connection conn = DriverManager.getConnection(CONTAINER.getJdbcUrl(), CONTAINER.getUsername(), CONTAINER.getPassword());
             Statement stmt = conn.createStatement()) {
            stmt.execute("IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'kpnquest') CREATE DATABASE kpnquest");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create kpnquest database", e);
        }
    }

    private static void runMigrations() {
        try (Connection conn = DriverManager.getConnection(jdbcUrl(), CONTAINER.getUsername(), CONTAINER.getPassword())) {
            // Skip if already migrated (container reuse)
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_CATALOG = 'kpnquest' AND TABLE_NAME = 'players'")) {
                rs.next();
                if (rs.getInt(1) > 0) return;
            }

            for (String file : MIGRATION_FILES) {
                try (InputStream stream = MssqlContainerExtension.class.getClassLoader().getResourceAsStream(file)) {
                    if (stream == null) throw new RuntimeException("Migration file not found: " + file);
                    String sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(sql);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to run migrations", e);
        }
    }

    protected static Map<String, String> datasourceProperties() {
        return Map.of(
            "datasources.default.url",               jdbcUrl(),
            "datasources.default.username",          CONTAINER.getUsername(),
            "datasources.default.password",          CONTAINER.getPassword(),
            "datasources.default.driver-class-name", "com.microsoft.sqlserver.jdbc.SQLServerDriver"
        );
    }

    private static String jdbcUrl() {
        return "jdbc:sqlserver://" + CONTAINER.getHost() + ":" + CONTAINER.getMappedPort(1433)
            + ";databaseName=kpnquest;encrypt=false;trustServerCertificate=true";
    }
}
