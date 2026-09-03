package com.ledgerlens.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class ReconciliationResultDto {
    private String reconciliationId;
    private int recordsChecked;
    private int exceptionsCreated;
    private int exceptionsAlreadyExisting;
    private int successfulChecks;
    private int failedChecks;
    private BigDecimal totalDiscrepancyAmount;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private List<ReconciliationItemResultDto> items;

    public ReconciliationResultDto() {}

    public ReconciliationResultDto(String reconciliationId, int recordsChecked, int exceptionsCreated, int exceptionsAlreadyExisting, int successfulChecks, int failedChecks, BigDecimal totalDiscrepancyAmount, OffsetDateTime startedAt, OffsetDateTime completedAt, List<ReconciliationItemResultDto> items) {
        this.reconciliationId = reconciliationId;
        this.recordsChecked = recordsChecked;
        this.exceptionsCreated = exceptionsCreated;
        this.exceptionsAlreadyExisting = exceptionsAlreadyExisting;
        this.successfulChecks = successfulChecks;
        this.failedChecks = failedChecks;
        this.totalDiscrepancyAmount = totalDiscrepancyAmount;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.items = items;
    }

    public static ReconciliationResultDtoBuilder builder() { return new ReconciliationResultDtoBuilder(); }

    public String getReconciliationId() { return reconciliationId; }
    public void setReconciliationId(String reconciliationId) { this.reconciliationId = reconciliationId; }
    public int getRecordsChecked() { return recordsChecked; }
    public void setRecordsChecked(int recordsChecked) { this.recordsChecked = recordsChecked; }
    public int getExceptionsCreated() { return exceptionsCreated; }
    public void setExceptionsCreated(int exceptionsCreated) { this.exceptionsCreated = exceptionsCreated; }
    public int getExceptionsAlreadyExisting() { return exceptionsAlreadyExisting; }
    public void setExceptionsAlreadyExisting(int exceptionsAlreadyExisting) { this.exceptionsAlreadyExisting = exceptionsAlreadyExisting; }
    public int getSuccessfulChecks() { return successfulChecks; }
    public void setSuccessfulChecks(int successfulChecks) { this.successfulChecks = successfulChecks; }
    public int getFailedChecks() { return failedChecks; }
    public void setFailedChecks(int failedChecks) { this.failedChecks = failedChecks; }
    public BigDecimal getTotalDiscrepancyAmount() { return totalDiscrepancyAmount; }
    public void setTotalDiscrepancyAmount(BigDecimal totalDiscrepancyAmount) { this.totalDiscrepancyAmount = totalDiscrepancyAmount; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
    public List<ReconciliationItemResultDto> getItems() { return items; }
    public void setItems(List<ReconciliationItemResultDto> items) { this.items = items; }

    public static class ReconciliationResultDtoBuilder {
        private String reconciliationId;
        private int recordsChecked;
        private int exceptionsCreated;
        private int exceptionsAlreadyExisting;
        private int successfulChecks;
        private int failedChecks;
        private BigDecimal totalDiscrepancyAmount;
        private OffsetDateTime startedAt;
        private OffsetDateTime completedAt;
        private List<ReconciliationItemResultDto> items;

        public ReconciliationResultDtoBuilder reconciliationId(String reconciliationId) { this.reconciliationId = reconciliationId; return this; }
        public ReconciliationResultDtoBuilder recordsChecked(int recordsChecked) { this.recordsChecked = recordsChecked; return this; }
        public ReconciliationResultDtoBuilder exceptionsCreated(int exceptionsCreated) { this.exceptionsCreated = exceptionsCreated; return this; }
        public ReconciliationResultDtoBuilder exceptionsAlreadyExisting(int exceptionsAlreadyExisting) { this.exceptionsAlreadyExisting = exceptionsAlreadyExisting; return this; }
        public ReconciliationResultDtoBuilder successfulChecks(int successfulChecks) { this.successfulChecks = successfulChecks; return this; }
        public ReconciliationResultDtoBuilder failedChecks(int failedChecks) { this.failedChecks = failedChecks; return this; }
        public ReconciliationResultDtoBuilder totalDiscrepancyAmount(BigDecimal totalDiscrepancyAmount) { this.totalDiscrepancyAmount = totalDiscrepancyAmount; return this; }
        public ReconciliationResultDtoBuilder startedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; return this; }
        public ReconciliationResultDtoBuilder completedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; return this; }
        public ReconciliationResultDtoBuilder items(List<ReconciliationItemResultDto> items) { this.items = items; return this; }

        public ReconciliationResultDto build() {
            return new ReconciliationResultDto(reconciliationId, recordsChecked, exceptionsCreated, exceptionsAlreadyExisting, successfulChecks, failedChecks, totalDiscrepancyAmount, startedAt, completedAt, items);
        }
    }
}
