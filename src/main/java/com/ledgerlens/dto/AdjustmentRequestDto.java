package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.AdjustmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AdjustmentRequestDto {

    @NotBlank(message = "adjustmentId must not be blank")
    private String adjustmentId;

    @NotBlank(message = "merchantId must not be blank")
    private String merchantId;

    private String settlementId;
    private String paymentId;

    @NotNull(message = "amount must not be null")
    private BigDecimal amount;

    @NotNull(message = "type must not be null")
    private AdjustmentType type;

    private String description;

    public AdjustmentRequestDto() {}

    public AdjustmentRequestDto(String adjustmentId, String merchantId, String settlementId, String paymentId, BigDecimal amount, AdjustmentType type, String description) {
        this.adjustmentId = adjustmentId;
        this.merchantId = merchantId;
        this.settlementId = settlementId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    public static AdjustmentRequestDtoBuilder builder() { return new AdjustmentRequestDtoBuilder(); }

    public String getAdjustmentId() { return adjustmentId; }
    public void setAdjustmentId(String adjustmentId) { this.adjustmentId = adjustmentId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getSettlementId() { return settlementId; }
    public void setSettlementId(String settlementId) { this.settlementId = settlementId; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public AdjustmentType getType() { return type; }
    public void setType(AdjustmentType type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public static class AdjustmentRequestDtoBuilder {
        private String adjustmentId;
        private String merchantId;
        private String settlementId;
        private String paymentId;
        private BigDecimal amount;
        private AdjustmentType type;
        private String description;

        public AdjustmentRequestDtoBuilder adjustmentId(String adjustmentId) { this.adjustmentId = adjustmentId; return this; }
        public AdjustmentRequestDtoBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public AdjustmentRequestDtoBuilder settlementId(String settlementId) { this.settlementId = settlementId; return this; }
        public AdjustmentRequestDtoBuilder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public AdjustmentRequestDtoBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public AdjustmentRequestDtoBuilder type(AdjustmentType type) { this.type = type; return this; }
        public AdjustmentRequestDtoBuilder description(String description) { this.description = description; return this; }

        public AdjustmentRequestDto build() {
            return new AdjustmentRequestDto(adjustmentId, merchantId, settlementId, paymentId, amount, type, description);
        }
    }
}
