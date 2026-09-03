package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvestigationEvidenceDto {

    private ExceptionSummaryDto exception;
    private OrderSummaryDto order;
    private PaymentSummaryDto payment;
    private List<RefundSummaryDto> refunds = new ArrayList<>();
    private List<FeeSummaryDto> fees = new ArrayList<>();
    private List<AdjustmentSummaryDto> adjustments = new ArrayList<>();
    private SettlementSummaryDto settlement;
    private CalculatedAmountsDto calculatedAmounts;
    private String lineage;

    public InvestigationEvidenceDto() {}

    public InvestigationEvidenceDto(ExceptionSummaryDto exception, OrderSummaryDto order, PaymentSummaryDto payment, List<RefundSummaryDto> refunds, List<FeeSummaryDto> fees, List<AdjustmentSummaryDto> adjustments, SettlementSummaryDto settlement, CalculatedAmountsDto calculatedAmounts, String lineage) {
        this.exception = exception;
        this.order = order;
        this.payment = payment;
        this.refunds = refunds != null ? refunds : new ArrayList<>();
        this.fees = fees != null ? fees : new ArrayList<>();
        this.adjustments = adjustments != null ? adjustments : new ArrayList<>();
        this.settlement = settlement;
        this.calculatedAmounts = calculatedAmounts;
        this.lineage = lineage;
    }

    public static InvestigationEvidenceDtoBuilder builder() { return new InvestigationEvidenceDtoBuilder(); }

    public ExceptionSummaryDto getException() { return exception; }
    public void setException(ExceptionSummaryDto exception) { this.exception = exception; }
    public OrderSummaryDto getOrder() { return order; }
    public void setOrder(OrderSummaryDto order) { this.order = order; }
    public PaymentSummaryDto getPayment() { return payment; }
    public void setPayment(PaymentSummaryDto payment) { this.payment = payment; }
    public List<RefundSummaryDto> getRefunds() { return refunds; }
    public void setRefunds(List<RefundSummaryDto> refunds) { this.refunds = refunds; }
    public List<FeeSummaryDto> getFees() { return fees; }
    public void setFees(List<FeeSummaryDto> fees) { this.fees = fees; }
    public List<AdjustmentSummaryDto> getAdjustments() { return adjustments; }
    public void setAdjustments(List<AdjustmentSummaryDto> adjustments) { this.adjustments = adjustments; }
    public SettlementSummaryDto getSettlement() { return settlement; }
    public void setSettlement(SettlementSummaryDto settlement) { this.settlement = settlement; }
    public CalculatedAmountsDto getCalculatedAmounts() { return calculatedAmounts; }
    public void setCalculatedAmounts(CalculatedAmountsDto calculatedAmounts) { this.calculatedAmounts = calculatedAmounts; }
    public String getLineage() { return lineage; }
    public void setLineage(String lineage) { this.lineage = lineage; }

    // Nested DTOs for clean structured evidence serialization

    public static class ExceptionSummaryDto {
        private String exceptionId;
        private String merchantId;
        private ExceptionType exceptionType;
        private ExceptionSeverity severity;
        private ExceptionStatus status;
        private BigDecimal discrepancyAmount;
        private BigDecimal expectedAmount;
        private BigDecimal actualAmount;
        private String description;
        private OffsetDateTime detectedAt;

        public ExceptionSummaryDto() {}
        public ExceptionSummaryDto(String exceptionId, String merchantId, ExceptionType exceptionType, ExceptionSeverity severity, ExceptionStatus status, BigDecimal discrepancyAmount, BigDecimal expectedAmount, BigDecimal actualAmount, String description, OffsetDateTime detectedAt) {
            this.exceptionId = exceptionId;
            this.merchantId = merchantId;
            this.exceptionType = exceptionType;
            this.severity = severity;
            this.status = status;
            this.discrepancyAmount = discrepancyAmount;
            this.expectedAmount = expectedAmount;
            this.actualAmount = actualAmount;
            this.description = description;
            this.detectedAt = detectedAt;
        }
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
        public BigDecimal getDiscrepancyAmount() { return discrepancyAmount; }
        public void setDiscrepancyAmount(BigDecimal discrepancyAmount) { this.discrepancyAmount = discrepancyAmount; }
        public BigDecimal getExpectedAmount() { return expectedAmount; }
        public void setExpectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; }
        public BigDecimal getActualAmount() { return actualAmount; }
        public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public OffsetDateTime getDetectedAt() { return detectedAt; }
        public void setDetectedAt(OffsetDateTime detectedAt) { this.detectedAt = detectedAt; }
    }

    public static class OrderSummaryDto {
        private String orderId;
        private String customerId;
        private String merchantId;
        private BigDecimal amount;
        private String currency;
        private OrderStatus status;
        private OffsetDateTime createdAt;

        public OrderSummaryDto() {}
        public OrderSummaryDto(String orderId, String customerId, String merchantId, BigDecimal amount, String currency, OrderStatus status, OffsetDateTime createdAt) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.merchantId = merchantId;
            this.amount = amount;
            this.currency = currency;
            this.status = status;
            this.createdAt = createdAt;
        }
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class PaymentSummaryDto {
        private String paymentId;
        private String orderId;
        private BigDecimal amount;
        private String currency;
        private PaymentStatus status;
        private PaymentMethod method;
        private String errorCode;
        private String errorDescription;
        private OffsetDateTime createdAt;

        public PaymentSummaryDto() {}
        public PaymentSummaryDto(String paymentId, String orderId, BigDecimal amount, String currency, PaymentStatus status, PaymentMethod method, String errorCode, String errorDescription, OffsetDateTime createdAt) {
            this.paymentId = paymentId;
            this.orderId = orderId;
            this.amount = amount;
            this.currency = currency;
            this.status = status;
            this.method = method;
            this.errorCode = errorCode;
            this.errorDescription = errorDescription;
            this.createdAt = createdAt;
        }
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public PaymentStatus getStatus() { return status; }
        public void setStatus(PaymentStatus status) { this.status = status; }
        public PaymentMethod getMethod() { return method; }
        public void setMethod(PaymentMethod method) { this.method = method; }
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        public String getErrorDescription() { return errorDescription; }
        public void setErrorDescription(String errorDescription) { this.errorDescription = errorDescription; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class RefundSummaryDto {
        private String refundId;
        private String paymentId;
        private BigDecimal amount;
        private RefundStatus status;
        private String reason;
        private OffsetDateTime createdAt;

        public RefundSummaryDto() {}
        public RefundSummaryDto(String refundId, String paymentId, BigDecimal amount, RefundStatus status, String reason, OffsetDateTime createdAt) {
            this.refundId = refundId;
            this.paymentId = paymentId;
            this.amount = amount;
            this.status = status;
            this.reason = reason;
            this.createdAt = createdAt;
        }
        public String getRefundId() { return refundId; }
        public void setRefundId(String refundId) { this.refundId = refundId; }
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public RefundStatus getStatus() { return status; }
        public void setStatus(RefundStatus status) { this.status = status; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class FeeSummaryDto {
        private BigDecimal feeAmount;
        private BigDecimal taxAmount;
        private BigDecimal totalFee;
        private BigDecimal feeRate;

        public FeeSummaryDto() {}
        public FeeSummaryDto(BigDecimal feeAmount, BigDecimal taxAmount, BigDecimal totalFee, BigDecimal feeRate) {
            this.feeAmount = feeAmount;
            this.taxAmount = taxAmount;
            this.totalFee = totalFee;
            this.feeRate = feeRate;
        }
        public BigDecimal getFeeAmount() { return feeAmount; }
        public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }
        public BigDecimal getTaxAmount() { return taxAmount; }
        public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
        public BigDecimal getTotalFee() { return totalFee; }
        public void setTotalFee(BigDecimal totalFee) { this.totalFee = totalFee; }
        public BigDecimal getFeeRate() { return feeRate; }
        public void setFeeRate(BigDecimal feeRate) { this.feeRate = feeRate; }
    }

    public static class AdjustmentSummaryDto {
        private String adjustmentId;
        private BigDecimal amount;
        private AdjustmentType type;
        private String description;

        public AdjustmentSummaryDto() {}
        public AdjustmentSummaryDto(String adjustmentId, BigDecimal amount, AdjustmentType type, String description) {
            this.adjustmentId = adjustmentId;
            this.amount = amount;
            this.type = type;
            this.description = description;
        }
        public String getAdjustmentId() { return adjustmentId; }
        public void setAdjustmentId(String adjustmentId) { this.adjustmentId = adjustmentId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public AdjustmentType getType() { return type; }
        public void setType(AdjustmentType type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class SettlementSummaryDto {
        private String settlementId;
        private String merchantId;
        private BigDecimal grossAmount;
        private BigDecimal totalRefundAmount;
        private BigDecimal totalFeeAmount;
        private BigDecimal totalTaxAmount;
        private BigDecimal totalAdjustmentAmount;
        private BigDecimal expectedNetAmount;
        private BigDecimal actualSettledAmount;
        private SettlementStatus status;
        private String utr;
        private OffsetDateTime settledAt;

        public SettlementSummaryDto() {}
        public SettlementSummaryDto(String settlementId, String merchantId, BigDecimal grossAmount, BigDecimal totalRefundAmount, BigDecimal totalFeeAmount, BigDecimal totalTaxAmount, BigDecimal totalAdjustmentAmount, BigDecimal expectedNetAmount, BigDecimal actualSettledAmount, SettlementStatus status, String utr, OffsetDateTime settledAt) {
            this.settlementId = settlementId;
            this.merchantId = merchantId;
            this.grossAmount = grossAmount;
            this.totalRefundAmount = totalRefundAmount;
            this.totalFeeAmount = totalFeeAmount;
            this.totalTaxAmount = totalTaxAmount;
            this.totalAdjustmentAmount = totalAdjustmentAmount;
            this.expectedNetAmount = expectedNetAmount;
            this.actualSettledAmount = actualSettledAmount;
            this.status = status;
            this.utr = utr;
            this.settledAt = settledAt;
        }
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
        public BigDecimal getExpectedNetAmount() { return expectedNetAmount; }
        public void setExpectedNetAmount(BigDecimal expectedNetAmount) { this.expectedNetAmount = expectedNetAmount; }
        public BigDecimal getActualSettledAmount() { return actualSettledAmount; }
        public void setActualSettledAmount(BigDecimal actualSettledAmount) { this.actualSettledAmount = actualSettledAmount; }
        public SettlementStatus getStatus() { return status; }
        public void setStatus(SettlementStatus status) { this.status = status; }
        public String getUtr() { return utr; }
        public void setUtr(String utr) { this.utr = utr; }
        public OffsetDateTime getSettledAt() { return settledAt; }
        public void setSettledAt(OffsetDateTime settledAt) { this.settledAt = settledAt; }
    }

    public static class CalculatedAmountsDto {
        private BigDecimal grossAmount;
        private BigDecimal totalRefunds;
        private BigDecimal totalFees;
        private BigDecimal totalTaxes;
        private BigDecimal totalAdjustments;
        private BigDecimal expectedSettlement;
        private BigDecimal actualSettlement;
        private BigDecimal discrepancy;

        public CalculatedAmountsDto() {}
        public CalculatedAmountsDto(BigDecimal grossAmount, BigDecimal totalRefunds, BigDecimal totalFees, BigDecimal totalTaxes, BigDecimal totalAdjustments, BigDecimal expectedSettlement, BigDecimal actualSettlement, BigDecimal discrepancy) {
            this.grossAmount = grossAmount;
            this.totalRefunds = totalRefunds;
            this.totalFees = totalFees;
            this.totalTaxes = totalTaxes;
            this.totalAdjustments = totalAdjustments;
            this.expectedSettlement = expectedSettlement;
            this.actualSettlement = actualSettlement;
            this.discrepancy = discrepancy;
        }
        public BigDecimal getGrossAmount() { return grossAmount; }
        public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
        public BigDecimal getTotalRefunds() { return totalRefunds; }
        public void setTotalRefunds(BigDecimal totalRefunds) { this.totalRefunds = totalRefunds; }
        public BigDecimal getTotalFees() { return totalFees; }
        public void setTotalFees(BigDecimal totalFees) { this.totalFees = totalFees; }
        public BigDecimal getTotalTaxes() { return totalTaxes; }
        public void setTotalTaxes(BigDecimal totalTaxes) { this.totalTaxes = totalTaxes; }
        public BigDecimal getTotalAdjustments() { return totalAdjustments; }
        public void setTotalAdjustments(BigDecimal totalAdjustments) { this.totalAdjustments = totalAdjustments; }
        public BigDecimal getExpectedSettlement() { return expectedSettlement; }
        public void setExpectedSettlement(BigDecimal expectedSettlement) { this.expectedSettlement = expectedSettlement; }
        public BigDecimal getActualSettlement() { return actualSettlement; }
        public void setActualSettlement(BigDecimal actualSettlement) { this.actualSettlement = actualSettlement; }
        public BigDecimal getDiscrepancy() { return discrepancy; }
        public void setDiscrepancy(BigDecimal discrepancy) { this.discrepancy = discrepancy; }
    }

    public static class InvestigationEvidenceDtoBuilder {
        private ExceptionSummaryDto exception;
        private OrderSummaryDto order;
        private PaymentSummaryDto payment;
        private List<RefundSummaryDto> refunds = new ArrayList<>();
        private List<FeeSummaryDto> fees = new ArrayList<>();
        private List<AdjustmentSummaryDto> adjustments = new ArrayList<>();
        private SettlementSummaryDto settlement;
        private CalculatedAmountsDto calculatedAmounts;
        private String lineage;

        public InvestigationEvidenceDtoBuilder exception(ExceptionSummaryDto exception) { this.exception = exception; return this; }
        public InvestigationEvidenceDtoBuilder order(OrderSummaryDto order) { this.order = order; return this; }
        public InvestigationEvidenceDtoBuilder payment(PaymentSummaryDto payment) { this.payment = payment; return this; }
        public InvestigationEvidenceDtoBuilder refunds(List<RefundSummaryDto> refunds) { this.refunds = refunds; return this; }
        public InvestigationEvidenceDtoBuilder fees(List<FeeSummaryDto> fees) { this.fees = fees; return this; }
        public InvestigationEvidenceDtoBuilder adjustments(List<AdjustmentSummaryDto> adjustments) { this.adjustments = adjustments; return this; }
        public InvestigationEvidenceDtoBuilder settlement(SettlementSummaryDto settlement) { this.settlement = settlement; return this; }
        public InvestigationEvidenceDtoBuilder calculatedAmounts(CalculatedAmountsDto calculatedAmounts) { this.calculatedAmounts = calculatedAmounts; return this; }
        public InvestigationEvidenceDtoBuilder lineage(String lineage) { this.lineage = lineage; return this; }

        public InvestigationEvidenceDto build() {
            return new InvestigationEvidenceDto(exception, order, payment, refunds, fees, adjustments, settlement, calculatedAmounts, lineage);
        }
    }
}
