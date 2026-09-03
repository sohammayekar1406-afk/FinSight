package com.ledgerlens.entity;

import com.ledgerlens.entity.enums.AdjustmentType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "adjustments", indexes = {
    @Index(name = "idx_adjustments_adjustment_id", columnList = "adjustment_id", unique = true),
    @Index(name = "idx_adjustments_settlement_id", columnList = "settlement_id"),
    @Index(name = "idx_adjustments_payment_id", columnList = "payment_id")
})
public class Adjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "adjustment_id", nullable = false, unique = true, length = 64)
    private String adjustmentId;

    @Column(name = "merchant_id", nullable = false, length = 64)
    private String merchantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AdjustmentType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Adjustment() {}

    public Adjustment(UUID id, String adjustmentId, String merchantId, Settlement settlement, Payment payment, BigDecimal amount, AdjustmentType type, String description, OffsetDateTime createdAt) {
        this.id = id;
        this.adjustmentId = adjustmentId;
        this.merchantId = merchantId;
        this.settlement = settlement;
        this.payment = payment;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static AdjustmentBuilder builder() { return new AdjustmentBuilder(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getAdjustmentId() { return adjustmentId; }
    public void setAdjustmentId(String adjustmentId) { this.adjustmentId = adjustmentId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public Settlement getSettlement() { return settlement; }
    public void setSettlement(Settlement settlement) { this.settlement = settlement; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public AdjustmentType getType() { return type; }
    public void setType(AdjustmentType type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public static class AdjustmentBuilder {
        private UUID id;
        private String adjustmentId;
        private String merchantId;
        private Settlement settlement;
        private Payment payment;
        private BigDecimal amount;
        private AdjustmentType type;
        private String description;
        private OffsetDateTime createdAt;

        public AdjustmentBuilder id(UUID id) { this.id = id; return this; }
        public AdjustmentBuilder adjustmentId(String adjustmentId) { this.adjustmentId = adjustmentId; return this; }
        public AdjustmentBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public AdjustmentBuilder settlement(Settlement settlement) { this.settlement = settlement; return this; }
        public AdjustmentBuilder payment(Payment payment) { this.payment = payment; return this; }
        public AdjustmentBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public AdjustmentBuilder type(AdjustmentType type) { this.type = type; return this; }
        public AdjustmentBuilder description(String description) { this.description = description; return this; }
        public AdjustmentBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Adjustment build() {
            return new Adjustment(id, adjustmentId, merchantId, settlement, payment, amount, type, description, createdAt);
        }
    }
}
