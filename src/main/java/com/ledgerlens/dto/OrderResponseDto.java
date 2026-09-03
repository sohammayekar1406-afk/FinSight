package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class OrderResponseDto {
    private UUID id;
    private String orderId;
    private String merchantId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private OrderStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public OrderResponseDto() {}

    public OrderResponseDto(UUID id, String orderId, String merchantId, String customerId, BigDecimal amount, String currency, OrderStatus status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.merchantId = merchantId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OrderResponseDtoBuilder builder() { return new OrderResponseDtoBuilder(); }

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

    public static class OrderResponseDtoBuilder {
        private UUID id;
        private String orderId;
        private String merchantId;
        private String customerId;
        private BigDecimal amount;
        private String currency;
        private OrderStatus status;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public OrderResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public OrderResponseDtoBuilder orderId(String orderId) { this.orderId = orderId; return this; }
        public OrderResponseDtoBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public OrderResponseDtoBuilder customerId(String customerId) { this.customerId = customerId; return this; }
        public OrderResponseDtoBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public OrderResponseDtoBuilder currency(String currency) { this.currency = currency; return this; }
        public OrderResponseDtoBuilder status(OrderStatus status) { this.status = status; return this; }
        public OrderResponseDtoBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public OrderResponseDtoBuilder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public OrderResponseDto build() {
            return new OrderResponseDto(id, orderId, merchantId, customerId, amount, currency, status, createdAt, updatedAt);
        }
    }
}
