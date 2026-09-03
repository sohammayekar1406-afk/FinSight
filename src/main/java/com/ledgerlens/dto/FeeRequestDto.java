package com.ledgerlens.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class FeeRequestDto {

    private String paymentId;
    private String refundId;

    @NotBlank(message = "merchantId must not be blank")
    private String merchantId;

    @NotNull(message = "feeAmount must not be null")
    @DecimalMin(value = "0.00", message = "feeAmount must not be negative")
    private BigDecimal feeAmount;

    @NotNull(message = "taxAmount must not be null")
    @DecimalMin(value = "0.00", message = "taxAmount must not be negative")
    private BigDecimal taxAmount;

    @NotNull(message = "totalFee must not be null")
    @DecimalMin(value = "0.00", message = "totalFee must not be negative")
    private BigDecimal totalFee;

    @DecimalMin(value = "0.0000", message = "feeRate must be at least 0.0000")
    @DecimalMax(value = "1.0000", message = "feeRate must be at most 1.0000")
    private BigDecimal feeRate;

    @NotBlank(message = "currency must not be blank")
    @Size(min = 3, max = 3, message = "currency must be exactly 3 characters")
    private String currency;

    public FeeRequestDto() {}

    public FeeRequestDto(String paymentId, String refundId, String merchantId, BigDecimal feeAmount, BigDecimal taxAmount, BigDecimal totalFee, BigDecimal feeRate, String currency) {
        this.paymentId = paymentId;
        this.refundId = refundId;
        this.merchantId = merchantId;
        this.feeAmount = feeAmount;
        this.taxAmount = taxAmount;
        this.totalFee = totalFee;
        this.feeRate = feeRate;
        this.currency = currency;
    }

    public static FeeRequestDtoBuilder builder() { return new FeeRequestDtoBuilder(); }

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

    public static class FeeRequestDtoBuilder {
        private String paymentId;
        private String refundId;
        private String merchantId;
        private BigDecimal feeAmount;
        private BigDecimal taxAmount;
        private BigDecimal totalFee;
        private BigDecimal feeRate;
        private String currency;

        public FeeRequestDtoBuilder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public FeeRequestDtoBuilder refundId(String refundId) { this.refundId = refundId; return this; }
        public FeeRequestDtoBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public FeeRequestDtoBuilder feeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; return this; }
        public FeeRequestDtoBuilder taxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; return this; }
        public FeeRequestDtoBuilder totalFee(BigDecimal totalFee) { this.totalFee = totalFee; return this; }
        public FeeRequestDtoBuilder feeRate(BigDecimal feeRate) { this.feeRate = feeRate; return this; }
        public FeeRequestDtoBuilder currency(String currency) { this.currency = currency; return this; }

        public FeeRequestDto build() {
            return new FeeRequestDto(paymentId, refundId, merchantId, feeAmount, taxAmount, totalFee, feeRate, currency);
        }
    }
}
