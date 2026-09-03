package com.ledgerlens.entity;

import com.ledgerlens.entity.enums.PaymentMethod;
import com.ledgerlens.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_payment_id", columnList = "payment_id", unique = true),
    @Index(name = "idx_payments_order_id", columnList = "order_id"),
    @Index(name = "idx_payments_settlement_id", columnList = "settlement_id")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payment_id", nullable = false, unique = true, length = 64)
    private String paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "merchant_id", nullable = false, length = 64)
    private String merchantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentMethod method;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "error_description", columnDefinition = "TEXT")
    private String errorDescription;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Payment() {
    }

    public Payment(UUID id, String paymentId, Order order, String merchantId, PaymentMethod method, BigDecimal amount, String currency, PaymentStatus status, Settlement settlement, String errorCode, String errorDescription, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.order = order;
        this.merchantId = merchantId;
        this.method = method;
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.status = status;
        this.settlement = settlement;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PaymentBuilder builder() { return new PaymentBuilder(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public Settlement getSettlement() { return settlement; }
    public void setSettlement(Settlement settlement) { this.settlement = settlement; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorDescription() { return errorDescription; }
    public void setErrorDescription(String errorDescription) { this.errorDescription = errorDescription; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class PaymentBuilder {
        private UUID id;
        private String paymentId;
        private Order order;
        private String merchantId;
        private PaymentMethod method;
        private BigDecimal amount;
        private String currency = "INR";
        private PaymentStatus status;
        private Settlement settlement;
        private String errorCode;
        private String errorDescription;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public PaymentBuilder id(UUID id) { this.id = id; return this; }
        public PaymentBuilder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public PaymentBuilder order(Order order) { this.order = order; return this; }
        public PaymentBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public PaymentBuilder method(PaymentMethod method) { this.method = method; return this; }
        public PaymentBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public PaymentBuilder currency(String currency) { if (currency != null) this.currency = currency; return this; }
        public PaymentBuilder status(PaymentStatus status) { this.status = status; return this; }
        public PaymentBuilder settlement(Settlement settlement) { this.settlement = settlement; return this; }
        public PaymentBuilder errorCode(String errorCode) { this.errorCode = errorCode; return this; }
        public PaymentBuilder errorDescription(String errorDescription) { this.errorDescription = errorDescription; return this; }
        public PaymentBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PaymentBuilder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Payment build() {
            return new Payment(id, paymentId, order, merchantId, method, amount, currency, status, settlement, errorCode, errorDescription, createdAt, updatedAt);
        }
    }
}
