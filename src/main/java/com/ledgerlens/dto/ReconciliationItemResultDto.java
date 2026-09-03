package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.ExceptionType;

import java.math.BigDecimal;

public class ReconciliationItemResultDto {
    private String entityType;
    private String entityId;
    private String status; // PASSED, DISCREPANCY, SKIPPED
    private BigDecimal expectedAmount;
    private BigDecimal actualAmount;
    private BigDecimal discrepancyAmount;
    private ExceptionType exceptionType;
    private String message;

    public ReconciliationItemResultDto() {}

    public ReconciliationItemResultDto(String entityType, String entityId, String status, BigDecimal expectedAmount, BigDecimal actualAmount, BigDecimal discrepancyAmount, ExceptionType exceptionType, String message) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.status = status;
        this.expectedAmount = expectedAmount;
        this.actualAmount = actualAmount;
        this.discrepancyAmount = discrepancyAmount;
        this.exceptionType = exceptionType;
        this.message = message;
    }

    public static ReconciliationItemResultDtoBuilder builder() { return new ReconciliationItemResultDtoBuilder(); }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getExpectedAmount() { return expectedAmount; }
    public void setExpectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; }
    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }
    public BigDecimal getDiscrepancyAmount() { return discrepancyAmount; }
    public void setDiscrepancyAmount(BigDecimal discrepancyAmount) { this.discrepancyAmount = discrepancyAmount; }
    public ExceptionType getExceptionType() { return exceptionType; }
    public void setExceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public static class ReconciliationItemResultDtoBuilder {
        private String entityType;
        private String entityId;
        private String status;
        private BigDecimal expectedAmount;
        private BigDecimal actualAmount;
        private BigDecimal discrepancyAmount;
        private ExceptionType exceptionType;
        private String message;

        public ReconciliationItemResultDtoBuilder entityType(String entityType) { this.entityType = entityType; return this; }
        public ReconciliationItemResultDtoBuilder entityId(String entityId) { this.entityId = entityId; return this; }
        public ReconciliationItemResultDtoBuilder status(String status) { this.status = status; return this; }
        public ReconciliationItemResultDtoBuilder expectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; return this; }
        public ReconciliationItemResultDtoBuilder actualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; return this; }
        public ReconciliationItemResultDtoBuilder discrepancyAmount(BigDecimal discrepancyAmount) { this.discrepancyAmount = discrepancyAmount; return this; }
        public ReconciliationItemResultDtoBuilder exceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; return this; }
        public ReconciliationItemResultDtoBuilder message(String message) { this.message = message; return this; }

        public ReconciliationItemResultDto build() {
            return new ReconciliationItemResultDto(entityType, entityId, status, expectedAmount, actualAmount, discrepancyAmount, exceptionType, message);
        }
    }
}
