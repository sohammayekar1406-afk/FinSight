package com.ledgerlens.entity;

import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.entity.enums.ExceptionType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "exceptions", indexes = {
    @Index(name = "idx_exceptions_exception_id", columnList = "exception_id", unique = true),
    @Index(name = "idx_exceptions_status_severity", columnList = "status, severity"),
    @Index(name = "idx_exceptions_payment_id", columnList = "payment_id"),
    @Index(name = "idx_exceptions_settlement_id", columnList = "settlement_id"),
    @Index(name = "idx_exceptions_deduplication_key", columnList = "deduplication_key", unique = true)
})
public class FinancialException {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "exception_id", nullable = false, unique = true, length = 64)
    private String exceptionId;

    /** Stable database-enforced identity for one open reconciliation finding. */
    // Nullable for a safe rolling upgrade of existing exception rows; every new row sets it.
    @Column(name = "deduplication_key", unique = true, length = 128)
    private String deduplicationKey;

    @Column(name = "merchant_id", nullable = false, length = 64)
    private String merchantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "exception_type", nullable = false, length = 32)
    private ExceptionType exceptionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private ExceptionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ExceptionSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ExceptionStatus status;

    @Column(name = "expected_amount", precision = 15, scale = 2)
    private BigDecimal expectedAmount;

    @Column(name = "actual_amount", precision = 15, scale = 2)
    private BigDecimal actualAmount;

    @Column(name = "discrepancy_amount", precision = 15, scale = 2)
    private BigDecimal discrepancyAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_id")
    private Refund refund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "detected_at", nullable = false, updatable = false)
    private OffsetDateTime detectedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    public FinancialException() {}

    public FinancialException(UUID id, String exceptionId, String merchantId, ExceptionType exceptionType, ExceptionType type, ExceptionSeverity severity, ExceptionStatus status, BigDecimal expectedAmount, BigDecimal actualAmount, BigDecimal discrepancyAmount, Order order, Payment payment, Refund refund, Settlement settlement, String description, OffsetDateTime detectedAt, OffsetDateTime createdAt, OffsetDateTime resolvedAt) {
        this.id = id;
        this.exceptionId = exceptionId;
        this.merchantId = merchantId;
        this.exceptionType = exceptionType;
        this.type = type != null ? type : exceptionType;
        this.severity = severity;
        this.status = status;
        this.expectedAmount = expectedAmount;
        this.actualAmount = actualAmount;
        this.discrepancyAmount = discrepancyAmount;
        this.order = order;
        this.payment = payment;
        this.refund = refund;
        this.settlement = settlement;
        this.description = description;
        this.detectedAt = detectedAt;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public static FinancialExceptionBuilder builder() { return new FinancialExceptionBuilder(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getExceptionId() { return exceptionId; }
    public void setExceptionId(String exceptionId) { this.exceptionId = exceptionId; }
    public String getDeduplicationKey() { return deduplicationKey; }
    public void setDeduplicationKey(String deduplicationKey) { this.deduplicationKey = deduplicationKey; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public ExceptionType getExceptionType() { return exceptionType; }
    public void setExceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; this.type = exceptionType; }
    public ExceptionType getType() { return type; }
    public void setType(ExceptionType type) { this.type = type; }
    public ExceptionSeverity getSeverity() { return severity; }
    public void setSeverity(ExceptionSeverity severity) { this.severity = severity; }
    public ExceptionStatus getStatus() { return status; }
    public void setStatus(ExceptionStatus status) { this.status = status; }
    public BigDecimal getExpectedAmount() { return expectedAmount; }
    public void setExpectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; }
    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }
    public BigDecimal getDiscrepancyAmount() { return discrepancyAmount; }
    public void setDiscrepancyAmount(BigDecimal discrepancyAmount) { this.discrepancyAmount = discrepancyAmount; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public Refund getRefund() { return refund; }
    public void setRefund(Refund refund) { this.refund = refund; }
    public Settlement getSettlement() { return settlement; }
    public void setSettlement(Settlement settlement) { this.settlement = settlement; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public OffsetDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(OffsetDateTime detectedAt) { this.detectedAt = detectedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public static class FinancialExceptionBuilder {
        private UUID id;
        private String exceptionId;
        private String merchantId;
        private ExceptionType exceptionType;
        private ExceptionType type;
        private ExceptionSeverity severity;
        private ExceptionStatus status;
        private BigDecimal expectedAmount;
        private BigDecimal actualAmount;
        private BigDecimal discrepancyAmount;
        private Order order;
        private Payment payment;
        private Refund refund;
        private Settlement settlement;
        private String description;
        private OffsetDateTime detectedAt;
        private OffsetDateTime createdAt;
        private OffsetDateTime resolvedAt;

        public FinancialExceptionBuilder id(UUID id) { this.id = id; return this; }
        public FinancialExceptionBuilder exceptionId(String exceptionId) { this.exceptionId = exceptionId; return this; }
        public FinancialExceptionBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public FinancialExceptionBuilder exceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; this.type = exceptionType; return this; }
        public FinancialExceptionBuilder type(ExceptionType type) { this.type = type; return this; }
        public FinancialExceptionBuilder severity(ExceptionSeverity severity) { this.severity = severity; return this; }
        public FinancialExceptionBuilder status(ExceptionStatus status) { this.status = status; return this; }
        public FinancialExceptionBuilder expectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; return this; }
        public FinancialExceptionBuilder actualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; return this; }
        public FinancialExceptionBuilder discrepancyAmount(BigDecimal discrepancyAmount) { this.discrepancyAmount = discrepancyAmount; return this; }
        public FinancialExceptionBuilder order(Order order) { this.order = order; return this; }
        public FinancialExceptionBuilder payment(Payment payment) { this.payment = payment; return this; }
        public FinancialExceptionBuilder refund(Refund refund) { this.refund = refund; return this; }
        public FinancialExceptionBuilder settlement(Settlement settlement) { this.settlement = settlement; return this; }
        public FinancialExceptionBuilder description(String description) { this.description = description; return this; }
        public FinancialExceptionBuilder detectedAt(OffsetDateTime detectedAt) { this.detectedAt = detectedAt; return this; }
        public FinancialExceptionBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public FinancialExceptionBuilder resolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }

        public FinancialException build() {
            return new FinancialException(id, exceptionId, merchantId, exceptionType, type != null ? type : exceptionType, severity, status, expectedAmount, actualAmount, discrepancyAmount, order, payment, refund, settlement, description, detectedAt, createdAt, resolvedAt);
        }
    }
}
