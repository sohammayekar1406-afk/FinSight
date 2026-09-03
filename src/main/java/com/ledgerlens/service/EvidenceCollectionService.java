package com.ledgerlens.service;

import com.ledgerlens.dto.InvestigationEvidenceDto;
import com.ledgerlens.entity.*;
import com.ledgerlens.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EvidenceCollectionService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final FeeRepository feeRepository;
    private final AdjustmentRepository adjustmentRepository;
    private final SettlementRepository settlementRepository;

    public EvidenceCollectionService(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            RefundRepository refundRepository,
            FeeRepository feeRepository,
            AdjustmentRepository adjustmentRepository,
            SettlementRepository settlementRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.feeRepository = feeRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.settlementRepository = settlementRepository;
    }

    @Transactional(readOnly = true)
    public InvestigationEvidenceDto collectEvidence(FinancialException exception) {
        // 1. Resolve primary entities
        Order order = exception.getOrder();
        Payment payment = exception.getPayment();
        Refund refund = exception.getRefund();
        Settlement settlement = exception.getSettlement();

        // Traversals
        if (order == null && payment != null) {
            order = payment.getOrder();
        }
        if (payment == null && order != null) {
            List<Payment> orderPayments = paymentRepository.findByOrder_OrderId(order.getOrderId());
            if (!orderPayments.isEmpty()) {
                payment = orderPayments.get(0);
            }
        }
        if (settlement == null && payment != null) {
            settlement = payment.getSettlement();
        }

        // 2. Gather Refunds
        List<Refund> refundList = new ArrayList<>();
        if (refund != null) {
            refundList.add(refund);
        }
        if (payment != null) {
            List<Refund> pRefunds = refundRepository.findByPayment_PaymentId(payment.getPaymentId());
            for (Refund r : pRefunds) {
                if (refundList.stream().noneMatch(existing -> existing.getId().equals(r.getId()))) {
                    refundList.add(r);
                }
            }
        }
        if (settlement != null) {
            List<Refund> sRefunds = refundRepository.findBySettlement_SettlementId(settlement.getSettlementId());
            for (Refund r : sRefunds) {
                if (refundList.stream().noneMatch(existing -> existing.getId().equals(r.getId()))) {
                    refundList.add(r);
                }
            }
        }

        // 3. Gather Fees
        List<Fee> feeList = new ArrayList<>();
        if (payment != null) {
            feeList.addAll(feeRepository.findByPayment_PaymentId(payment.getPaymentId()));
        }
        if (refund != null) {
            List<Fee> rFees = feeRepository.findByRefund_RefundId(refund.getRefundId());
            for (Fee f : rFees) {
                if (feeList.stream().noneMatch(existing -> existing.getId().equals(f.getId()))) {
                    feeList.add(f);
                }
            }
        }

        // 4. Gather Adjustments
        List<Adjustment> adjustmentList = new ArrayList<>();
        if (payment != null) {
            adjustmentList.addAll(adjustmentRepository.findByPayment_PaymentId(payment.getPaymentId()));
        }
        if (settlement != null) {
            List<Adjustment> sAdjustments = adjustmentRepository.findBySettlement_SettlementId(settlement.getSettlementId());
            for (Adjustment a : sAdjustments) {
                if (adjustmentList.stream().noneMatch(existing -> existing.getId().equals(a.getId()))) {
                    adjustmentList.add(a);
                }
            }
        }

        // 5. Build DTO Summaries
        InvestigationEvidenceDto.ExceptionSummaryDto exDto = new InvestigationEvidenceDto.ExceptionSummaryDto(
                exception.getExceptionId(),
                exception.getMerchantId(),
                exception.getExceptionType(),
                exception.getSeverity(),
                exception.getStatus(),
                exception.getDiscrepancyAmount(),
                exception.getExpectedAmount(),
                exception.getActualAmount(),
                exception.getDescription(),
                exception.getDetectedAt()
        );

        InvestigationEvidenceDto.OrderSummaryDto orderDto = order != null ? new InvestigationEvidenceDto.OrderSummaryDto(
                order.getOrderId(), order.getCustomerId(), order.getMerchantId(), order.getAmount(), order.getCurrency(), order.getStatus(), order.getCreatedAt()
        ) : null;

        InvestigationEvidenceDto.PaymentSummaryDto paymentDto = payment != null ? new InvestigationEvidenceDto.PaymentSummaryDto(
                payment.getPaymentId(), payment.getOrder() != null ? payment.getOrder().getOrderId() : null, payment.getAmount(), payment.getCurrency(), payment.getStatus(), payment.getMethod(), payment.getErrorCode(), payment.getErrorDescription(), payment.getCreatedAt()
        ) : null;

        List<InvestigationEvidenceDto.RefundSummaryDto> refundDtos = refundList.stream().map(r ->
                new InvestigationEvidenceDto.RefundSummaryDto(r.getRefundId(), r.getPayment() != null ? r.getPayment().getPaymentId() : null, r.getAmount(), r.getStatus(), r.getReason(), r.getCreatedAt())
        ).collect(Collectors.toList());

        List<InvestigationEvidenceDto.FeeSummaryDto> feeDtos = feeList.stream().map(f ->
                new InvestigationEvidenceDto.FeeSummaryDto(f.getFeeAmount(), f.getTaxAmount(), f.getTotalFee(), f.getFeeRate())
        ).collect(Collectors.toList());

        List<InvestigationEvidenceDto.AdjustmentSummaryDto> adjustmentDtos = adjustmentList.stream().map(a ->
                new InvestigationEvidenceDto.AdjustmentSummaryDto(a.getAdjustmentId(), a.getAmount(), a.getType(), a.getDescription())
        ).collect(Collectors.toList());

        // 6. Calculate Financial Aggregates
        BigDecimal totalRefunds = refundDtos.stream().map(InvestigationEvidenceDto.RefundSummaryDto::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFees = feeDtos.stream().map(InvestigationEvidenceDto.FeeSummaryDto::getTotalFee).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTaxes = feeDtos.stream().map(InvestigationEvidenceDto.FeeSummaryDto::getTaxAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAdjustments = adjustmentDtos.stream().map(InvestigationEvidenceDto.AdjustmentSummaryDto::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossAmount = settlement != null ? settlement.getGrossAmount() : (payment != null ? payment.getAmount() : (order != null ? order.getAmount() : BigDecimal.ZERO));
        BigDecimal actualSettlement = settlement != null ? (settlement.getActualSettledAmount() != null ? settlement.getActualSettledAmount() : BigDecimal.ZERO) : exception.getActualAmount();

        BigDecimal expectedSettlement;
        if (settlement != null) {
            expectedSettlement = settlement.getGrossAmount()
                    .subtract(settlement.getTotalRefundAmount())
                    .subtract(settlement.getTotalFeeAmount())
                    .subtract(settlement.getTotalTaxAmount())
                    .add(settlement.getTotalAdjustmentAmount());
        } else if (exception.getExpectedAmount() != null) {
            expectedSettlement = exception.getExpectedAmount();
        } else {
            expectedSettlement = grossAmount;
        }

        BigDecimal discrepancy = exception.getDiscrepancyAmount() != null ? exception.getDiscrepancyAmount() : expectedSettlement.subtract(actualSettlement != null ? actualSettlement : BigDecimal.ZERO).abs();

        InvestigationEvidenceDto.SettlementSummaryDto settlementDto = settlement != null ? new InvestigationEvidenceDto.SettlementSummaryDto(
                settlement.getSettlementId(), settlement.getMerchantId(), settlement.getGrossAmount(), settlement.getTotalRefundAmount(), settlement.getTotalFeeAmount(), settlement.getTotalTaxAmount(), settlement.getTotalAdjustmentAmount(), expectedSettlement, actualSettlement, settlement.getStatus(), settlement.getUtr(), settlement.getSettledAt()
        ) : null;

        InvestigationEvidenceDto.CalculatedAmountsDto calculatedAmounts = new InvestigationEvidenceDto.CalculatedAmountsDto(
                grossAmount, totalRefunds, totalFees, totalTaxes, totalAdjustments, expectedSettlement, actualSettlement, discrepancy
        );

        // 7. Construct Transaction Lineage String
        StringBuilder lineageBuilder = new StringBuilder();
        if (order != null) lineageBuilder.append("Order ").append(order.getOrderId()).append(" → ");
        if (payment != null) lineageBuilder.append("Payment ").append(payment.getPaymentId()).append(" → ");
        if (refund != null) lineageBuilder.append("Refund ").append(refund.getRefundId()).append(" → ");
        if (settlement != null) lineageBuilder.append("Settlement ").append(settlement.getSettlementId()).append(" → ");
        lineageBuilder.append(exception.getExceptionType().name()).append(" ").append(exception.getExceptionId());

        return InvestigationEvidenceDto.builder()
                .exception(exDto)
                .order(orderDto)
                .payment(paymentDto)
                .refunds(refundDtos)
                .fees(feeDtos)
                .adjustments(adjustmentDtos)
                .settlement(settlementDto)
                .calculatedAmounts(calculatedAmounts)
                .lineage(lineageBuilder.toString())
                .build();
    }
}
