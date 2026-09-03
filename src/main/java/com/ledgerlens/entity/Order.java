package com.ledgerlens.entity;

import com.ledgerlens.entity.enums.OrderStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_order_id", columnList = "order_id", unique = true),
    @Index(name = "idx_orders_merchant_status", columnList = "merchant_id, status")
})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;

    @Column(name = "merchant_id", nullable = false, length = 64)
    private String merchantId;

    @Column(name = "customer_id", length = 64)
    private String customerId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Order() {
    }

    public Order(UUID id, String orderId, String merchantId, String customerId, BigDecimal amount, String currency, OrderStatus status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.merchantId = merchantId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class OrderBuilder {
        private UUID id;
        private String orderId;
        private String merchantId;
        private String customerId;
        private BigDecimal amount;
        private String currency = "INR";
        private OrderStatus status;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public OrderBuilder id(UUID id) { this.id = id; return this; }
        public OrderBuilder orderId(String orderId) { this.orderId = orderId; return this; }
        public OrderBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public OrderBuilder customerId(String customerId) { this.customerId = customerId; return this; }
        public OrderBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public OrderBuilder currency(String currency) { if (currency != null) this.currency = currency; return this; }
        public OrderBuilder status(OrderStatus status) { this.status = status; return this; }
        public OrderBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public OrderBuilder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Order build() {
            return new Order(id, orderId, merchantId, customerId, amount, currency, status, createdAt, updatedAt);
        }
    }
}
