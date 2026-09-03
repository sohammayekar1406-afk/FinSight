package com.ledgerlens.controller;

import com.ledgerlens.config.AiProperties;
import com.ledgerlens.dto.HealthResponseDto;
import javax.sql.DataSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final DataSource dataSource;
    private final AiProperties aiProperties;

    public HealthController(DataSource dataSource, AiProperties aiProperties) {
        this.dataSource = dataSource;
        this.aiProperties = aiProperties;
    }

    @GetMapping("/health")
    public HealthResponseDto getHealth() {
        String dbStatus = "UP";
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(2)) {
                dbStatus = "DOWN";
            }
        } catch (Exception e) {
            dbStatus = "DOWN";
        }

        String aiStatus = (aiProperties.isEnabled() && aiProperties.getApiKey() != null && !aiProperties.getApiKey().isBlank())
                ? "CONFIGURED"
                : "RULE_BASED_FALLBACK_ACTIVE";

        String overallStatus = "UP".equals(dbStatus) ? "UP" : "DEGRADED";

        return HealthResponseDto.builder()
                .status(overallStatus)
                .databaseStatus(dbStatus)
                .reconciliationEngineStatus("READY")
                .aiProviderStatus(aiStatus)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
