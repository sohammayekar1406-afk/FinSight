package com.ledgerlens.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class FeeResponseDto {
    private UUID id;
    private String paymentId;
    private String refundId;
    private String merchantId;
    private BigDecimal feeAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalFee;
    private BigDecimal feeRate;
    private String currency;
    private OffsetDateTime createdAt;

    public FeeResponseDto() {}

    public FeeResponseDto(UUID id, String paymentId, String refundId, String merchantId, BigDecimal feeAmount, BigDecimal taxAmount, BigDecimal totalFee, BigDecimal feeRate, String currency, OffsetDateTime createdAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.refundId = refundId;
        this.merchantId = merchantId;
        this.feeAmount = feeAmount;
        this.taxAmount = taxAmount;
        this.totalFee = totalFee;
        this.feeRate = feeRate;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    public static FeeResponseDtoBuilder builder() { return new FeeResponseDtoBuilder(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }
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

    public static class FeeResponseDtoBuilder {
        private UUID id;
        private String paymentId;
        private String refundId;
        private String merchantId;
        private BigDecimal feeAmount;
        private BigDecimal taxAmount;
        private BigDecimal totalFee;
        private BigDecimal feeRate;
        private String currency;
        private OffsetDateTime createdAt;

        public FeeResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public FeeResponseDtoBuilder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public FeeResponseDtoBuilder refundId(String refundId) { this.refundId = refundId; return this; }
        public FeeResponseDtoBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public FeeResponseDtoBuilder feeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; return this; }
        public FeeResponseDtoBuilder taxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; return this; }
        public FeeResponseDtoBuilder totalFee(BigDecimal totalFee) { this.totalFee = totalFee; return this; }
        public FeeResponseDtoBuilder feeRate(BigDecimal feeRate) { this.feeRate = feeRate; return this; }
        public FeeResponseDtoBuilder currency(String currency) { this.currency = currency; return this; }
        public FeeResponseDtoBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public FeeResponseDto build() {
            return new FeeResponseDto(id, paymentId, refundId, merchantId, feeAmount, taxAmount, totalFee, feeRate, currency, createdAt);
        }
    }
}
