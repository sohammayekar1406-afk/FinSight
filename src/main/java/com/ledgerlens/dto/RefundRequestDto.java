package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.RefundStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class RefundRequestDto {

    @NotBlank(message = "refundId must not be blank")
    private String refundId;

    @NotBlank(message = "paymentId must not be blank")
    private String paymentId;

    @NotBlank(message = "merchantId must not be blank")
    private String merchantId;

    @NotNull(message = "amount must not be null")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "currency must not be blank")
    @Size(min = 3, max = 3, message = "currency must be exactly 3 characters")
    private String currency;

    @NotNull(message = "status must not be null")
    private RefundStatus status;

    private String reason;

    public RefundRequestDto() {}

    public RefundRequestDto(String refundId, String paymentId, String merchantId, BigDecimal amount, String currency, RefundStatus status, String reason) {
        this.refundId = refundId;
        this.paymentId = paymentId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.reason = reason;
    }

    public static RefundRequestDtoBuilder builder() { return new RefundRequestDtoBuilder(); }

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

    public static class RefundRequestDtoBuilder {
        private String refundId;
        private String paymentId;
        private String merchantId;
        private BigDecimal amount;
        private String currency;
        private RefundStatus status;
        private String reason;

        public RefundRequestDtoBuilder refundId(String refundId) { this.refundId = refundId; return this; }
        public RefundRequestDtoBuilder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public RefundRequestDtoBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public RefundRequestDtoBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public RefundRequestDtoBuilder currency(String currency) { this.currency = currency; return this; }
        public RefundRequestDtoBuilder status(RefundStatus status) { this.status = status; return this; }
        public RefundRequestDtoBuilder reason(String reason) { this.reason = reason; return this; }

        public RefundRequestDto build() {
            return new RefundRequestDto(refundId, paymentId, merchantId, amount, currency, status, reason);
        }
    }
}
