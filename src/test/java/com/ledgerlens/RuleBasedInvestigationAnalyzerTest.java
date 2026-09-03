package com.ledgerlens;

import com.ledgerlens.dto.InvestigationAnalysis;
import com.ledgerlens.dto.InvestigationEvidenceDto;
import com.ledgerlens.entity.enums.ActionTaken;
import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.entity.enums.RecommendedAction;
import com.ledgerlens.service.analyzer.RuleBasedInvestigationAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RuleBasedInvestigationAnalyzerTest {

    private RuleBasedInvestigationAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new RuleBasedInvestigationAnalyzer();
    }

    private InvestigationEvidenceDto createEvidence(ExceptionType type, BigDecimal expected, BigDecimal actual, BigDecimal discrepancy, BigDecimal refunds, BigDecimal fees, BigDecimal adjustments) {
        InvestigationEvidenceDto.ExceptionSummaryDto ex = new InvestigationEvidenceDto.ExceptionSummaryDto(
                "exp_1", "merch_1", type, ExceptionSeverity.MEDIUM, ExceptionStatus.OPEN, discrepancy, expected, actual, "Test exception", null
        );
        InvestigationEvidenceDto.CalculatedAmountsDto calc = new InvestigationEvidenceDto.CalculatedAmountsDto(
                expected, refunds, fees, BigDecimal.ZERO, adjustments, expected, actual, discrepancy
        );
        return InvestigationEvidenceDto.builder()
                .exception(ex)
                .calculatedAmounts(calc)
                .lineage("Lineage string")
                .build();
    }

    @Test
    void testAmountMismatchUnexplainedDifference() {
        InvestigationEvidenceDto evidence = createEvidence(
                ExceptionType.AMOUNT_MISMATCH, new BigDecimal("976.40"), new BigDecimal("476.40"), new BigDecimal("500.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        InvestigationAnalysis analysis = analyzer.analyze(evidence);

        assertNotNull(analysis);
        assertEquals(new BigDecimal("96.00"), analysis.getConfidenceScore());
        assertEquals(RecommendedAction.HUMAN_REVIEW_REQUIRED, analysis.getRecommendedAction());
        assertEquals(ActionTaken.SENT_TO_HUMAN, analysis.getActionTaken());
        assertFalse(analysis.isAutoResolved());
        assertTrue(analysis.getLikelyRootCause().contains("500.00 remains unexplained"));
    }

    @Test
    void testAmountMismatchFullyExplained() {
        InvestigationEvidenceDto evidence = createEvidence(
                ExceptionType.AMOUNT_MISMATCH, new BigDecimal("1000.00"), new BigDecimal("900.00"), new BigDecimal("100.00"), new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO
        );

        InvestigationAnalysis analysis = analyzer.analyze(evidence);

        assertEquals(new BigDecimal("100.00"), analysis.getConfidenceScore());
        assertEquals(RecommendedAction.AUTO_RESOLVE, analysis.getRecommendedAction());
        assertEquals(ActionTaken.AUTO_RESOLVED, analysis.getActionTaken());
        assertTrue(analysis.isAutoResolved());
    }

    @Test
    void testMissingPayment() {
        InvestigationEvidenceDto evidence = createEvidence(ExceptionType.MISSING_PAYMENT, new BigDecimal("500.00"), BigDecimal.ZERO, new BigDecimal("500.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        InvestigationAnalysis analysis = analyzer.analyze(evidence);

        assertEquals(new BigDecimal("100.00"), analysis.getConfidenceScore());
        assertEquals(RecommendedAction.HUMAN_REVIEW_REQUIRED, analysis.getRecommendedAction());
    }

    @Test
    void testMissingSettlement() {
        InvestigationEvidenceDto evidence = createEvidence(ExceptionType.MISSING_SETTLEMENT, new BigDecimal("500.00"), BigDecimal.ZERO, new BigDecimal("500.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        InvestigationAnalysis analysis = analyzer.analyze(evidence);

        assertEquals(new BigDecimal("95.00"), analysis.getConfidenceScore());
        assertEquals(RecommendedAction.RETRY_SETTLEMENT, analysis.getRecommendedAction());
    }

    @Test
    void testUnexpectedFee() {
        InvestigationEvidenceDto evidence = createEvidence(ExceptionType.UNEXPECTED_FEE, new BigDecimal("50.00"), new BigDecimal("60.00"), new BigDecimal("10.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        InvestigationAnalysis analysis = analyzer.analyze(evidence);

        assertEquals(new BigDecimal("100.00"), analysis.getConfidenceScore());
        assertEquals(RecommendedAction.MANUAL_ADJUSTMENT, analysis.getRecommendedAction());
    }

    @Test
    void testDuplicateTransaction() {
        InvestigationEvidenceDto evidence = createEvidence(ExceptionType.DUPLICATE_TRANSACTION, new BigDecimal("100.00"), new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        InvestigationAnalysis analysis = analyzer.analyze(evidence);

        assertEquals(new BigDecimal("100.00"), analysis.getConfidenceScore());
        assertEquals(RecommendedAction.HUMAN_REVIEW_REQUIRED, analysis.getRecommendedAction());
    }

    @Test
    void testDelayedSettlement() {
        InvestigationEvidenceDto evidence = createEvidence(ExceptionType.DELAYED_SETTLEMENT, new BigDecimal("100.00"), new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        InvestigationAnalysis analysis = analyzer.analyze(evidence);

        assertEquals(new BigDecimal("90.00"), analysis.getConfidenceScore());
        assertEquals(RecommendedAction.HUMAN_REVIEW_REQUIRED, analysis.getRecommendedAction());
    }

    @Test
    void testDiscrepantRefund() {
        InvestigationEvidenceDto evidence = createEvidence(ExceptionType.DISCREPANT_REFUND, new BigDecimal("100.00"), new BigDecimal("150.00"), new BigDecimal("50.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        InvestigationAnalysis analysis = analyzer.analyze(evidence);

        assertEquals(new BigDecimal("100.00"), analysis.getConfidenceScore());
        assertEquals(RecommendedAction.HUMAN_REVIEW_REQUIRED, analysis.getRecommendedAction());
    }

    @Test
    void testUnknownTransaction() {
        InvestigationEvidenceDto evidence = createEvidence(ExceptionType.UNKNOWN_TRANSACTION, BigDecimal.ZERO, new BigDecimal("100.00"), new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        InvestigationAnalysis analysis = analyzer.analyze(evidence);

        assertEquals(new BigDecimal("100.00"), analysis.getConfidenceScore());
        assertEquals(RecommendedAction.HUMAN_REVIEW_REQUIRED, analysis.getRecommendedAction());
    }
}
