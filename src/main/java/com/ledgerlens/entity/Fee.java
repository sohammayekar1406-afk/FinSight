package com.ledgerlens.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "fees", indexes = {
    @Index(name = "idx_fees_payment_id", columnList = "payment_id"),
    @Index(name = "idx_fees_refund_id", columnList = "refund_id")
})
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_id")
    private Refund refund;

    @Column(name = "merchant_id", nullable = false, length = 64)
    private String merchantId;

    @Column(name = "fee_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal feeAmount;

    @Column(name = "tax_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalFee;

    @Column(name = "fee_rate", precision = 6, scale = 4)
    private BigDecimal feeRate;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Fee() {}

    public Fee(UUID id, Payment payment, Refund refund, String merchantId, BigDecimal feeAmount, BigDecimal taxAmount, BigDecimal totalFee, BigDecimal feeRate, String currency, OffsetDateTime createdAt) {
        this.id = id;
        this.payment = payment;
        this.refund = refund;
        this.merchantId = merchantId;
        this.feeAmount = feeAmount;
        this.taxAmount = taxAmount;
        this.totalFee = totalFee;
        this.feeRate = feeRate;
        this.currency = currency != null ? currency : "INR";
        this.createdAt = createdAt;
    }

    public static FeeBuilder builder() { return new FeeBuilder(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public Refund getRefund() { return refund; }
    public void setRefund(Refund refund) { this.refund = refund; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalFee() { return totalFee; }
    public void setTotalFee(BigDecimal totalFee) { this.totalFee = totalFee; }
    public BigDecimal getFeeRate() { return feeRate; }
    public void setFeeRate(BigDecimal feeRate) { this.feeRate = feeRate; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public static class FeeBuilder {
        private UUID id;
        private Payment payment;
        private Refund refund;
        private String merchantId;
        private BigDecimal feeAmount;
        private BigDecimal taxAmount;
        private BigDecimal totalFee;
        private BigDecimal feeRate;
        private String currency = "INR";
        private OffsetDateTime createdAt;

        public FeeBuilder id(UUID id) { this.id = id; return this; }
        public FeeBuilder payment(Payment payment) { this.payment = payment; return this; }
        public FeeBuilder refund(Refund refund) { this.refund = refund; return this; }
        public FeeBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public FeeBuilder feeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; return this; }
        public FeeBuilder taxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; return this; }
        public FeeBuilder totalFee(BigDecimal totalFee) { this.totalFee = totalFee; return this; }
        public FeeBuilder feeRate(BigDecimal feeRate) { this.feeRate = feeRate; return this; }
        public FeeBuilder currency(String currency) { if (currency != null) this.currency = currency; return this; }
        public FeeBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Fee build() {
            return new Fee(id, payment, refund, merchantId, feeAmount, taxAmount, totalFee, feeRate, currency, createdAt);
        }
    }
}
