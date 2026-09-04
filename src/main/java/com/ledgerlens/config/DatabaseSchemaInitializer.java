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

                // Ensure exceptions check constraints include all ExceptionType enum values (including CURRENCY_MISMATCH)
                jdbcTemplate.execute("ALTER TABLE exceptions DROP CONSTRAINT IF EXISTS exceptions_exception_type_check");
                jdbcTemplate.execute("ALTER TABLE exceptions ADD CONSTRAINT exceptions_exception_type_check CHECK (" +
                        "exception_type IN ('AMOUNT_MISMATCH', 'MISSING_PAYMENT', 'MISSING_SETTLEMENT', 'UNEXPECTED_FEE', " +
                        "'DUPLICATE_TRANSACTION', 'DELAYED_SETTLEMENT', 'DISCREPANT_REFUND', 'UNKNOWN_TRANSACTION', " +
                        "'UNMATCHED_ADJUSTMENT', 'CURRENCY_MISMATCH', 'DATA_INCOMPLETE'))");
                jdbcTemplate.execute("ALTER TABLE exceptions DROP CONSTRAINT IF EXISTS exceptions_type_check");
                jdbcTemplate.execute("ALTER TABLE exceptions ADD CONSTRAINT exceptions_type_check CHECK (" +
                        "type IN ('AMOUNT_MISMATCH', 'MISSING_PAYMENT', 'MISSING_SETTLEMENT', 'UNEXPECTED_FEE', " +
                        "'DUPLICATE_TRANSACTION', 'DELAYED_SETTLEMENT', 'DISCREPANT_REFUND', 'UNKNOWN_TRANSACTION', " +
                        "'UNMATCHED_ADJUSTMENT', 'CURRENCY_MISMATCH', 'DATA_INCOMPLETE'))");
                log.info("Successfully ensured exceptions table check constraints cover all enum values for both columns.");
            } catch (Exception e) {
                log.warn("DatabaseSchemaInitializer non-fatal error: {}", e.getMessage());
            }
        };
    }
}
