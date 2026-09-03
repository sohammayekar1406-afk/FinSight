package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class RefundResponseDto {
    private UUID id;
    private String refundId;
    private String paymentId;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private RefundStatus status;
    private String reason;
    private String settlementId;
    private OffsetDateTime createdAt;
    private OffsetDateTime processedAt;

    public RefundResponseDto() {}

    public RefundResponseDto(UUID id, String refundId, String paymentId, String merchantId, BigDecimal amount, String currency, RefundStatus status, String reason, String settlementId, OffsetDateTime createdAt, OffsetDateTime processedAt) {
        this.id = id;
        this.refundId = refundId;
        this.paymentId = paymentId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.reason = reason;
        this.settlementId = settlementId;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public static RefundResponseDtoBuilder builder() { return new RefundResponseDtoBuilder(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
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
    public String getSettlementId() { return settlementId; }
    public void setSettlementId(String settlementId) { this.settlementId = settlementId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(OffsetDateTime processedAt) { this.processedAt = processedAt; }

    public static class RefundResponseDtoBuilder {
        private UUID id;
        private String refundId;
        private String paymentId;
        private String merchantId;
        private BigDecimal amount;
        private String currency;
        private RefundStatus status;
        private String reason;
        private String settlementId;
        private OffsetDateTime createdAt;
        private OffsetDateTime processedAt;

        public RefundResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public RefundResponseDtoBuilder refundId(String refundId) { this.refundId = refundId; return this; }
        public RefundResponseDtoBuilder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public RefundResponseDtoBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public RefundResponseDtoBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public RefundResponseDtoBuilder currency(String currency) { this.currency = currency; return this; }
        public RefundResponseDtoBuilder status(RefundStatus status) { this.status = status; return this; }
        public RefundResponseDtoBuilder reason(String reason) { this.reason = reason; return this; }
        public RefundResponseDtoBuilder settlementId(String settlementId) { this.settlementId = settlementId; return this; }
        public RefundResponseDtoBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public RefundResponseDtoBuilder processedAt(OffsetDateTime processedAt) { this.processedAt = processedAt; return this; }

        public RefundResponseDto build() {
            return new RefundResponseDto(id, refundId, paymentId, merchantId, amount, currency, status, reason, settlementId, createdAt, processedAt);
        }
    }
}
