package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.SettlementStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class SettlementRequestDto {

    @NotBlank(message = "settlementId must not be blank")
    private String settlementId;

    @NotBlank(message = "merchantId must not be blank")
    private String merchantId;

    @NotNull(message = "grossAmount must not be null")
    @DecimalMin(value = "0.00", message = "grossAmount must not be negative")
    private BigDecimal grossAmount;

    @NotNull(message = "totalRefundAmount must not be null")
    @DecimalMin(value = "0.00", message = "totalRefundAmount must not be negative")
    private BigDecimal totalRefundAmount;

    @NotNull(message = "totalFeeAmount must not be null")
    @DecimalMin(value = "0.00", message = "totalFeeAmount must not be negative")
    private BigDecimal totalFeeAmount;

    @NotNull(message = "totalTaxAmount must not be null")
    @DecimalMin(value = "0.00", message = "totalTaxAmount must not be negative")
    private BigDecimal totalTaxAmount;

    @NotNull(message = "totalAdjustmentAmount must not be null")
    private BigDecimal totalAdjustmentAmount;

    @NotNull(message = "netAmount must not be null")
    private BigDecimal netAmount;

    private BigDecimal actualSettledAmount;

    @NotNull(message = "status must not be null")
    private SettlementStatus status;

    private String utr;

    public SettlementRequestDto() {}

    public SettlementRequestDto(String settlementId, String merchantId, BigDecimal grossAmount, BigDecimal totalRefundAmount, BigDecimal totalFeeAmount, BigDecimal totalTaxAmount, BigDecimal totalAdjustmentAmount, BigDecimal netAmount, BigDecimal actualSettledAmount, SettlementStatus status, String utr) {
        this.settlementId = settlementId;
        this.merchantId = merchantId;
        this.grossAmount = grossAmount;
        this.totalRefundAmount = totalRefundAmount;
        this.totalFeeAmount = totalFeeAmount;
        this.totalTaxAmount = totalTaxAmount;
        this.totalAdjustmentAmount = totalAdjustmentAmount;
        this.netAmount = netAmount;
        this.actualSettledAmount = actualSettledAmount;
        this.status = status;
        this.utr = utr;
    }

    public static SettlementRequestDtoBuilder builder() { return new SettlementRequestDtoBuilder(); }

    public String getSettlementId() { return settlementId; }
    public void setSettlementId(String settlementId) { this.settlementId = settlementId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public BigDecimal getTotalRefundAmount() { return totalRefundAmount; }
    public void setTotalRefundAmount(BigDecimal totalRefundAmount) { this.totalRefundAmount = totalRefundAmount; }
    public BigDecimal getTotalFeeAmount() { return totalFeeAmount; }
    public void setTotalFeeAmount(BigDecimal totalFeeAmount) { this.totalFeeAmount = totalFeeAmount; }
    public BigDecimal getTotalTaxAmount() { return totalTaxAmount; }
    public void setTotalTaxAmount(BigDecimal totalTaxAmount) { this.totalTaxAmount = totalTaxAmount; }
    public BigDecimal getTotalAdjustmentAmount() { return totalAdjustmentAmount; }
    public void setTotalAdjustmentAmount(BigDecimal totalAdjustmentAmount) { this.totalAdjustmentAmount = totalAdjustmentAmount; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public BigDecimal getActualSettledAmount() { return actualSettledAmount; }
    public void setActualSettledAmount(BigDecimal actualSettledAmount) { this.actualSettledAmount = actualSettledAmount; }
    public SettlementStatus getStatus() { return status; }
    public void setStatus(SettlementStatus status) { this.status = status; }
    public String getUtr() { return utr; }
    public void setUtr(String utr) { this.utr = utr; }

    public static class SettlementRequestDtoBuilder {
        private String settlementId;
        private String merchantId;
        private BigDecimal grossAmount;
        private BigDecimal totalRefundAmount;
        private BigDecimal totalFeeAmount;
        private BigDecimal totalTaxAmount;
        private BigDecimal totalAdjustmentAmount;
        private BigDecimal netAmount;
        private BigDecimal actualSettledAmount;
        private SettlementStatus status;
        private String utr;

        public SettlementRequestDtoBuilder settlementId(String settlementId) { this.settlementId = settlementId; return this; }
        public SettlementRequestDtoBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public SettlementRequestDtoBuilder grossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; return this; }
        public SettlementRequestDtoBuilder totalRefundAmount(BigDecimal totalRefundAmount) { this.totalRefundAmount = totalRefundAmount; return this; }
        public SettlementRequestDtoBuilder totalFeeAmount(BigDecimal totalFeeAmount) { this.totalFeeAmount = totalFeeAmount; return this; }
        public SettlementRequestDtoBuilder totalTaxAmount(BigDecimal totalTaxAmount) { this.totalTaxAmount = totalTaxAmount; return this; }
        public SettlementRequestDtoBuilder totalAdjustmentAmount(BigDecimal totalAdjustmentAmount) { this.totalAdjustmentAmount = totalAdjustmentAmount; return this; }
        public SettlementRequestDtoBuilder netAmount(BigDecimal netAmount) { this.netAmount = netAmount; return this; }
        public SettlementRequestDtoBuilder actualSettledAmount(BigDecimal actualSettledAmount) { this.actualSettledAmount = actualSettledAmount; return this; }
        public SettlementRequestDtoBuilder status(SettlementStatus status) { this.status = status; return this; }
        public SettlementRequestDtoBuilder utr(String utr) { this.utr = utr; return this; }

        public SettlementRequestDto build() {
            return new SettlementRequestDto(settlementId, merchantId, grossAmount, totalRefundAmount, totalFeeAmount, totalTaxAmount, totalAdjustmentAmount, netAmount, actualSettledAmount, status, utr);
        }
    }
}
