package com.ledgerlens.service;

import com.ledgerlens.dto.*;
import com.ledgerlens.entity.enums.ExceptionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Phase 3.5: Evidence Graph Service
 * 
 * Converts collected evidence into a structured evidence graph with provenance
 * and calculates evidence sufficiency scores deterministically.
 * 
 * CORE PRINCIPLE:
 * Backend determines financial truth.
 * Backend retrieves evidence.
 * This service structures that evidence for AI consumption.
 * AI reasons over the evidence - it does NOT generate it.
 */
@Service
public class EvidenceGraphService {

    /**
     * Build evidence graph from collected evidence
     */
    public EvidenceGraphDto buildEvidenceGraph(InvestigationEvidenceDto evidence, ExceptionType exceptionType) {
        List<EvidenceNodeDto> nodes = new ArrayList<>();

        // Exception node (always present)
        if (evidence.getException() != null) {
            nodes.add(EvidenceNodeDto.builder()
                    .entityType(EvidenceNodeDto.EntityType.EXCEPTION)
                    .entityId(evidence.getException().getExceptionId())
                    .availability(EvidenceNodeDto.AvailabilityStatus.FOUND)
                    .relationshipToException("PRIMARY_EXCEPTION")
                    .source("financial_exceptions table")
                    .amount(evidence.getException().getDiscrepancyAmount())
                    .currency("INR")
                    .status(evidence.getException().getStatus().name())
                    .timestamp(evidence.getException().getDetectedAt())
                    .relevanceReason("The exception being investigated")
                    .build());
        }

        // Order node
        if (evidence.getOrder() != null) {
            nodes.add(EvidenceNodeDto.builder()
                    .entityType(EvidenceNodeDto.EntityType.ORDER)
                    .entityId(evidence.getOrder().getOrderId())
                    .availability(EvidenceNodeDto.AvailabilityStatus.FOUND)
                    .relationshipToException("ORIGINATING_ORDER")
                    .source("orders table")
                    .amount(evidence.getOrder().getAmount())
                    .currency(evidence.getOrder().getCurrency())
                    .status(evidence.getOrder().getStatus().name())
                    .timestamp(evidence.getOrder().getCreatedAt())
                    .relevanceReason("Original customer order that initiated this transaction flow")
                    .build());
        } else if (requiresOrder(exceptionType)) {
            nodes.add(EvidenceNodeDto.builder()
                    .entityType(EvidenceNodeDto.EntityType.ORDER)
                    .entityId(null)
                    .availability(EvidenceNodeDto.AvailabilityStatus.MISSING)
                    .relationshipToException("ORIGINATING_ORDER")
                    .source("orders table")
                    .relevanceReason("Expected order record is missing")
                    .build());
        }

        // Payment node
        if (evidence.getPayment() != null) {
            nodes.add(EvidenceNodeDto.builder()
                    .entityType(EvidenceNodeDto.EntityType.PAYMENT)
                    .entityId(evidence.getPayment().getPaymentId())
                    .availability(EvidenceNodeDto.AvailabilityStatus.FOUND)
                    .relationshipToException("PRIMARY_PAYMENT")
                    .source("payments table")
                    .amount(evidence.getPayment().getAmount())
                    .currency(evidence.getPayment().getCurrency())
                    .status(evidence.getPayment().getStatus().name())
                    .timestamp(evidence.getPayment().getCreatedAt())
                    .relevanceReason("Payment transaction linked to this exception")
                    .build());
        } else if (requiresPayment(exceptionType)) {
            nodes.add(EvidenceNodeDto.builder()
                    .entityType(EvidenceNodeDto.EntityType.PAYMENT)
                    .entityId(null)
                    .availability(EvidenceNodeDto.AvailabilityStatus.MISSING)
                    .relationshipToException("PRIMARY_PAYMENT")
                    .source("payments table")
                    .relevanceReason("Expected payment record is missing")
                    .build());
        }

        // Refund nodes
        if (evidence.getRefunds() != null && !evidence.getRefunds().isEmpty()) {
            for (InvestigationEvidenceDto.RefundSummaryDto refund : evidence.getRefunds()) {
                nodes.add(EvidenceNodeDto.builder()
                        .entityType(EvidenceNodeDto.EntityType.REFUND)
                        .entityId(refund.getRefundId())
                        .availability(EvidenceNodeDto.AvailabilityStatus.FOUND)
                        .relationshipToException("LINKED_REFUND")
                        .source("refunds table")
                        .amount(refund.getAmount())
                        .status(refund.getStatus().name())
                        .timestamp(refund.getCreatedAt())
                        .relevanceReason("Refund that may explain discrepancy: " + refund.getReason())
                        .build());
            }
        } else if (requiresRefund(exceptionType)) {
            nodes.add(EvidenceNodeDto.builder()
                    .entityType(EvidenceNodeDto.EntityType.REFUND)
                    .entityId(null)
                    .availability(EvidenceNodeDto.AvailabilityStatus.MISSING)
                    .relationshipToException("LINKED_REFUND")
                    .source("refunds table")
                    .relevanceReason("Expected refund record for this discrepancy type")
                    .build());
        }

        // Fee nodes
        if (evidence.getFees() != null && !evidence.getFees().isEmpty()) {
            int feeIndex = 0;
            for (InvestigationEvidenceDto.FeeSummaryDto fee : evidence.getFees()) {
                nodes.add(EvidenceNodeDto.builder()
                        .entityType(EvidenceNodeDto.EntityType.FEE)
                        .entityId("fee_" + feeIndex++)
                        .availability(EvidenceNodeDto.AvailabilityStatus.FOUND)
                        .relationshipToException("APPLIED_FEE")
                        .source("fees table")
                        .amount(fee.getTotalFee())
                        .relevanceReason("Fee deducted from settlement (rate: " + fee.getFeeRate() + "%)")
                        .build());
            }
        }

        // Adjustment nodes
        if (evidence.getAdjustments() != null && !evidence.getAdjustments().isEmpty()) {
            for (InvestigationEvidenceDto.AdjustmentSummaryDto adj : evidence.getAdjustments()) {
                nodes.add(EvidenceNodeDto.builder()
                        .entityType(EvidenceNodeDto.EntityType.ADJUSTMENT)
                        .entityId(adj.getAdjustmentId())
                        .availability(EvidenceNodeDto.AvailabilityStatus.FOUND)
                        .relationshipToException("APPLIED_ADJUSTMENT")
                        .source("adjustments table")
                        .amount(adj.getAmount())
                        .relevanceReason("Adjustment: " + adj.getType() + " - " + adj.getDescription())
                        .build());
            }
        }

        // Settlement node
        if (evidence.getSettlement() != null) {
            nodes.add(EvidenceNodeDto.builder()
                    .entityType(EvidenceNodeDto.EntityType.SETTLEMENT)
                    .entityId(evidence.getSettlement().getSettlementId())
                    .availability(EvidenceNodeDto.AvailabilityStatus.FOUND)
                    .relationshipToException("SETTLEMENT")
                    .source("settlements table")
                    .amount(evidence.getSettlement().getActualSettledAmount())
                    .status(evidence.getSettlement().getStatus().name())
                    .timestamp(evidence.getSettlement().getSettledAt())
                    .relevanceReason("Final settlement amount transferred to merchant")
                    .build());
        } else if (requiresSettlement(exceptionType)) {
            nodes.add(EvidenceNodeDto.builder()
                    .entityType(EvidenceNodeDto.EntityType.SETTLEMENT)
                    .entityId(null)
                    .availability(EvidenceNodeDto.AvailabilityStatus.MISSING)
                    .relationshipToException("SETTLEMENT")
                    .source("settlements table")
                    .relevanceReason("Expected settlement record is missing")
                    .build());
        }

        // Count found vs missing
        long foundCount = nodes.stream()
                .filter(n -> n.getAvailability() == EvidenceNodeDto.AvailabilityStatus.FOUND)
                .count();
        long missingCount = nodes.stream()
                .filter(n -> n.getAvailability() == EvidenceNodeDto.AvailabilityStatus.MISSING)
                .count();

        return EvidenceGraphDto.builder()
                .nodes(nodes)
                .transactionFlow(evidence.getLineage())
                .totalNodesRetrieved(nodes.size())
                .foundNodes((int) foundCount)
                .missingNodes((int) missingCount)
                .build();
    }

    /**
     * Calculate evidence sufficiency deterministically based on exception type
     */
    public EvidenceSufficiencyDto calculateSufficiency(EvidenceGraphDto graph, ExceptionType exceptionType) {
        List<String> found = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (EvidenceNodeDto node : graph.getNodes()) {
            String evidenceLabel = node.getEntityType().name() + 
                    (node.getEntityId() != null ? " (" + node.getEntityId() + ")" : "");

            if (node.getAvailability() == EvidenceNodeDto.AvailabilityStatus.FOUND) {
                found.add(evidenceLabel);
            } else if (node.getAvailability() == EvidenceNodeDto.AvailabilityStatus.MISSING) {
                missing.add(evidenceLabel);
            }
        }

        // Determine required evidence based on exception type
        List<String> required = getRequiredEvidence(exceptionType);
        
        // Calculate score: % of required evidence that was found
        long requiredFound = required.stream()
                .filter(req -> found.stream().anyMatch(f -> f.startsWith(req)))
                .count();

        BigDecimal score = required.isEmpty() ? BigDecimal.valueOf(100) :
                BigDecimal.valueOf(requiredFound)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(required.size()), 2, RoundingMode.HALF_UP);

        String assessment;
        String reasoning;

        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            assessment = "SUFFICIENT";
            reasoning = "All or most critical evidence is available for forensic analysis";
        } else if (score.compareTo(BigDecimal.valueOf(50)) >= 0) {
            assessment = "PARTIAL";
            reasoning = "Some evidence is missing; analysis may be limited or inconclusive";
        } else {
            assessment = "INSUFFICIENT";
            reasoning = "Critical evidence is missing; high-confidence analysis not possible";
        }

        return EvidenceSufficiencyDto.builder()
                .sufficiencyScore(score)
                .assessment(assessment)
                .foundEvidence(found)
                .missingEvidence(missing)
                .reasoning(reasoning)
                .build();
    }

    /**
     * Determine required evidence based on exception type
     */
    private List<String> getRequiredEvidence(ExceptionType exceptionType) {
        return switch (exceptionType) {
            case MISSING_PAYMENT -> List.of("ORDER", "PAYMENT");
            case AMOUNT_MISMATCH -> List.of("ORDER", "PAYMENT", "SETTLEMENT");
            case MISSING_SETTLEMENT -> List.of("PAYMENT", "SETTLEMENT");
            case DISCREPANT_REFUND -> List.of("PAYMENT", "REFUND");
            case UNEXPECTED_FEE -> List.of("PAYMENT", "FEE", "SETTLEMENT");
            case CURRENCY_MISMATCH -> List.of("PAYMENT", "ORDER");
            case DUPLICATE_TRANSACTION -> List.of("PAYMENT");
            case UNMATCHED_ADJUSTMENT -> List.of("PAYMENT", "SETTLEMENT", "ADJUSTMENT");
            case DELAYED_SETTLEMENT -> List.of("PAYMENT", "SETTLEMENT");
            case UNKNOWN_TRANSACTION -> List.of("PAYMENT");
            case DATA_INCOMPLETE -> List.of("ORDER", "PAYMENT");
            default -> List.of("PAYMENT"); // Minimal requirement
        };
    }

    private boolean requiresOrder(ExceptionType type) {
        return type == ExceptionType.MISSING_PAYMENT || 
               type == ExceptionType.AMOUNT_MISMATCH ||
               type == ExceptionType.CURRENCY_MISMATCH ||
               type == ExceptionType.DATA_INCOMPLETE;
    }

    private boolean requiresPayment(ExceptionType type) {
        // Most exception types require payment
        return type != ExceptionType.DATA_INCOMPLETE;
    }

    private boolean requiresRefund(ExceptionType type) {
        return type == ExceptionType.DISCREPANT_REFUND;
    }

    private boolean requiresSettlement(ExceptionType type) {
        return type == ExceptionType.AMOUNT_MISMATCH ||
               type == ExceptionType.MISSING_SETTLEMENT ||
               type == ExceptionType.UNEXPECTED_FEE ||
               type == ExceptionType.UNMATCHED_ADJUSTMENT ||
               type == ExceptionType.DELAYED_SETTLEMENT;
    }
}
