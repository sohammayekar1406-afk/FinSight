package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.PaymentMethod;
import com.ledgerlens.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PaymentResponseDto {
    private UUID id;
    private String paymentId;
    private String orderId;
    private String merchantId;
    private PaymentMethod method;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String settlementId;
    private String errorCode;
    private String errorDescription;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public PaymentResponseDto() {}

    public PaymentResponseDto(UUID id, String paymentId, String orderId, String merchantId, PaymentMethod method, BigDecimal amount, String currency, PaymentStatus status, String settlementId, String errorCode, String errorDescription, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.merchantId = merchantId;
        this.method = method;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.settlementId = settlementId;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PaymentResponseDtoBuilder builder() { return new PaymentResponseDtoBuilder(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
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
    public String getSettlementId() { return settlementId; }
    public void setSettlementId(String settlementId) { this.settlementId = settlementId; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorDescription() { return errorDescription; }
    public void setErrorDescription(String errorDescription) { this.errorDescription = errorDescription; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class PaymentResponseDtoBuilder {
        private UUID id;
        private String paymentId;
        private String orderId;
        private String merchantId;
        private PaymentMethod method;
        private BigDecimal amount;
        private String currency;
        private PaymentStatus status;
        private String settlementId;
        private String errorCode;
        private String errorDescription;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public PaymentResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public PaymentResponseDtoBuilder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public PaymentResponseDtoBuilder orderId(String orderId) { this.orderId = orderId; return this; }
        public PaymentResponseDtoBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public PaymentResponseDtoBuilder method(PaymentMethod method) { this.method = method; return this; }
        public PaymentResponseDtoBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public PaymentResponseDtoBuilder currency(String currency) { this.currency = currency; return this; }
        public PaymentResponseDtoBuilder status(PaymentStatus status) { this.status = status; return this; }
        public PaymentResponseDtoBuilder settlementId(String settlementId) { this.settlementId = settlementId; return this; }
        public PaymentResponseDtoBuilder errorCode(String errorCode) { this.errorCode = errorCode; return this; }
        public PaymentResponseDtoBuilder errorDescription(String errorDescription) { this.errorDescription = errorDescription; return this; }
        public PaymentResponseDtoBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PaymentResponseDtoBuilder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public PaymentResponseDto build() {
            return new PaymentResponseDto(id, paymentId, orderId, merchantId, method, amount, currency, status, settlementId, errorCode, errorDescription, createdAt, updatedAt);
        }
    }
}
