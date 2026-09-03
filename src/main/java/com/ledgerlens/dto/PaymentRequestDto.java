package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.PaymentMethod;
import com.ledgerlens.entity.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PaymentRequestDto {

    @NotBlank(message = "paymentId must not be blank")
    private String paymentId;

    @NotBlank(message = "orderId must not be blank")
    private String orderId;

    @NotBlank(message = "merchantId must not be blank")
    private String merchantId;

    @NotNull(message = "method must not be null")
    private PaymentMethod method;

    @NotNull(message = "amount must not be null")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "currency must not be blank")
    @Size(min = 3, max = 3, message = "currency must be exactly 3 characters")
    private String currency;

    @NotNull(message = "status must not be null")
    private PaymentStatus status;

    private String errorCode;
    private String errorDescription;

    public PaymentRequestDto() {}

    public PaymentRequestDto(String paymentId, String orderId, String merchantId, PaymentMethod method, BigDecimal amount, String currency, PaymentStatus status, String errorCode, String errorDescription) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.merchantId = merchantId;
        this.method = method;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public static PaymentRequestDtoBuilder builder() { return new PaymentRequestDtoBuilder(); }

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
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorDescription() { return errorDescription; }
    public void setErrorDescription(String errorDescription) { this.errorDescription = errorDescription; }

    public static class PaymentRequestDtoBuilder {
        private String paymentId;
        private String orderId;
        private String merchantId;
        private PaymentMethod method;
        private BigDecimal amount;
        private String currency;
        private PaymentStatus status;
        private String errorCode;
        private String errorDescription;

        public PaymentRequestDtoBuilder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public PaymentRequestDtoBuilder orderId(String orderId) { this.orderId = orderId; return this; }
        public PaymentRequestDtoBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public PaymentRequestDtoBuilder method(PaymentMethod method) { this.method = method; return this; }
        public PaymentRequestDtoBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public PaymentRequestDtoBuilder currency(String currency) { this.currency = currency; return this; }
        public PaymentRequestDtoBuilder status(PaymentStatus status) { this.status = status; return this; }
        public PaymentRequestDtoBuilder errorCode(String errorCode) { this.errorCode = errorCode; return this; }
        public PaymentRequestDtoBuilder errorDescription(String errorDescription) { this.errorDescription = errorDescription; return this; }

        public PaymentRequestDto build() {
            return new PaymentRequestDto(paymentId, orderId, merchantId, method, amount, currency, status, errorCode, errorDescription);
        }
    }
}
