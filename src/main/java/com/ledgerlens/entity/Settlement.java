package com.ledgerlens.entity;

import com.ledgerlens.entity.enums.SettlementStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlements", indexes = {
    @Index(name = "idx_settlements_settlement_id", columnList = "settlement_id", unique = true),
    @Index(name = "idx_settlements_merchant_status", columnList = "merchant_id, status")
})
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "settlement_id", nullable = false, unique = true, length = 64)
    private String settlementId;

    @Column(name = "merchant_id", nullable = false, length = 64)
    private String merchantId;

    @Column(name = "gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "total_refund_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRefundAmount;

    @Column(name = "total_fee_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalFeeAmount;

    @Column(name = "total_tax_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalTaxAmount;

    @Column(name = "total_adjustment_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAdjustmentAmount;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "actual_settled_amount", precision = 15, scale = 2)
    private BigDecimal actualSettledAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SettlementStatus status;

    @Column(length = 64)
    private String utr;

    @Column(name = "settled_at")
    private OffsetDateTime settledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Settlement() {}

    public Settlement(UUID id, String settlementId, String merchantId, BigDecimal grossAmount, BigDecimal totalRefundAmount, BigDecimal totalFeeAmount, BigDecimal totalTaxAmount, BigDecimal totalAdjustmentAmount, BigDecimal netAmount, BigDecimal actualSettledAmount, SettlementStatus status, String utr, OffsetDateTime settledAt, OffsetDateTime createdAt) {
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

    public static SettlementBuilder builder() { return new SettlementBuilder(); }

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

    public static class SettlementBuilder {
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

        public SettlementBuilder id(UUID id) { this.id = id; return this; }
        public SettlementBuilder settlementId(String settlementId) { this.settlementId = settlementId; return this; }
        public SettlementBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public SettlementBuilder grossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; return this; }
        public SettlementBuilder totalRefundAmount(BigDecimal totalRefundAmount) { this.totalRefundAmount = totalRefundAmount; return this; }
        public SettlementBuilder totalFeeAmount(BigDecimal totalFeeAmount) { this.totalFeeAmount = totalFeeAmount; return this; }
        public SettlementBuilder totalTaxAmount(BigDecimal totalTaxAmount) { this.totalTaxAmount = totalTaxAmount; return this; }
        public SettlementBuilder totalAdjustmentAmount(BigDecimal totalAdjustmentAmount) { this.totalAdjustmentAmount = totalAdjustmentAmount; return this; }
        public SettlementBuilder netAmount(BigDecimal netAmount) { this.netAmount = netAmount; return this; }
        public SettlementBuilder actualSettledAmount(BigDecimal actualSettledAmount) { this.actualSettledAmount = actualSettledAmount; return this; }
        public SettlementBuilder status(SettlementStatus status) { this.status = status; return this; }
        public SettlementBuilder utr(String utr) { this.utr = utr; return this; }
        public SettlementBuilder settledAt(OffsetDateTime settledAt) { this.settledAt = settledAt; return this; }
        public SettlementBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Settlement build() {
            return new Settlement(id, settlementId, merchantId, grossAmount, totalRefundAmount, totalFeeAmount, totalTaxAmount, totalAdjustmentAmount, netAmount, actualSettledAmount, status, utr, settledAt, createdAt);
        }
    }
}
