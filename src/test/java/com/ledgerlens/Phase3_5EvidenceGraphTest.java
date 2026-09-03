package com.ledgerlens;

import com.ledgerlens.dto.*;
import com.ledgerlens.entity.enums.*;
import com.ledgerlens.service.EvidenceGraphService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3.5: Evidence Graph + Evidence Sufficiency Tests
 * 
 * Tests the evidence graph construction, sufficiency scoring,
 * missing evidence detection, and provenance tracking.
 */
@DisplayName("Phase 3.5: Evidence Graph & Sufficiency")
public class Phase3_5EvidenceGraphTest {

    private final EvidenceGraphService evidenceGraphService = new EvidenceGraphService();

    @Test
    @DisplayName("1. Evidence Graph: Complete Transaction - All Evidence Found")
    void testCompleteEvidenceGraph() {
        // Arrange: Build complete evidence with Order, Payment, Refund, Settlement
        InvestigationEvidenceDto evidence = buildCompleteEvidence();
        
        // Act
        EvidenceGraphDto graph = evidenceGraphService.buildEvidenceGraph(evidence, ExceptionType.AMOUNT_MISMATCH);
        
        // Assert
        assertNotNull(graph);
        assertNotNull(graph.getNodes());
        assertTrue(graph.getFoundNodes() > 0, "Should have found evidence nodes");
        assertEquals(0, graph.getMissingNodes(), "Should have no missing evidence");
        
        // Verify provenance
        boolean hasProvenanceInfo = graph.getNodes().stream()
                .allMatch(node -> node.getSource() != null && !node.getSource().isEmpty());
        assertTrue(hasProvenanceInfo, "All nodes must have source/provenance information");
        
        // Verify availability status
        long foundCount = graph.getNodes().stream()
                .filter(n -> n.getAvailability() == EvidenceNodeDto.AvailabilityStatus.FOUND)
                .count();
        assertEquals(graph.getFoundNodes(), foundCount);
    }

    @Test
    @DisplayName("2. Evidence Sufficiency: Complete Evidence -> SUFFICIENT")
    void testSufficientEvidence() {
        // Arrange
        InvestigationEvidenceDto evidence = buildCompleteEvidence();
        EvidenceGraphDto graph = evidenceGraphService.buildEvidenceGraph(evidence, ExceptionType.AMOUNT_MISMATCH);
        
        // Act
        EvidenceSufficiencyDto sufficiency = evidenceGraphService.calculateSufficiency(graph, ExceptionType.AMOUNT_MISMATCH);
        
        // Assert
        assertNotNull(sufficiency);
        assertEquals("SUFFICIENT", sufficiency.getAssessment());
        assertTrue(sufficiency.getSufficiencyScore().compareTo(BigDecimal.valueOf(80)) >= 0, 
                   "Sufficient evidence should score >= 80%");
        assertNotNull(sufficiency.getFoundEvidence());
        assertFalse(sufficiency.getFoundEvidence().isEmpty(), "Should list found evidence");
        assertNotNull(sufficiency.getReasoning(), "Should provide reasoning for score");
    }

    @Test
    @DisplayName("3. Missing Evidence: MISSING_PAYMENT Type Without Order")
    void testMissingOrderEvidence() {
        // Arrange: Evidence without Order
        InvestigationEvidenceDto evidence = buildEvidenceWithoutOrder();
        EvidenceGraphDto graph = evidenceGraphService.buildEvidenceGraph(evidence, ExceptionType.MISSING_PAYMENT);
        
        // Act
        EvidenceSufficiencyDto sufficiency = evidenceGraphService.calculateSufficiency(graph, ExceptionType.MISSING_PAYMENT);
        
        // Assert
        assertTrue(graph.getMissingNodes() > 0, "Should detect missing order");
        
        // Verify missing evidence is explicitly tracked
        boolean hasMissingOrder = graph.getNodes().stream()
                .anyMatch(n -> n.getEntityType() == EvidenceNodeDto.EntityType.ORDER &&
                              n.getAvailability() == EvidenceNodeDto.AvailabilityStatus.MISSING);
        assertTrue(hasMissingOrder, "Order should be marked as MISSING");
        
        // Sufficiency should be PARTIAL or INSUFFICIENT
        assertNotEquals("SUFFICIENT", sufficiency.getAssessment());
        assertNotNull(sufficiency.getMissingEvidence());
        assertFalse(sufficiency.getMissingEvidence().isEmpty(), "Should list missing evidence");
    }

    @Test
    @DisplayName("4. Provenance Tracking: Every Evidence Node Has Source")
    void testProvenanceTracking() {
        // Arrange
        InvestigationEvidenceDto evidence = buildCompleteEvidence();
        
        // Act
        EvidenceGraphDto graph = evidenceGraphService.buildEvidenceGraph(evidence, ExceptionType.AMOUNT_MISMATCH);
        
        // Assert: Every node must have source information
        for (EvidenceNodeDto node : graph.getNodes()) {
            assertNotNull(node.getSource(), "Node " + node.getEntityType() + " missing source");
            assertFalse(node.getSource().isEmpty(), "Node source should not be empty");
            
            // Verify source indicates database table
            assertTrue(node.getSource().contains("table") || 
                      node.getSource().contains("record") ||
                      node.getSource().equals("financial_exceptions table"),
                      "Source should reference database origin: " + node.getSource());
        }
    }

    @Test
    @DisplayName("5. Evidence Graph: DISCREPANT_REFUND Requires Refund Evidence")
    void testRefundEvidenceRequirement() {
        // Arrange: Evidence without refund
        InvestigationEvidenceDto evidence = buildEvidenceWithoutRefund();
        EvidenceGraphDto graph = evidenceGraphService.buildEvidenceGraph(evidence, ExceptionType.DISCREPANT_REFUND);
        
        // Act
        EvidenceSufficiencyDto sufficiency = evidenceGraphService.calculateSufficiency(graph, ExceptionType.DISCREPANT_REFUND);
        
        // Assert
        boolean hasMissingRefund = graph.getNodes().stream()
                .anyMatch(n -> n.getEntityType() == EvidenceNodeDto.EntityType.REFUND &&
                              n.getAvailability() == EvidenceNodeDto.AvailabilityStatus.MISSING);
        assertTrue(hasMissingRefund, "Refund should be marked as MISSING for DISCREPANT_REFUND");
        
        // Sufficiency should reflect missing critical evidence
        assertNotEquals("SUFFICIENT", sufficiency.getAssessment());
        assertTrue(sufficiency.getMissingEvidence().stream()
                .anyMatch(e -> e.contains("REFUND")), "Missing evidence should mention REFUND");
    }

    @Test
    @DisplayName("6. Evidence Sufficiency: Partial Evidence -> PARTIAL Assessment")
    void testPartialEvidence() {
        // Arrange: Evidence with payment but missing settlement (for UNEXPECTED_FEE)
        InvestigationEvidenceDto evidence = buildPartialEvidence();
        EvidenceGraphDto graph = evidenceGraphService.buildEvidenceGraph(evidence, ExceptionType.UNEXPECTED_FEE);
        
        // Act
        EvidenceSufficiencyDto sufficiency = evidenceGraphService.calculateSufficiency(graph, ExceptionType.UNEXPECTED_FEE);
        
        // Assert
        BigDecimal score = sufficiency.getSufficiencyScore();
        assertTrue(score.compareTo(BigDecimal.valueOf(50)) >= 0 && 
                  score.compareTo(BigDecimal.valueOf(80)) < 0,
                  "Partial evidence should score between 50-80%");
        
        // Can be PARTIAL or INSUFFICIENT depending on exact evidence
        assertTrue(sufficiency.getAssessment().equals("PARTIAL") || 
                  sufficiency.getAssessment().equals("INSUFFICIENT"));
    }

    @Test
    @DisplayName("7. Evidence Node Relationships: Correct Relationship Labels")
    void testEvidenceRelationships() {
        // Arrange
        InvestigationEvidenceDto evidence = buildCompleteEvidence();
        
        // Act
        EvidenceGraphDto graph = evidenceGraphService.buildEvidenceGraph(evidence, ExceptionType.AMOUNT_MISMATCH);
        
        // Assert: Verify relationship labels
        boolean hasExceptionNode = graph.getNodes().stream()
                .anyMatch(n -> n.getRelationshipToException().equals("PRIMARY_EXCEPTION"));
        assertTrue(hasExceptionNode, "Should have PRIMARY_EXCEPTION node");
        
        boolean hasPaymentNode = graph.getNodes().stream()
                .anyMatch(n -> n.getRelationshipToException().equals("PRIMARY_PAYMENT"));
        assertTrue(hasPaymentNode, "Should have PRIMARY_PAYMENT node");
        
        boolean hasOrderNode = graph.getNodes().stream()
                .anyMatch(n -> n.getRelationshipToException().equals("ORIGINATING_ORDER"));
        assertTrue(hasOrderNode, "Should have ORIGINATING_ORDER node");
    }

    @Test
    @DisplayName("8. Evidence Sufficiency: DATA_INCOMPLETE Type")
    void testDataIncompleteEvidence() {
        // Arrange: Minimal evidence
        InvestigationEvidenceDto evidence = buildMinimalEvidence();
        EvidenceGraphDto graph = evidenceGraphService.buildEvidenceGraph(evidence, ExceptionType.DATA_INCOMPLETE);
        
        // Act
        EvidenceSufficiencyDto sufficiency = evidenceGraphService.calculateSufficiency(graph, ExceptionType.DATA_INCOMPLETE);
        
        // Assert
        assertNotNull(sufficiency);
        assertTrue(sufficiency.getSufficiencyScore().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(sufficiency.getSufficiencyScore().compareTo(BigDecimal.valueOf(100)) <= 0);
        
        // For DATA_INCOMPLETE, assessment may vary based on available evidence
        assertNotNull(sufficiency.getAssessment());
    }

    @Test
    @DisplayName("9. Missing Evidence != Zero: Explicit MISSING Status")
    void testMissingEvidenceNotTreatedAsZero() {
        // Arrange: Evidence without settlement
        InvestigationEvidenceDto evidence = buildEvidenceWithoutSettlement();
        EvidenceGraphDto graph = evidenceGraphService.buildEvidenceGraph(evidence, ExceptionType.MISSING_SETTLEMENT);
        
        // Assert: Missing evidence is explicitly marked MISSING, not assumed zero or absent
        boolean hasMissingSettlement = graph.getNodes().stream()
                .anyMatch(n -> n.getEntityType() == EvidenceNodeDto.EntityType.SETTLEMENT &&
                              n.getAvailability() == EvidenceNodeDto.AvailabilityStatus.MISSING);
        
        assertTrue(hasMissingSettlement, 
                  "Settlement should be explicitly MISSING, not absent from graph");
        
        // The node should exist with MISSING status
        long settlementNodeCount = graph.getNodes().stream()
                .filter(n -> n.getEntityType() == EvidenceNodeDto.EntityType.SETTLEMENT)
                .count();
        assertEquals(1, settlementNodeCount, "Should have exactly one settlement node marked MISSING");
    }

    @Test
    @DisplayName("10. Bounded Retrieval: Graph Size is Reasonable")
    void testBoundedRetrieval() {
        // Arrange
        InvestigationEvidenceDto evidence = buildCompleteEvidenceWithMultipleRefunds();
        
        // Act
        EvidenceGraphDto graph = evidenceGraphService.buildEvidenceGraph(evidence, ExceptionType.DISCREPANT_REFUND);
        
        // Assert: Graph should be bounded (not retrieving entire merchant database)
        assertTrue(graph.getTotalNodesRetrieved() < 50, 
                  "Evidence graph should be bounded to relevant entities");
        
        // Should have multiple refunds but not unbounded
        long refundCount = graph.getNodes().stream()
                .filter(n -> n.getEntityType() == EvidenceNodeDto.EntityType.REFUND)
                .count();
        assertTrue(refundCount > 0 && refundCount < 20, "Should have reasonable refund count");
    }

    // Helper: Build complete evidence
    private InvestigationEvidenceDto buildCompleteEvidence() {
        InvestigationEvidenceDto.ExceptionSummaryDto exception = new InvestigationEvidenceDto.ExceptionSummaryDto(
                "exp_test_001", "merchant_001", ExceptionType.AMOUNT_MISMATCH, ExceptionSeverity.HIGH, 
                ExceptionStatus.OPEN,
                BigDecimal.valueOf(100), BigDecimal.valueOf(10000), BigDecimal.valueOf(9900),
                "Amount mismatch", OffsetDateTime.now());

        InvestigationEvidenceDto.OrderSummaryDto order = new InvestigationEvidenceDto.OrderSummaryDto(
                "order_001", "cust_001", "merchant_001", BigDecimal.valueOf(10000), "INR", 
                OrderStatus.PAID, OffsetDateTime.now());

        InvestigationEvidenceDto.PaymentSummaryDto payment = new InvestigationEvidenceDto.PaymentSummaryDto(
                "pay_001", "order_001", BigDecimal.valueOf(10000), "INR", PaymentStatus.SUCCESS, 
                PaymentMethod.CARD, null, null, OffsetDateTime.now());

        InvestigationEvidenceDto.RefundSummaryDto refund = new InvestigationEvidenceDto.RefundSummaryDto(
                "ref_001", "pay_001", BigDecimal.valueOf(100), RefundStatus.PROCESSED, 
                "Customer request", OffsetDateTime.now());

        InvestigationEvidenceDto.SettlementSummaryDto settlement = new InvestigationEvidenceDto.SettlementSummaryDto(
                "settle_001", "merchant_001", BigDecimal.valueOf(10000), BigDecimal.valueOf(100),
                BigDecimal.valueOf(200), BigDecimal.valueOf(18), BigDecimal.ZERO,
                BigDecimal.valueOf(9682), BigDecimal.valueOf(9682), SettlementStatus.SETTLED, "UTR123", 
                OffsetDateTime.now());

        InvestigationEvidenceDto.CalculatedAmountsDto calculated = new InvestigationEvidenceDto.CalculatedAmountsDto(
                BigDecimal.valueOf(10000), BigDecimal.valueOf(100), BigDecimal.valueOf(200),
                BigDecimal.valueOf(18), BigDecimal.ZERO, BigDecimal.valueOf(9682), 
                BigDecimal.valueOf(9682), BigDecimal.ZERO);

        return InvestigationEvidenceDto.builder()
                .exception(exception)
                .order(order)
                .payment(payment)
                .refunds(List.of(refund))
                .fees(new ArrayList<>())
                .adjustments(new ArrayList<>())
                .settlement(settlement)
                .calculatedAmounts(calculated)
                .lineage("Order order_001 → Payment pay_001 → Refund ref_001 → Settlement settle_001 → AMOUNT_MISMATCH exp_test_001")
                .build();
    }

    // Helper: Evidence without order
    private InvestigationEvidenceDto buildEvidenceWithoutOrder() {
        InvestigationEvidenceDto.ExceptionSummaryDto exception = new InvestigationEvidenceDto.ExceptionSummaryDto(
                "exp_test_002", "merchant_001", ExceptionType.MISSING_PAYMENT, ExceptionSeverity.HIGH,
                ExceptionStatus.OPEN,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), BigDecimal.ZERO,
                "Missing payment", OffsetDateTime.now());

        InvestigationEvidenceDto.PaymentSummaryDto payment = new InvestigationEvidenceDto.PaymentSummaryDto(
                "pay_002", null, BigDecimal.valueOf(10000), "INR", PaymentStatus.SUCCESS,
                PaymentMethod.CARD, null, null, OffsetDateTime.now());

        InvestigationEvidenceDto.CalculatedAmountsDto calculated = new InvestigationEvidenceDto.CalculatedAmountsDto(
                BigDecimal.valueOf(10000), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.valueOf(10000), BigDecimal.ZERO, BigDecimal.valueOf(10000));

        return InvestigationEvidenceDto.builder()
                .exception(exception)
                .order(null)  // Missing order
                .payment(payment)
                .refunds(new ArrayList<>())
                .fees(new ArrayList<>())
                .adjustments(new ArrayList<>())
                .settlement(null)
                .calculatedAmounts(calculated)
                .lineage("Payment pay_002 → MISSING_PAYMENT exp_test_002")
                .build();
    }

    // Helper: Evidence without refund
    private InvestigationEvidenceDto buildEvidenceWithoutRefund() {
        InvestigationEvidenceDto.ExceptionSummaryDto exception = new InvestigationEvidenceDto.ExceptionSummaryDto(
                "exp_test_003", "merchant_001", ExceptionType.DISCREPANT_REFUND, ExceptionSeverity.MEDIUM,
                ExceptionStatus.OPEN,
                BigDecimal.valueOf(500), BigDecimal.valueOf(500), BigDecimal.ZERO,
                "Refund discrepancy", OffsetDateTime.now());

        InvestigationEvidenceDto.OrderSummaryDto order = new InvestigationEvidenceDto.OrderSummaryDto(
                "order_003", "cust_003", "merchant_001", BigDecimal.valueOf(5000), "INR",
                OrderStatus.PAID, OffsetDateTime.now());

        InvestigationEvidenceDto.PaymentSummaryDto payment = new InvestigationEvidenceDto.PaymentSummaryDto(
                "pay_003", "order_003", BigDecimal.valueOf(5000), "INR", PaymentStatus.SUCCESS,
                PaymentMethod.UPI, null, null, OffsetDateTime.now());

        InvestigationEvidenceDto.CalculatedAmountsDto calculated = new InvestigationEvidenceDto.CalculatedAmountsDto(
                BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.valueOf(5000), BigDecimal.valueOf(5000), BigDecimal.ZERO);

        return InvestigationEvidenceDto.builder()
                .exception(exception)
                .order(order)
                .payment(payment)
                .refunds(new ArrayList<>())  // No refunds
                .fees(new ArrayList<>())
                .adjustments(new ArrayList<>())
                .settlement(null)
                .calculatedAmounts(calculated)
                .lineage("Order order_003 → Payment pay_003 → DISCREPANT_REFUND exp_test_003")
                .build();
    }

    // Helper: Partial evidence
    private InvestigationEvidenceDto buildPartialEvidence() {
        InvestigationEvidenceDto.ExceptionSummaryDto exception = new InvestigationEvidenceDto.ExceptionSummaryDto(
                "exp_test_004", "merchant_001", ExceptionType.UNEXPECTED_FEE, ExceptionSeverity.MEDIUM,
                ExceptionStatus.OPEN,
                BigDecimal.valueOf(50), BigDecimal.valueOf(200), BigDecimal.valueOf(250),
                "Unexpected fee", OffsetDateTime.now());

        InvestigationEvidenceDto.PaymentSummaryDto payment = new InvestigationEvidenceDto.PaymentSummaryDto(
                "pay_004", "order_004", BigDecimal.valueOf(10000), "INR", PaymentStatus.SUCCESS,
                PaymentMethod.CARD, null, null, OffsetDateTime.now());

        InvestigationEvidenceDto.FeeSummaryDto fee = new InvestigationEvidenceDto.FeeSummaryDto(
                BigDecimal.valueOf(200), BigDecimal.valueOf(18), BigDecimal.valueOf(218), BigDecimal.valueOf(2.0));

        InvestigationEvidenceDto.CalculatedAmountsDto calculated = new InvestigationEvidenceDto.CalculatedAmountsDto(
                BigDecimal.valueOf(10000), BigDecimal.ZERO, BigDecimal.valueOf(218), BigDecimal.valueOf(18),
                BigDecimal.ZERO, BigDecimal.valueOf(9782), BigDecimal.valueOf(9782), BigDecimal.ZERO);

        return InvestigationEvidenceDto.builder()
                .exception(exception)
                .order(null)
                .payment(payment)
                .refunds(new ArrayList<>())
                .fees(List.of(fee))
                .adjustments(new ArrayList<>())
                .settlement(null)  // Missing settlement
                .calculatedAmounts(calculated)
                .lineage("Payment pay_004 → UNEXPECTED_FEE exp_test_004")
                .build();
    }

    // Helper: Minimal evidence
    private InvestigationEvidenceDto buildMinimalEvidence() {
        InvestigationEvidenceDto.ExceptionSummaryDto exception = new InvestigationEvidenceDto.ExceptionSummaryDto(
                "exp_test_005", "merchant_001", ExceptionType.DATA_INCOMPLETE, ExceptionSeverity.HIGH,
                ExceptionStatus.OPEN,
                null, null, null, "Incomplete data", OffsetDateTime.now());

        InvestigationEvidenceDto.CalculatedAmountsDto calculated = new InvestigationEvidenceDto.CalculatedAmountsDto(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        return InvestigationEvidenceDto.builder()
                .exception(exception)
                .order(null)
                .payment(null)
                .refunds(new ArrayList<>())
                .fees(new ArrayList<>())
                .adjustments(new ArrayList<>())
                .settlement(null)
                .calculatedAmounts(calculated)
                .lineage("DATA_INCOMPLETE exp_test_005")
                .build();
    }

    // Helper: Evidence without settlement
    private InvestigationEvidenceDto buildEvidenceWithoutSettlement() {
        InvestigationEvidenceDto.ExceptionSummaryDto exception = new InvestigationEvidenceDto.ExceptionSummaryDto(
                "exp_test_006", "merchant_001", ExceptionType.MISSING_SETTLEMENT, ExceptionSeverity.HIGH,
                ExceptionStatus.OPEN,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), BigDecimal.ZERO,
                "Settlement missing", OffsetDateTime.now());

        InvestigationEvidenceDto.OrderSummaryDto order = new InvestigationEvidenceDto.OrderSummaryDto(
                "order_006", "cust_006", "merchant_001", BigDecimal.valueOf(10000), "INR",
                OrderStatus.PAID, OffsetDateTime.now());

        InvestigationEvidenceDto.PaymentSummaryDto payment = new InvestigationEvidenceDto.PaymentSummaryDto(
                "pay_006", "order_006", BigDecimal.valueOf(10000), "INR", PaymentStatus.SUCCESS,
                PaymentMethod.CARD, null, null, OffsetDateTime.now());

        InvestigationEvidenceDto.CalculatedAmountsDto calculated = new InvestigationEvidenceDto.CalculatedAmountsDto(
                BigDecimal.valueOf(10000), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.valueOf(10000), BigDecimal.ZERO, BigDecimal.valueOf(10000));

        return InvestigationEvidenceDto.builder()
                .exception(exception)
                .order(order)
                .payment(payment)
                .refunds(new ArrayList<>())
                .fees(new ArrayList<>())
                .adjustments(new ArrayList<>())
                .settlement(null)  // Missing settlement
                .calculatedAmounts(calculated)
                .lineage("Order order_006 → Payment pay_006 → MISSING_SETTLEMENT exp_test_006")
                .build();
    }

    // Helper: Complete evidence with multiple refunds
    private InvestigationEvidenceDto buildCompleteEvidenceWithMultipleRefunds() {
        InvestigationEvidenceDto.ExceptionSummaryDto exception = new InvestigationEvidenceDto.ExceptionSummaryDto(
                "exp_test_007", "merchant_001", ExceptionType.DISCREPANT_REFUND, ExceptionSeverity.MEDIUM,
                ExceptionStatus.OPEN,
                BigDecimal.valueOf(300), BigDecimal.valueOf(500), BigDecimal.valueOf(200),
                "Multiple refund discrepancy", OffsetDateTime.now());

        InvestigationEvidenceDto.OrderSummaryDto order = new InvestigationEvidenceDto.OrderSummaryDto(
                "order_007", "cust_007", "merchant_001", BigDecimal.valueOf(10000), "INR",
                OrderStatus.PAID, OffsetDateTime.now());

        InvestigationEvidenceDto.PaymentSummaryDto payment = new InvestigationEvidenceDto.PaymentSummaryDto(
                "pay_007", "order_007", BigDecimal.valueOf(10000), "INR", PaymentStatus.SUCCESS,
                PaymentMethod.CARD, null, null, OffsetDateTime.now());

        List<InvestigationEvidenceDto.RefundSummaryDto> refunds = List.of(
                new InvestigationEvidenceDto.RefundSummaryDto("ref_007_1", "pay_007", BigDecimal.valueOf(100), RefundStatus.PROCESSED, "Reason 1", OffsetDateTime.now()),
                new InvestigationEvidenceDto.RefundSummaryDto("ref_007_2", "pay_007", BigDecimal.valueOf(100), RefundStatus.PROCESSED, "Reason 2", OffsetDateTime.now()),
                new InvestigationEvidenceDto.RefundSummaryDto("ref_007_3", "pay_007", BigDecimal.valueOf(100), RefundStatus.PROCESSED, "Reason 3", OffsetDateTime.now())
        );

        InvestigationEvidenceDto.CalculatedAmountsDto calculated = new InvestigationEvidenceDto.CalculatedAmountsDto(
                BigDecimal.valueOf(10000), BigDecimal.valueOf(300), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.valueOf(9700), BigDecimal.valueOf(9700), BigDecimal.ZERO);

        return InvestigationEvidenceDto.builder()
                .exception(exception)
                .order(order)
                .payment(payment)
                .refunds(refunds)
                .fees(new ArrayList<>())
                .adjustments(new ArrayList<>())
                .settlement(null)
                .calculatedAmounts(calculated)
                .lineage("Order order_007 → Payment pay_007 → Multiple Refunds → DISCREPANT_REFUND exp_test_007")
                .build();
    }
}
