package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.AdjustmentType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class AdjustmentResponseDto {
    private UUID id;
    private String adjustmentId;
    private String merchantId;
    private String settlementId;
    private String paymentId;
    private BigDecimal amount;
    private AdjustmentType type;
    private String description;
    private OffsetDateTime createdAt;

    public AdjustmentResponseDto() {}

    public AdjustmentResponseDto(UUID id, String adjustmentId, String merchantId, String settlementId, String paymentId, BigDecimal amount, AdjustmentType type, String description, OffsetDateTime createdAt) {
        this.id = id;
        this.adjustmentId = adjustmentId;
        this.merchantId = merchantId;
        this.settlementId = settlementId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static AdjustmentResponseDtoBuilder builder() { return new AdjustmentResponseDtoBuilder(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public static class AdjustmentResponseDtoBuilder {
        private UUID id;
        private String adjustmentId;
        private String merchantId;
        private String settlementId;
        private String paymentId;
        private BigDecimal amount;
        private AdjustmentType type;
        private String description;
        private OffsetDateTime createdAt;

        public AdjustmentResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public AdjustmentResponseDtoBuilder adjustmentId(String adjustmentId) { this.adjustmentId = adjustmentId; return this; }
        public AdjustmentResponseDtoBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public AdjustmentResponseDtoBuilder settlementId(String settlementId) { this.settlementId = settlementId; return this; }
        public AdjustmentResponseDtoBuilder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public AdjustmentResponseDtoBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public AdjustmentResponseDtoBuilder type(AdjustmentType type) { this.type = type; return this; }
        public AdjustmentResponseDtoBuilder description(String description) { this.description = description; return this; }
        public AdjustmentResponseDtoBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public AdjustmentResponseDto build() {
            return new AdjustmentResponseDto(id, adjustmentId, merchantId, settlementId, paymentId, amount, type, description, createdAt);
        }
    }
}
