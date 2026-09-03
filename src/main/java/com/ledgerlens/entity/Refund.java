package com.ledgerlens.entity;

import com.ledgerlens.entity.enums.RefundStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refunds", indexes = {
    @Index(name = "idx_refunds_refund_id", columnList = "refund_id", unique = true),
    @Index(name = "idx_refunds_payment_id", columnList = "payment_id"),
    @Index(name = "idx_refunds_settlement_id", columnList = "settlement_id")
})
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "refund_id", nullable = false, unique = true, length = 64)
    private String refundId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "merchant_id", nullable = false, length = 64)
    private String merchantId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RefundStatus status;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    public Refund() {}

    public Refund(UUID id, String refundId, Payment payment, String merchantId, BigDecimal amount, String currency, RefundStatus status, String reason, Settlement settlement, OffsetDateTime createdAt, OffsetDateTime processedAt) {
        this.id = id;
        this.refundId = refundId;
        this.payment = payment;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.status = status;
        this.reason = reason;
        this.settlement = settlement;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public static RefundBuilder builder() { return new RefundBuilder(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Settlement getSettlement() { return settlement; }
    public void setSettlement(Settlement settlement) { this.settlement = settlement; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(OffsetDateTime processedAt) { this.processedAt = processedAt; }

    public static class RefundBuilder {
        private UUID id;
        private String refundId;
        private Payment payment;
        private String merchantId;
        private BigDecimal amount;
        private String currency = "INR";
        private RefundStatus status;
        private String reason;
        private Settlement settlement;
        private OffsetDateTime createdAt;
        private OffsetDateTime processedAt;

        public RefundBuilder id(UUID id) { this.id = id; return this; }
        public RefundBuilder refundId(String refundId) { this.refundId = refundId; return this; }
        public RefundBuilder payment(Payment payment) { this.payment = payment; return this; }
        public RefundBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public RefundBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public RefundBuilder currency(String currency) { if (currency != null) this.currency = currency; return this; }
        public RefundBuilder status(RefundStatus status) { this.status = status; return this; }
        public RefundBuilder reason(String reason) { this.reason = reason; return this; }
        public RefundBuilder settlement(Settlement settlement) { this.settlement = settlement; return this; }
        public RefundBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public RefundBuilder processedAt(OffsetDateTime processedAt) { this.processedAt = processedAt; return this; }

        public Refund build() {
            return new Refund(id, refundId, payment, merchantId, amount, currency, status, reason, settlement, createdAt, processedAt);
        }
    }
}
