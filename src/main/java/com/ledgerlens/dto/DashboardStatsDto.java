package com.ledgerlens.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardStatsDto {

    private long totalTransactions;
    private long successfulPayments;
    private long refundsCount;
    private BigDecimal totalRefundsAmount;
    private BigDecimal totalFeesAmount;
    private long totalSettlements;
    private BigDecimal totalSettlementsAmount;
    private BigDecimal unreconciledAmount;
    private long openExceptionsCount;
    private Map<String, Long> severityBreakdown;
    private Map<String, Long> settlementOverview;
    private List<FinancialExceptionResponseDto> recentExceptions;
    private boolean hasReconciled;
    private java.time.OffsetDateTime lastReconciledAt;

    public DashboardStatsDto() {}

    public DashboardStatsDto(long totalTransactions, long successfulPayments, long refundsCount, BigDecimal totalRefundsAmount, BigDecimal totalFeesAmount, long totalSettlements, BigDecimal totalSettlementsAmount, BigDecimal unreconciledAmount, long openExceptionsCount, Map<String, Long> severityBreakdown, Map<String, Long> settlementOverview, List<FinancialExceptionResponseDto> recentExceptions) {
        this(totalTransactions, successfulPayments, refundsCount, totalRefundsAmount, totalFeesAmount, totalSettlements, totalSettlementsAmount, unreconciledAmount, openExceptionsCount, severityBreakdown, settlementOverview, recentExceptions, false, null);
    }

    public DashboardStatsDto(long totalTransactions, long successfulPayments, long refundsCount, BigDecimal totalRefundsAmount, BigDecimal totalFeesAmount, long totalSettlements, BigDecimal totalSettlementsAmount, BigDecimal unreconciledAmount, long openExceptionsCount, Map<String, Long> severityBreakdown, Map<String, Long> settlementOverview, List<FinancialExceptionResponseDto> recentExceptions, boolean hasReconciled, java.time.OffsetDateTime lastReconciledAt) {
        this.totalTransactions = totalTransactions;
        this.successfulPayments = successfulPayments;
        this.refundsCount = refundsCount;
        this.totalRefundsAmount = totalRefundsAmount;
        this.totalFeesAmount = totalFeesAmount;
        this.totalSettlements = totalSettlements;
        this.totalSettlementsAmount = totalSettlementsAmount;
        this.unreconciledAmount = unreconciledAmount;
        this.openExceptionsCount = openExceptionsCount;
        this.severityBreakdown = severityBreakdown;
        this.settlementOverview = settlementOverview;
        this.recentExceptions = recentExceptions;
        this.hasReconciled = hasReconciled;
        this.lastReconciledAt = lastReconciledAt;
    }

    public static DashboardStatsDtoBuilder builder() { return new DashboardStatsDtoBuilder(); }

    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }

    public long getSuccessfulPayments() { return successfulPayments; }
    public void setSuccessfulPayments(long successfulPayments) { this.successfulPayments = successfulPayments; }

    public long getRefundsCount() { return refundsCount; }
    public void setRefundsCount(long refundsCount) { this.refundsCount = refundsCount; }

    public BigDecimal getTotalRefundsAmount() { return totalRefundsAmount; }
    public void setTotalRefundsAmount(BigDecimal totalRefundsAmount) { this.totalRefundsAmount = totalRefundsAmount; }

    public BigDecimal getTotalFeesAmount() { return totalFeesAmount; }
    public void setTotalFeesAmount(BigDecimal totalFeesAmount) { this.totalFeesAmount = totalFeesAmount; }

    public long getTotalSettlements() { return totalSettlements; }
    public void setTotalSettlements(long totalSettlements) { this.totalSettlements = totalSettlements; }

    public BigDecimal getTotalSettlementsAmount() { return totalSettlementsAmount; }
    public void setTotalSettlementsAmount(BigDecimal totalSettlementsAmount) { this.totalSettlementsAmount = totalSettlementsAmount; }

    public BigDecimal getUnreconciledAmount() { return unreconciledAmount; }
    public void setUnreconciledAmount(BigDecimal unreconciledAmount) { this.unreconciledAmount = unreconciledAmount; }

    public long getOpenExceptionsCount() { return openExceptionsCount; }
    public void setOpenExceptionsCount(long openExceptionsCount) { this.openExceptionsCount = openExceptionsCount; }

    public Map<String, Long> getSeverityBreakdown() { return severityBreakdown; }
    public void setSeverityBreakdown(Map<String, Long> severityBreakdown) { this.severityBreakdown = severityBreakdown; }

    public Map<String, Long> getSettlementOverview() { return settlementOverview; }
    public void setSettlementOverview(Map<String, Long> settlementOverview) { this.settlementOverview = settlementOverview; }

    public List<FinancialExceptionResponseDto> getRecentExceptions() { return recentExceptions; }
    public void setRecentExceptions(List<FinancialExceptionResponseDto> recentExceptions) { this.recentExceptions = recentExceptions; }

    public boolean isHasReconciled() { return hasReconciled; }
    public void setHasReconciled(boolean hasReconciled) { this.hasReconciled = hasReconciled; }

    public java.time.OffsetDateTime getLastReconciledAt() { return lastReconciledAt; }
    public void setLastReconciledAt(java.time.OffsetDateTime lastReconciledAt) { this.lastReconciledAt = lastReconciledAt; }

    public static class DashboardStatsDtoBuilder {
        private long totalTransactions;
        private long successfulPayments;
        private long refundsCount;
        private BigDecimal totalRefundsAmount = BigDecimal.ZERO;
        private BigDecimal totalFeesAmount = BigDecimal.ZERO;
        private long totalSettlements;
        private BigDecimal totalSettlementsAmount = BigDecimal.ZERO;
        private BigDecimal unreconciledAmount = BigDecimal.ZERO;
        private long openExceptionsCount;
        private Map<String, Long> severityBreakdown = Map.of();
        private Map<String, Long> settlementOverview = Map.of();
        private List<FinancialExceptionResponseDto> recentExceptions = List.of();
        private boolean hasReconciled;
        private java.time.OffsetDateTime lastReconciledAt;

        public DashboardStatsDtoBuilder totalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; return this; }
        public DashboardStatsDtoBuilder successfulPayments(long successfulPayments) { this.successfulPayments = successfulPayments; return this; }
        public DashboardStatsDtoBuilder refundsCount(long refundsCount) { this.refundsCount = refundsCount; return this; }
        public DashboardStatsDtoBuilder totalRefundsAmount(BigDecimal totalRefundsAmount) { this.totalRefundsAmount = totalRefundsAmount; return this; }
        public DashboardStatsDtoBuilder totalFeesAmount(BigDecimal totalFeesAmount) { this.totalFeesAmount = totalFeesAmount; return this; }
        public DashboardStatsDtoBuilder totalSettlements(long totalSettlements) { this.totalSettlements = totalSettlements; return this; }
        public DashboardStatsDtoBuilder totalSettlementsAmount(BigDecimal totalSettlementsAmount) { this.totalSettlementsAmount = totalSettlementsAmount; return this; }
        public DashboardStatsDtoBuilder unreconciledAmount(BigDecimal unreconciledAmount) { this.unreconciledAmount = unreconciledAmount; return this; }
        public DashboardStatsDtoBuilder openExceptionsCount(long openExceptionsCount) { this.openExceptionsCount = openExceptionsCount; return this; }
        public DashboardStatsDtoBuilder severityBreakdown(Map<String, Long> severityBreakdown) { this.severityBreakdown = severityBreakdown; return this; }
        public DashboardStatsDtoBuilder settlementOverview(Map<String, Long> settlementOverview) { this.settlementOverview = settlementOverview; return this; }
        public DashboardStatsDtoBuilder recentExceptions(List<FinancialExceptionResponseDto> recentExceptions) { this.recentExceptions = recentExceptions; return this; }
        public DashboardStatsDtoBuilder hasReconciled(boolean hasReconciled) { this.hasReconciled = hasReconciled; return this; }
        public DashboardStatsDtoBuilder lastReconciledAt(java.time.OffsetDateTime lastReconciledAt) { this.lastReconciledAt = lastReconciledAt; return this; }

        public DashboardStatsDto build() {
            return new DashboardStatsDto(totalTransactions, successfulPayments, refundsCount, totalRefundsAmount, totalFeesAmount, totalSettlements, totalSettlementsAmount, unreconciledAmount, openExceptionsCount, severityBreakdown, settlementOverview, recentExceptions, hasReconciled, lastReconciledAt);
        }
    }
}
