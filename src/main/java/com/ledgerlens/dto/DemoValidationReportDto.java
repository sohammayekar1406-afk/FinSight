package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.entity.enums.ExceptionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Structured report produced by DemoValidationService.
 * Captures the end-to-end workflow verification result for Step 13.
 */
public class DemoValidationReportDto {

    private String overallStatus;
    private OffsetDateTime generatedAt;
    private SeedSummary seedSummary;
    private ReconciliationSummary reconciliationSummary;
    private ExceptionsSummary exceptionsSummary;
    private InvestigationsSummary investigationsSummary;
    private AuditSummary auditSummary;
    private DashboardStatsDto dashboardStats;
    private List<StepResult> stepsVerified;

    public DemoValidationReportDto() {}

    private DemoValidationReportDto(Builder b) {
        this.overallStatus = b.overallStatus;
        this.generatedAt = b.generatedAt;
        this.seedSummary = b.seedSummary;
        this.reconciliationSummary = b.reconciliationSummary;
        this.exceptionsSummary = b.exceptionsSummary;
        this.investigationsSummary = b.investigationsSummary;
        this.auditSummary = b.auditSummary;
        this.dashboardStats = b.dashboardStats;
        this.stepsVerified = b.stepsVerified;
    }

    public static Builder builder() { return new Builder(); }

    public String getOverallStatus() { return overallStatus; }
    public void setOverallStatus(String s) { this.overallStatus = s; }
    public OffsetDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(OffsetDateTime t) { this.generatedAt = t; }
    public SeedSummary getSeedSummary() { return seedSummary; }
    public void setSeedSummary(SeedSummary s) { this.seedSummary = s; }
    public ReconciliationSummary getReconciliationSummary() { return reconciliationSummary; }
    public void setReconciliationSummary(ReconciliationSummary r) { this.reconciliationSummary = r; }
    public ExceptionsSummary getExceptionsSummary() { return exceptionsSummary; }
    public void setExceptionsSummary(ExceptionsSummary e) { this.exceptionsSummary = e; }
    public InvestigationsSummary getInvestigationsSummary() { return investigationsSummary; }
    public void setInvestigationsSummary(InvestigationsSummary i) { this.investigationsSummary = i; }
    public AuditSummary getAuditSummary() { return auditSummary; }
    public void setAuditSummary(AuditSummary a) { this.auditSummary = a; }
    public DashboardStatsDto getDashboardStats() { return dashboardStats; }
    public void setDashboardStats(DashboardStatsDto d) { this.dashboardStats = d; }
    public List<StepResult> getStepsVerified() { return stepsVerified; }
    public void setStepsVerified(List<StepResult> s) { this.stepsVerified = s; }

    // -- Nested DTOs -----------------------------------------------------------

    public static class SeedSummary {
        private int ordersSeeded;
        private int paymentsSeeded;
        private int refundsSeeded;
        private int feesSeeded;
        private int settlementsSeeded;
        private int adjustmentsSeeded;

        public SeedSummary() {}
        public SeedSummary(int orders, int payments, int refunds, int fees, int settlements, int adjustments) {
            this.ordersSeeded = orders; this.paymentsSeeded = payments;
            this.refundsSeeded = refunds; this.feesSeeded = fees;
            this.settlementsSeeded = settlements; this.adjustmentsSeeded = adjustments;
        }

        public int getOrdersSeeded() { return ordersSeeded; }
        public int getPaymentsSeeded() { return paymentsSeeded; }
        public int getRefundsSeeded() { return refundsSeeded; }
        public int getFeesSeeded() { return feesSeeded; }
        public int getSettlementsSeeded() { return settlementsSeeded; }
        public int getAdjustmentsSeeded() { return adjustmentsSeeded; }
    }

    public static class ReconciliationSummary {
        private int recordsChecked;
        private int exceptionsCreated;
        private int failedChecks;
        private int successfulChecks;
        private BigDecimal totalDiscrepancyAmount;

        public ReconciliationSummary() {}
        public ReconciliationSummary(int recordsChecked, int exceptionsCreated, int failedChecks, int successfulChecks, BigDecimal totalDiscrepancyAmount) {
            this.recordsChecked = recordsChecked; this.exceptionsCreated = exceptionsCreated;
            this.failedChecks = failedChecks; this.successfulChecks = successfulChecks;
            this.totalDiscrepancyAmount = totalDiscrepancyAmount;
        }

        public int getRecordsChecked() { return recordsChecked; }
        public int getExceptionsCreated() { return exceptionsCreated; }
        public int getFailedChecks() { return failedChecks; }
        public int getSuccessfulChecks() { return successfulChecks; }
        public BigDecimal getTotalDiscrepancyAmount() { return totalDiscrepancyAmount; }
    }

    public static class ExceptionsSummary {
        private int totalExceptions;
        private int openExceptions;
        private int investigatingExceptions;
        private int resolvedExceptions;
        private Map<String, Long> byType;
        private Map<String, Long> bySeverity;
        private List<ExceptionBrief> exceptions;

        public ExceptionsSummary() {}

        public int getTotalExceptions() { return totalExceptions; }
        public void setTotalExceptions(int v) { this.totalExceptions = v; }
        public int getOpenExceptions() { return openExceptions; }
        public void setOpenExceptions(int v) { this.openExceptions = v; }
        public int getInvestigatingExceptions() { return investigatingExceptions; }
        public void setInvestigatingExceptions(int v) { this.investigatingExceptions = v; }
        public int getResolvedExceptions() { return resolvedExceptions; }
        public void setResolvedExceptions(int v) { this.resolvedExceptions = v; }
        public Map<String, Long> getByType() { return byType; }
        public void setByType(Map<String, Long> v) { this.byType = v; }
        public Map<String, Long> getBySeverity() { return bySeverity; }
        public void setBySeverity(Map<String, Long> v) { this.bySeverity = v; }
        public List<ExceptionBrief> getExceptions() { return exceptions; }
        public void setExceptions(List<ExceptionBrief> v) { this.exceptions = v; }
    }

    public static class ExceptionBrief {
        private String exceptionId;
        private ExceptionType type;
        private ExceptionSeverity severity;
        private ExceptionStatus status;
        private BigDecimal discrepancyAmount;

        public ExceptionBrief() {}
        public ExceptionBrief(String exceptionId, ExceptionType type, ExceptionSeverity severity, ExceptionStatus status, BigDecimal discrepancyAmount) {
            this.exceptionId = exceptionId; this.type = type; this.severity = severity;
            this.status = status; this.discrepancyAmount = discrepancyAmount;
        }

        public String getExceptionId() { return exceptionId; }
        public ExceptionType getType() { return type; }
        public ExceptionSeverity getSeverity() { return severity; }
        public ExceptionStatus getStatus() { return status; }
        public BigDecimal getDiscrepancyAmount() { return discrepancyAmount; }
    }

    public static class InvestigationsSummary {
        private int totalInvestigated;
        private int aiInvestigations;
        private int fallbackInvestigations;
        private int autoResolved;
        private int pendingHumanReview;

        public InvestigationsSummary() {}
        public InvestigationsSummary(int totalInvestigated, int aiInvestigations, int fallbackInvestigations, int autoResolved, int pendingHumanReview) {
            this.totalInvestigated = totalInvestigated; this.aiInvestigations = aiInvestigations;
            this.fallbackInvestigations = fallbackInvestigations; this.autoResolved = autoResolved;
            this.pendingHumanReview = pendingHumanReview;
        }

        public int getTotalInvestigated() { return totalInvestigated; }
        public int getAiInvestigations() { return aiInvestigations; }
        public int getFallbackInvestigations() { return fallbackInvestigations; }
        public int getAutoResolved() { return autoResolved; }
        public int getPendingHumanReview() { return pendingHumanReview; }
    }

    public static class AuditSummary {
        private long totalAuditEntries;
        private boolean auditTrailIntact;

        public AuditSummary() {}
        public AuditSummary(long totalAuditEntries, boolean auditTrailIntact) {
            this.totalAuditEntries = totalAuditEntries;
            this.auditTrailIntact = auditTrailIntact;
        }

        public long getTotalAuditEntries() { return totalAuditEntries; }
        public boolean isAuditTrailIntact() { return auditTrailIntact; }
    }

    public static class StepResult {
        private int step;
        private String name;
        private String status;
        private String details;

        public StepResult() {}
        public StepResult(int step, String name, String status, String details) {
            this.step = step; this.name = name; this.status = status; this.details = details;
        }

        public int getStep() { return step; }
        public String getName() { return name; }
        public String getStatus() { return status; }
        public String getDetails() { return details; }
    }

    // -- Builder ---------------------------------------------------------------

    public static class Builder {
        private String overallStatus;
        private OffsetDateTime generatedAt;
        private SeedSummary seedSummary;
        private ReconciliationSummary reconciliationSummary;
        private ExceptionsSummary exceptionsSummary;
        private InvestigationsSummary investigationsSummary;
        private AuditSummary auditSummary;
        private DashboardStatsDto dashboardStats;
        private List<StepResult> stepsVerified;

        public Builder overallStatus(String v) { this.overallStatus = v; return this; }
        public Builder generatedAt(OffsetDateTime v) { this.generatedAt = v; return this; }
        public Builder seedSummary(SeedSummary v) { this.seedSummary = v; return this; }
        public Builder reconciliationSummary(ReconciliationSummary v) { this.reconciliationSummary = v; return this; }
        public Builder exceptionsSummary(ExceptionsSummary v) { this.exceptionsSummary = v; return this; }
        public Builder investigationsSummary(InvestigationsSummary v) { this.investigationsSummary = v; return this; }
        public Builder auditSummary(AuditSummary v) { this.auditSummary = v; return this; }
        public Builder dashboardStats(DashboardStatsDto v) { this.dashboardStats = v; return this; }
        public Builder stepsVerified(List<StepResult> v) { this.stepsVerified = v; return this; }

        public DemoValidationReportDto build() { return new DemoValidationReportDto(this); }
    }
}
