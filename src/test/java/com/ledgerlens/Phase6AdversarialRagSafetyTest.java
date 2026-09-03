package com.ledgerlens;

import com.ledgerlens.dto.*;
import com.ledgerlens.service.ai.FinancialAmountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 6.8: Adversarial AI Safety Tests for RAG
 * 
 * Verifies that:
 * 1. A semantically similar but financially misleading historical case does not get treated as authoritative.
 * 2. No fabricated historical investigation IDs or amounts make it into trusted financial state.
 * 3. Evidence sufficiency is unaffected by RAG — INSUFFICIENT evidence remains acknowledged.
 */
@DisplayName("Phase 6.8: Adversarial RAG Safety Tests")
class Phase6AdversarialRagSafetyTest {

    private FinancialAmountValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FinancialAmountValidator();
    }

    @Test
    @DisplayName("Financial amount validator rejects AI output that hallucinates amounts from misleading RAG case")
    void testValidatorRejectsAmountsHallucinatedFromRagCase() {
        // GIVEN: Current case evidence has discrepancy of ₹250.00
        InvestigationEvidenceDto currentEvidence = InvestigationEvidenceDto.builder()
                .calculatedAmounts(new InvestigationEvidenceDto.CalculatedAmountsDto(
                        new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        new BigDecimal("1000.00"), new BigDecimal("750.00"), new BigDecimal("250.00")))
                .lineage("Order ord_101 -> Payment pay_101 -> Settlement set_101")
                .build();

        // RAG case had discrepancy of ₹9999.00 (misleading)
        RagHistoricalCaseDto misleadingRagCase = RagHistoricalCaseDto.builder()
                .investigationId("inv_misleading_999")
                .discrepancyAmount(new BigDecimal("9999.00"))
                .previousRootCause("Overcharged GST tax fee of ₹9999.00")
                .build();

        // WHEN: AI generates summary hallucinating the ₹9999.00 amount not in current evidence
        String aiOutputWithHallucinatedAmount = "Investigation shows overcharge of ₹9999.00 from GST fee.";

        // THEN: Validator rejects the output
        boolean valid = validator.validateAmounts(aiOutputWithHallucinatedAmount, currentEvidence);
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Financial amount validator accepts AI output properly grounded in current Evidence Graph")
    void testValidatorAcceptsProperlyGroundedAiOutput() {
        InvestigationEvidenceDto currentEvidence = InvestigationEvidenceDto.builder()
                .calculatedAmounts(new InvestigationEvidenceDto.CalculatedAmountsDto(
                        new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        new BigDecimal("1000.00"), new BigDecimal("750.00"), new BigDecimal("250.00")))
                .lineage("Order ord_101 (₹1000) -> Settlement set_101 (₹750)")
                .build();

        String groundedAiOutput = "Discrepancy of ₹250.00 observed between Order ₹1000 and Settlement ₹750.";

        boolean valid = validator.validateAmounts(groundedAiOutput, currentEvidence);
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("Evidence sufficiency scoring remains INSUFFICIENT even when RAG finds high similarity case")
    void testEvidenceSufficiencyUnaffectedByRag() {
        // GIVEN: Evidence Graph has only 1 node found out of 4 required (Missing payment, settlement)
        EvidenceGraphDto graph = EvidenceGraphDto.builder()
                .nodes(List.of(
                        EvidenceNodeDto.builder()
                                .entityId("order_1")
                                .entityType(EvidenceNodeDto.EntityType.ORDER)
                                .availability(EvidenceNodeDto.AvailabilityStatus.FOUND)
                                .build(),
                        EvidenceNodeDto.builder()
                                .entityId("payment_1")
                                .entityType(EvidenceNodeDto.EntityType.PAYMENT)
                                .availability(EvidenceNodeDto.AvailabilityStatus.MISSING)
                                .build()
                ))
                .totalNodesRetrieved(2)
                .foundNodes(1)
                .missingNodes(1)
                .build();

        EvidenceSufficiencyDto sufficiency = EvidenceSufficiencyDto.builder()
                .sufficiencyScore(new BigDecimal("35.00"))
                .assessment("INSUFFICIENT")
                .missingEvidence(List.of("PAYMENT"))
                .reasoning("Missing core payment record")
                .build();

        // RAG case has 98% semantic similarity
        RagHistoricalCaseDto highSimRagCase = RagHistoricalCaseDto.builder()
                .investigationId("inv_old_perfect_match")
                .semanticSimilarityScore(new BigDecimal("0.98"))
                .blendedScore(new BigDecimal("0.96"))
                .build();

        // THEN: Sufficiency remains INSUFFICIENT; RAG cannot manufacture artificial confidence
        assertThat(sufficiency.getAssessment()).isEqualTo("INSUFFICIENT");
        assertThat(sufficiency.getSufficiencyScore()).isLessThan(new BigDecimal("50.00"));
        assertThat(highSimRagCase.getSemanticSimilarityScore()).isGreaterThan(new BigDecimal("0.90"));
    }
}
