package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class OrderRequestDto {

    @NotBlank(message = "orderId must not be blank")
    private String orderId;

    @NotBlank(message = "merchantId must not be blank")
    private String merchantId;

    private String customerId;

    @NotNull(message = "amount must not be null")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "currency must not be blank")
    @Size(min = 3, max = 3, message = "currency must be exactly 3 characters")
    private String currency;

    @NotNull(message = "status must not be null")
    private OrderStatus status;

    public OrderRequestDto() {}

    public OrderRequestDto(String orderId, String merchantId, String customerId, BigDecimal amount, String currency, OrderStatus status) {
        this.orderId = orderId;
        this.merchantId = merchantId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public static OrderRequestDtoBuilder builder() { return new OrderRequestDtoBuilder(); }

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

    public static class OrderRequestDtoBuilder {
        private String orderId;
        private String merchantId;
        private String customerId;
        private BigDecimal amount;
        private String currency;
        private OrderStatus status;

        public OrderRequestDtoBuilder orderId(String orderId) { this.orderId = orderId; return this; }
        public OrderRequestDtoBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public OrderRequestDtoBuilder customerId(String customerId) { this.customerId = customerId; return this; }
        public OrderRequestDtoBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public OrderRequestDtoBuilder currency(String currency) { this.currency = currency; return this; }
        public OrderRequestDtoBuilder status(OrderStatus status) { this.status = status; return this; }

        public OrderRequestDto build() {
            return new OrderRequestDto(orderId, merchantId, customerId, amount, currency, status);
        }
    }
}
