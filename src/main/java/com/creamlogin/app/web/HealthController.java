package com.creamlogin.app.web;

import java.util.Map;
import javax.sql.DataSource;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  private final JdbcTemplate jdbcTemplate;

  public HealthController(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @GetMapping("/api/health")
  public ResponseEntity<Map<String, String>> health() {
    try {
      jdbcTemplate.queryForObject("SELECT 1", Integer.class);
      return ResponseEntity.ok(Map.of("status", "ok", "db", "connected"));
    } catch (Exception ex) {
      return ResponseEntity.status(503).body(Map.of("status", "error", "db", "disconnected"));
    }
  }
}
