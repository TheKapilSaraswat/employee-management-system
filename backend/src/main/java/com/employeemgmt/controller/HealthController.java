package com.employeemgmt.controller;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final ObjectProvider<DataSource> dataSourceProvider;

    public HealthController(ObjectProvider<DataSource> dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "timestamp", Instant.now().toString()));
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        boolean databaseUp = false;
        try {
            DataSource dataSource = dataSourceProvider.getIfAvailable();
            if (dataSource != null) {
                try (var connection = dataSource.getConnection()) {
                    databaseUp = connection.isValid(2);
                }
            }
        } catch (Exception e) {
            databaseUp = false;
        }

        if (databaseUp) {
            return ResponseEntity.ok(Map.of("status", "ready", "database", "up"));
        }
        return ResponseEntity.status(503).body(Map.of("status", "not-ready", "database", "down"));
    }
}
