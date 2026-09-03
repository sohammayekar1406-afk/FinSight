package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.SettlementStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class SettlementResponseDto {
    private UUID id;
    private String settlementId;
    private String merchantId;
    private BigDecimal grossAmount;
    private BigDecimal totalRefundAmount;
    private BigDecimal totalFeeAmount;
    private BigDecimal totalTaxAmount;
    private BigDecimal totalAdjustmentAmount;
    private BigDecimal netAmount;
    private BigDecimal actualSettledAmount;
    private SettlementStatus status;
    private String utr;
    private OffsetDateTime settledAt;
    private OffsetDateTime createdAt;

    public SettlementResponseDto() {}

    public SettlementResponseDto(UUID id, String settlementId, String merchantId, BigDecimal grossAmount, BigDecimal totalRefundAmount, BigDecimal totalFeeAmount, BigDecimal totalTaxAmount, BigDecimal totalAdjustmentAmount, BigDecimal netAmount, BigDecimal actualSettledAmount, SettlementStatus status, String utr, OffsetDateTime settledAt, OffsetDateTime createdAt) {
        this.id = id;
        this.settlementId = settlementId;
        this.merchantId = merchantId;
        this.grossAmount = grossAmount;
        this.totalRefundAmount = totalRefundAmount;
        this.totalFeeAmount = totalFeeAmount;
        this.totalTaxAmount = totalTaxAmount;
        this.totalAdjustmentAmount = totalAdjustmentAmount;
        this.netAmount = netAmount;
        this.actualSettledAmount = actualSettledAmount;
        this.status = status;
        this.utr = utr;
        this.settledAt = settledAt;
        this.createdAt = createdAt;
    }

    public static SettlementResponseDtoBuilder builder() { return new SettlementResponseDtoBuilder(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSettlementId() { return settlementId; }
    public void setSettlementId(String settlementId) { this.settlementId = settlementId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public BigDecimal getTotalRefundAmount() { return totalRefundAmount; }
    public void setTotalRefundAmount(BigDecimal totalRefundAmount) { this.totalRefundAmount = totalRefundAmount; }
    public BigDecimal getTotalFeeAmount() { return totalFeeAmount; }
    public void setTotalFeeAmount(BigDecimal totalFeeAmount) { this.totalFeeAmount = totalFeeAmount; }
    public BigDecimal getTotalTaxAmount() { return totalTaxAmount; }
    public void setTotalTaxAmount(BigDecimal totalTaxAmount) { this.totalTaxAmount = totalTaxAmount; }
    public BigDecimal getTotalAdjustmentAmount() { return totalAdjustmentAmount; }
    public void setTotalAdjustmentAmount(BigDecimal totalAdjustmentAmount) { this.totalAdjustmentAmount = totalAdjustmentAmount; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public BigDecimal getActualSettledAmount() { return actualSettledAmount; }
    public void setActualSettledAmount(BigDecimal actualSettledAmount) { this.actualSettledAmount = actualSettledAmount; }
    public SettlementStatus getStatus() { return status; }
    public void setStatus(SettlementStatus status) { this.status = status; }
    public String getUtr() { return utr; }
    public void setUtr(String utr) { this.utr = utr; }
    public OffsetDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(OffsetDateTime settledAt) { this.settledAt = settledAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public static class SettlementResponseDtoBuilder {
        private UUID id;
        private String settlementId;
        private String merchantId;
        private BigDecimal grossAmount;
        private BigDecimal totalRefundAmount;
        private BigDecimal totalFeeAmount;
        private BigDecimal totalTaxAmount;
        private BigDecimal totalAdjustmentAmount;
        private BigDecimal netAmount;
        private BigDecimal actualSettledAmount;
        private SettlementStatus status;
        private String utr;
        private OffsetDateTime settledAt;
        private OffsetDateTime createdAt;

        public SettlementResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public SettlementResponseDtoBuilder settlementId(String settlementId) { this.settlementId = settlementId; return this; }
        public SettlementResponseDtoBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public SettlementResponseDtoBuilder grossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; return this; }
        public SettlementResponseDtoBuilder totalRefundAmount(BigDecimal totalRefundAmount) { this.totalRefundAmount = totalRefundAmount; return this; }
        public SettlementResponseDtoBuilder totalFeeAmount(BigDecimal totalFeeAmount) { this.totalFeeAmount = totalFeeAmount; return this; }
        public SettlementResponseDtoBuilder totalTaxAmount(BigDecimal totalTaxAmount) { this.totalTaxAmount = totalTaxAmount; return this; }
        public SettlementResponseDtoBuilder totalAdjustmentAmount(BigDecimal totalAdjustmentAmount) { this.totalAdjustmentAmount = totalAdjustmentAmount; return this; }
        public SettlementResponseDtoBuilder netAmount(BigDecimal netAmount) { this.netAmount = netAmount; return this; }
        public SettlementResponseDtoBuilder actualSettledAmount(BigDecimal actualSettledAmount) { this.actualSettledAmount = actualSettledAmount; return this; }
        public SettlementResponseDtoBuilder status(SettlementStatus status) { this.status = status; return this; }
        public SettlementResponseDtoBuilder utr(String utr) { this.utr = utr; return this; }
        public SettlementResponseDtoBuilder settledAt(OffsetDateTime settledAt) { this.settledAt = settledAt; return this; }
        public SettlementResponseDtoBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public SettlementResponseDto build() {
            return new SettlementResponseDto(id, settlementId, merchantId, grossAmount, totalRefundAmount, totalFeeAmount, totalTaxAmount, totalAdjustmentAmount, netAmount, actualSettledAmount, status, utr, settledAt, createdAt);
        }
    }
}
