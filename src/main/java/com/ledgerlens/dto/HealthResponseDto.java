package com.ledgerlens.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthResponseDto {
    private String status;
    private String databaseStatus;
    private String reconciliationEngineStatus;
    private String aiProviderStatus;
    private OffsetDateTime timestamp;

    public HealthResponseDto() {}

    public HealthResponseDto(String status) {
        this.status = status;
        this.timestamp = OffsetDateTime.now();
    }

    public HealthResponseDto(String status, String databaseStatus, String reconciliationEngineStatus, String aiProviderStatus, OffsetDateTime timestamp) {
        this.status = status;
        this.databaseStatus = databaseStatus;
        this.reconciliationEngineStatus = reconciliationEngineStatus;
        this.aiProviderStatus = aiProviderStatus;
        this.timestamp = timestamp;
    }

    public static HealthResponseDtoBuilder builder() { return new HealthResponseDtoBuilder(); }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDatabaseStatus() { return databaseStatus; }
    public void setDatabaseStatus(String databaseStatus) { this.databaseStatus = databaseStatus; }
    public String getReconciliationEngineStatus() { return reconciliationEngineStatus; }
    public void setReconciliationEngineStatus(String reconciliationEngineStatus) { this.reconciliationEngineStatus = reconciliationEngineStatus; }
    public String getAiProviderStatus() { return aiProviderStatus; }
    public void setAiProviderStatus(String aiProviderStatus) { this.aiProviderStatus = aiProviderStatus; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

    public static class HealthResponseDtoBuilder {
        private String status;
        private String databaseStatus;
        private String reconciliationEngineStatus;
        private String aiProviderStatus;
        private OffsetDateTime timestamp;

        public HealthResponseDtoBuilder status(String status) { this.status = status; return this; }
        public HealthResponseDtoBuilder databaseStatus(String databaseStatus) { this.databaseStatus = databaseStatus; return this; }
        public HealthResponseDtoBuilder reconciliationEngineStatus(String reconciliationEngineStatus) { this.reconciliationEngineStatus = reconciliationEngineStatus; return this; }
        public HealthResponseDtoBuilder aiProviderStatus(String aiProviderStatus) { this.aiProviderStatus = aiProviderStatus; return this; }
        public HealthResponseDtoBuilder timestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; return this; }

        public HealthResponseDto build() {
            return new HealthResponseDto(status, databaseStatus, reconciliationEngineStatus, aiProviderStatus, timestamp);
        }
    }
}

