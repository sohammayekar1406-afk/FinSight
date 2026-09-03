package com.ledgerlens.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);

    @Bean
    @Order(1)
    ApplicationRunner auditLogSchemaRunner(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS merchant_id VARCHAR(64) DEFAULT 'merch_default'");
                jdbcTemplate.execute("UPDATE audit_logs SET merchant_id = 'merch_default' WHERE merchant_id IS NULL");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_audit_logs_merchant_created ON audit_logs (merchant_id, created_at)");
                log.info("Successfully ensured audit_logs table has merchant_id column and index.");
            } catch (Exception e) {
                log.warn("DatabaseSchemaInitializer non-fatal error: {}", e.getMessage());
            }
        };
    }
}
