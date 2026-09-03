package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.entity.enums.ExceptionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class FinancialExceptionResponseDto {
    private UUID id;
    private String exceptionId;
    private String merchantId;
    private ExceptionType exceptionType;
    private ExceptionSeverity severity;
    private ExceptionStatus status;
    private BigDecimal expectedAmount;
    private BigDecimal actualAmount;
    private BigDecimal discrepancyAmount;
    private String orderId;
    private String paymentId;
    private String refundId;
    private String settlementId;
    private String description;
    private OffsetDateTime detectedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime resolvedAt;

    public FinancialExceptionResponseDto() {}

    public FinancialExceptionResponseDto(UUID id, String exceptionId, String merchantId, ExceptionType exceptionType, ExceptionSeverity severity, ExceptionStatus status, BigDecimal expectedAmount, BigDecimal actualAmount, BigDecimal discrepancyAmount, String orderId, String paymentId, String refundId, String settlementId, String description, OffsetDateTime detectedAt, OffsetDateTime createdAt, OffsetDateTime resolvedAt) {
        this.id = id;
        this.exceptionId = exceptionId;
        this.merchantId = merchantId;
        this.exceptionType = exceptionType;
        this.severity = severity;
        this.status = status;
        this.expectedAmount = expectedAmount;
        this.actualAmount = actualAmount;
        this.discrepancyAmount = discrepancyAmount;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.refundId = refundId;
        this.settlementId = settlementId;
        this.description = description;
        this.detectedAt = detectedAt;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public static FinancialExceptionResponseDtoBuilder builder() { return new FinancialExceptionResponseDtoBuilder(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getExceptionId() { return exceptionId; }
    public void setExceptionId(String exceptionId) { this.exceptionId = exceptionId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public ExceptionType getExceptionType() { return exceptionType; }
    public void setExceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; }
    public ExceptionSeverity getSeverity() { return severity; }
    public void setSeverity(ExceptionSeverity severity) { this.severity = severity; }
    public ExceptionStatus getStatus() { return status; }
    public void setStatus(ExceptionStatus status) { this.status = status; }
    public BigDecimal getExpectedAmount() { return expectedAmount; }
    public void setExpectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; }
    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }
    public BigDecimal getDiscrepancyAmount() { return discrepancyAmount; }
    public void setDiscrepancyAmount(BigDecimal discrepancyAmount) { this.discrepancyAmount = discrepancyAmount; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }
    public String getSettlementId() { return settlementId; }
    public void setSettlementId(String settlementId) { this.settlementId = settlementId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public OffsetDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(OffsetDateTime detectedAt) { this.detectedAt = detectedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public static class FinancialExceptionResponseDtoBuilder {
        private UUID id;
        private String exceptionId;
        private String merchantId;
        private ExceptionType exceptionType;
        private ExceptionSeverity severity;
        private ExceptionStatus status;
        private BigDecimal expectedAmount;
        private BigDecimal actualAmount;
        private BigDecimal discrepancyAmount;
        private String orderId;
        private String paymentId;
        private String refundId;
        private String settlementId;
        private String description;
        private OffsetDateTime detectedAt;
        private OffsetDateTime createdAt;
        private OffsetDateTime resolvedAt;

        public FinancialExceptionResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public FinancialExceptionResponseDtoBuilder exceptionId(String exceptionId) { this.exceptionId = exceptionId; return this; }
        public FinancialExceptionResponseDtoBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public FinancialExceptionResponseDtoBuilder exceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; return this; }
        public FinancialExceptionResponseDtoBuilder severity(ExceptionSeverity severity) { this.severity = severity; return this; }
        public FinancialExceptionResponseDtoBuilder status(ExceptionStatus status) { this.status = status; return this; }
        public FinancialExceptionResponseDtoBuilder expectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; return this; }
        public FinancialExceptionResponseDtoBuilder actualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; return this; }
        public FinancialExceptionResponseDtoBuilder discrepancyAmount(BigDecimal discrepancyAmount) { this.discrepancyAmount = discrepancyAmount; return this; }
        public FinancialExceptionResponseDtoBuilder orderId(String orderId) { this.orderId = orderId; return this; }
        public FinancialExceptionResponseDtoBuilder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public FinancialExceptionResponseDtoBuilder refundId(String refundId) { this.refundId = refundId; return this; }
        public FinancialExceptionResponseDtoBuilder settlementId(String settlementId) { this.settlementId = settlementId; return this; }
        public FinancialExceptionResponseDtoBuilder description(String description) { this.description = description; return this; }
        public FinancialExceptionResponseDtoBuilder detectedAt(OffsetDateTime detectedAt) { this.detectedAt = detectedAt; return this; }
        public FinancialExceptionResponseDtoBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public FinancialExceptionResponseDtoBuilder resolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }

        public FinancialExceptionResponseDto build() {
            return new FinancialExceptionResponseDto(id, exceptionId, merchantId, exceptionType, severity, status, expectedAmount, actualAmount, discrepancyAmount, orderId, paymentId, refundId, settlementId, description, detectedAt, createdAt, resolvedAt);
        }
    }
}
