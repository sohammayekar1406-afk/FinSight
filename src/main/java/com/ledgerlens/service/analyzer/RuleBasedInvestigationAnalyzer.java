package com.ledgerlens.service.analyzer;

import com.ledgerlens.dto.InvestigationAnalysis;
import com.ledgerlens.dto.InvestigationEvidenceDto;
import com.ledgerlens.entity.enums.ActionTaken;
import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.entity.enums.RecommendedAction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RuleBasedInvestigationAnalyzer implements InvestigationAnalyzer {

    @Override
    public InvestigationAnalysis analyze(InvestigationEvidenceDto evidence) {
        InvestigationEvidenceDto.ExceptionSummaryDto ex = evidence.getException();
        ExceptionType type = ex != null ? ex.getExceptionType() : ExceptionType.UNKNOWN_TRANSACTION;

        return switch (type) {
            case AMOUNT_MISMATCH -> analyzeAmountMismatch(evidence);
            case MISSING_PAYMENT -> analyzeMissingPayment(evidence);
            case MISSING_SETTLEMENT -> analyzeMissingSettlement(evidence);
            case UNEXPECTED_FEE -> analyzeUnexpectedFee(evidence);
            case DUPLICATE_TRANSACTION -> analyzeDuplicateTransaction(evidence);
            case DELAYED_SETTLEMENT -> analyzeDelayedSettlement(evidence);
            case DISCREPANT_REFUND -> analyzeDiscrepantRefund(evidence);
            case UNKNOWN_TRANSACTION -> analyzeUnknownTransaction(evidence);
            case UNMATCHED_ADJUSTMENT -> analyzeUnmatchedAdjustment(evidence);
            case CURRENCY_MISMATCH -> analyzeCurrencyMismatch(evidence);
            case DATA_INCOMPLETE -> analyzeDataIncomplete(evidence);
        };
    }

    private InvestigationAnalysis analyzeAmountMismatch(InvestigationEvidenceDto evidence) {
        InvestigationEvidenceDto.CalculatedAmountsDto calc = evidence.getCalculatedAmounts();
        BigDecimal discrepancy = calc != null && calc.getDiscrepancy() != null ? calc.getDiscrepancy() : BigDecimal.ZERO;
        BigDecimal totalRefunds = calc != null ? calc.getTotalRefunds() : BigDecimal.ZERO;
        BigDecimal totalFees = calc != null ? calc.getTotalFees() : BigDecimal.ZERO;
        BigDecimal totalAdjustments = calc != null ? calc.getTotalAdjustments() : BigDecimal.ZERO;

        // Check if discrepancy is fully explained by recorded adjustments or refunds
        boolean isExplained = (discrepancy.compareTo(BigDecimal.ZERO) > 0) &&
                (discrepancy.compareTo(totalAdjustments) == 0 || discrepancy.compareTo(totalRefunds) == 0 || discrepancy.compareTo(totalFees) == 0);

        if (isExplained) {
            return InvestigationAnalysis.builder()
                    .summary("Settlement discrepancy of ₹" + discrepancy + " is fully explained by recorded adjustments/fees.")
                    .likelyRootCause("Discrepancy is accounted for by known transaction adjustments.")
                    .confidenceScore(new BigDecimal("100.00"))
                    .recommendedAction(RecommendedAction.AUTO_RESOLVE)
                    .actionTaken(ActionTaken.AUTO_RESOLVED)
                    .autoResolved(true)
                    .evidence(evidence)
                    .build();
        }

        // Unexplained discrepancy (e.g. ₹500 payout shortfall)
        String summary = "Settlement payout is ₹" + discrepancy + " lower than the expected net amount.";
        String likelyRootCause = "₹" + discrepancy + " remains unexplained after accounting for refunds, fees, taxes, and adjustments.";

        return InvestigationAnalysis.builder()
                .summary(summary)
                .likelyRootCause(likelyRootCause)
                .confidenceScore(new BigDecimal("96.00"))
                .recommendedAction(RecommendedAction.HUMAN_REVIEW_REQUIRED)
                .actionTaken(ActionTaken.SENT_TO_HUMAN)
                .autoResolved(false)
                .evidence(evidence)
                .build();
    }

    private InvestigationAnalysis analyzeMissingPayment(InvestigationEvidenceDto evidence) {
        String orderId = evidence.getOrder() != null ? evidence.getOrder().getOrderId() : "UNKNOWN";
        return InvestigationAnalysis.builder()
                .summary("Order " + orderId + " is marked PAID but lacks a corresponding successful payment record.")
                .likelyRootCause("Order state was updated to PAID without gateway payment confirmation payload.")
                .confidenceScore(new BigDecimal("100.00"))
                .recommendedAction(RecommendedAction.HUMAN_REVIEW_REQUIRED)
                .actionTaken(ActionTaken.SENT_TO_HUMAN)
                .autoResolved(false)
                .evidence(evidence)
                .build();
    }

    private InvestigationAnalysis analyzeMissingSettlement(InvestigationEvidenceDto evidence) {
        String paymentId = evidence.getPayment() != null ? evidence.getPayment().getPaymentId() : "UNKNOWN";
        return InvestigationAnalysis.builder()
                .summary("Payment " + paymentId + " remains unsettled beyond the threshold window.")
                .likelyRootCause("Payment was successful but payout payload was not captured within the 24-hour settlement SLA window.")
                .confidenceScore(new BigDecimal("95.00"))
                .recommendedAction(RecommendedAction.RETRY_SETTLEMENT)
                .actionTaken(ActionTaken.SENT_TO_HUMAN)
                .autoResolved(false)
                .evidence(evidence)
                .build();
    }

    private InvestigationAnalysis analyzeUnexpectedFee(InvestigationEvidenceDto evidence) {
        return InvestigationAnalysis.builder()
                .summary("Fee total calculation mismatch detected.")
                .likelyRootCause("Calculated total fee does not match feeAmount plus taxAmount.")
                .confidenceScore(new BigDecimal("100.00"))
                .recommendedAction(RecommendedAction.MANUAL_ADJUSTMENT)
                .actionTaken(ActionTaken.SENT_TO_HUMAN)
                .autoResolved(false)
                .evidence(evidence)
                .build();
    }

    private InvestigationAnalysis analyzeDuplicateTransaction(InvestigationEvidenceDto evidence) {
        String paymentId = evidence.getPayment() != null ? evidence.getPayment().getPaymentId() : "UNKNOWN";
        return InvestigationAnalysis.builder()
                .summary("Duplicate payment transaction identifier detected.")
                .likelyRootCause("Multiple payment records exist for paymentId " + paymentId + ".")
                .confidenceScore(new BigDecimal("100.00"))
                .recommendedAction(RecommendedAction.HUMAN_REVIEW_REQUIRED)
                .actionTaken(ActionTaken.SENT_TO_HUMAN)
                .autoResolved(false)
                .evidence(evidence)
                .build();
    }

    private InvestigationAnalysis analyzeDelayedSettlement(InvestigationEvidenceDto evidence) {
        return InvestigationAnalysis.builder()
                .summary("Settlement payout was delayed beyond normal processing window.")
                .likelyRootCause("Settlement timestamp exceeds standard payment SLA threshold.")
                .confidenceScore(new BigDecimal("90.00"))
                .recommendedAction(RecommendedAction.HUMAN_REVIEW_REQUIRED)
                .actionTaken(ActionTaken.SENT_TO_HUMAN)
                .autoResolved(false)
                .evidence(evidence)
                .build();
    }

    private InvestigationAnalysis analyzeDiscrepantRefund(InvestigationEvidenceDto evidence) {
        return InvestigationAnalysis.builder()
                .summary("Total refund amount exceeds original payment principal.")
                .likelyRootCause("Multiple or excessive refund transactions issued against payment.")
                .confidenceScore(new BigDecimal("100.00"))
                .recommendedAction(RecommendedAction.HUMAN_REVIEW_REQUIRED)
                .actionTaken(ActionTaken.SENT_TO_HUMAN)
                .autoResolved(false)
                .evidence(evidence)
                .build();
    }

    private InvestigationAnalysis analyzeUnknownTransaction(InvestigationEvidenceDto evidence) {
        return InvestigationAnalysis.builder()
                .summary("Transaction cannot be linked to a known order or payment lineage.")
                .likelyRootCause("Transaction could not be linked to a known order or customer record.")
                .confidenceScore(new BigDecimal("100.00"))
                .recommendedAction(RecommendedAction.HUMAN_REVIEW_REQUIRED)
                .actionTaken(ActionTaken.SENT_TO_HUMAN)
                .autoResolved(false)
                .evidence(evidence)
                .build();
    }

    private InvestigationAnalysis analyzeUnmatchedAdjustment(InvestigationEvidenceDto evidence) {
        return InvestigationAnalysis.builder()
                .summary("Manual adjustment or chargeback debit/credit lacks valid reference or lineage mapping.")
                .likelyRootCause("An adjustment transaction exists but does not reference a valid payment or settlement.")
                .confidenceScore(new BigDecimal("95.00"))
                .recommendedAction(RecommendedAction.MANUAL_ADJUSTMENT)
                .actionTaken(ActionTaken.SENT_TO_HUMAN)
                .autoResolved(false)
                .evidence(evidence)
                .build();
    }

    private InvestigationAnalysis analyzeCurrencyMismatch(InvestigationEvidenceDto evidence) {
        return InvestigationAnalysis.builder()
                .summary("Transaction ISO currency codes mismatch across order, payment, or settlement records.")
                .likelyRootCause("Currency mismatch detected between the purchase currency and settlement payout currency.")
                .confidenceScore(new BigDecimal("100.00"))
                .recommendedAction(RecommendedAction.HUMAN_REVIEW_REQUIRED)
                .actionTaken(ActionTaken.SENT_TO_HUMAN)
                .autoResolved(false)
                .evidence(evidence)
                .build();
    }

    private InvestigationAnalysis analyzeDataIncomplete(InvestigationEvidenceDto evidence) {
        return InvestigationAnalysis.builder()
                .summary("Reconciliation could not complete because required financial data is missing.")
                .likelyRootCause("An upstream transaction payload is incomplete; no financial value was inferred.")
                .confidenceScore(new BigDecimal("100.00"))
                .recommendedAction(RecommendedAction.HUMAN_REVIEW_REQUIRED)
                .actionTaken(ActionTaken.SENT_TO_HUMAN)
                .autoResolved(false)
                .evidence(evidence)
                .build();
    }
}
