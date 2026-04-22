package com.kpnquest.shared;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.MSSQLServerContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.Map;

public class MssqlContainerExtension {

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
        try (Connection conn = DriverManager.getConnection(
                CONTAINER.getJdbcUrl(), CONTAINER.getUsername(), CONTAINER.getPassword());
             Statement stmt = conn.createStatement()) {
            stmt.execute("IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'kpnquest') CREATE DATABASE kpnquest");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create kpnquest database", e);
        }
    }

    private static void runMigrations() {
        Flyway.configure()
            .dataSource(jdbcUrl(), CONTAINER.getUsername(), CONTAINER.getPassword())
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
            .migrate();
    }

    /**
     * Creates a test player by username and returns their generated ID.
     * If the player already exists, returns their existing ID.
     */
    public static long createTestPlayer(String username) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl(), CONTAINER.getUsername(), CONTAINER.getPassword())) {
            // Check if already exists
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM players WHERE username = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
            // Insert new player
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO players (username, is_admin, created_at, updated_at) VALUES (?, 0, GETDATE(), GETDATE()); SELECT SCOPE_IDENTITY()")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test player: " + username, e);
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

    protected static String jdbcUrl() {
        return "jdbc:sqlserver://" + CONTAINER.getHost() + ":" + CONTAINER.getMappedPort(1433)
            + ";databaseName=kpnquest;encrypt=false;trustServerCertificate=true";
    }
}