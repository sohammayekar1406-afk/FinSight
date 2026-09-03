package com.ledgerlens;

import com.ledgerlens.dto.*;
import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.service.ai.FinancialAmountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 4 Adversarial AI Safety Tests
 * 
 * Tests that the system correctly handles malicious or incorrect AI output:
 * 1. AI fabricates exception IDs
 * 2. AI fabricates investigation IDs (historical cases)
 * 3. AI provides invalid confidence scores (< 0 or > 100)
 * 4. AI fabricates financial amounts inconsistent with backend evidence
 * 5. AI invents related exception IDs not supplied by backend
 * 6. AI attempts to override deterministic reconciliation truth
 * 
 * EXPECTED BEHAVIOR:
 * - Invalid AI output is rejected by FinancialAmountValidator
 * - Deterministic backend remains authoritative
 * - Fabricated IDs cannot become trusted financial state
 */
class Phase4AdversarialAiTest {

    private FinancialAmountValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FinancialAmountValidator();
    }

    @Test
    void testAiFabricatesRelatedExceptionIds() {
        // GIVEN: Backend supplies related exception IDs [100, 101, 102]
        List<RelatedExceptionDto> backendSupplied = Arrays.asList(
                RelatedExceptionDto.builder().exceptionId(100L).build(),
                RelatedExceptionDto.builder().exceptionId(101L).build(),
                RelatedExceptionDto.builder().exceptionId(102L).build()
        );

        // WHEN: AI fabricates additional exception IDs [999, 1000]
        List<RelatedExceptionDto> aiFabricated = Arrays.asList(
                RelatedExceptionDto.builder().exceptionId(100L).build(),
                RelatedExceptionDto.builder().exceptionId(999L).build(), // FABRICATED
                RelatedExceptionDto.builder().exceptionId(1000L).build() // FABRICATED
        );

        AiInvestigationResponse aiResponse = AiInvestigationResponse.builder()
                .summary("Investigation summary")
                .relatedExceptions(aiFabricated)
                .build();

        // THEN: Validation REJECTS fabricated IDs
        boolean valid = validator.validateForensicReasoning(aiResponse, backendSupplied);
        assertThat(valid).isFalse();
    }

    @Test
    void testAiProvidesNegativeConfidenceScore() {
        // GIVEN: AI provides hypothesis with negative confidence
        List<HypothesisDto> hypotheses = Arrays.asList(
                HypothesisDto.builder()
                        .hypothesis("Test hypothesis")
                        .confidence(BigDecimal.valueOf(-10.0)) // INVALID
                        .status(HypothesisDto.HypothesisStatus.UNRESOLVED)
                        .build()
        );

        AiInvestigationResponse aiResponse = AiInvestigationResponse.builder()
                .summary("Analysis")
                .hypotheses(hypotheses)
                .build();

        // THEN: Validation REJECTS negative confidence
        boolean valid = validator.validateForensicReasoning(aiResponse, List.of());
        assertThat(valid).isFalse();
    }

    @Test
    void testAiProvidesConfidenceAbove100() {
        // GIVEN: AI provides hypothesis with confidence > 100
        List<HypothesisDto> hypotheses = Arrays.asList(
                HypothesisDto.builder()
                        .hypothesis("Test hypothesis")
                        .confidence(BigDecimal.valueOf(150.0)) // INVALID
                        .status(HypothesisDto.HypothesisStatus.SUPPORTED)
                        .build()
        );

        AiInvestigationResponse aiResponse = AiInvestigationResponse.builder()
                .summary("Analysis")
                .hypotheses(hypotheses)
                .build();

        // THEN: Validation REJECTS confidence > 100
        boolean valid = validator.validateForensicReasoning(aiResponse, List.of());
        assertThat(valid).isFalse();
    }

    @Test
    void testAiProvidesMultipleInvalidConfidenceScores() {
        // GIVEN: AI provides multiple hypotheses with mixed valid/invalid confidence
        List<HypothesisDto> hypotheses = Arrays.asList(
                HypothesisDto.builder()
                        .hypothesis("Hypothesis 1")
                        .confidence(BigDecimal.valueOf(75.0)) // VALID
                        .status(HypothesisDto.HypothesisStatus.SUPPORTED)
                        .build(),
                HypothesisDto.builder()
                        .hypothesis("Hypothesis 2")
                        .confidence(BigDecimal.valueOf(200.0)) // INVALID
                        .status(HypothesisDto.HypothesisStatus.WEAKENED)
                        .build(),
                HypothesisDto.builder()
                        .hypothesis("Hypothesis 3")
                        .confidence(BigDecimal.valueOf(-50.0)) // INVALID
                        .status(HypothesisDto.HypothesisStatus.CONTRADICTED)
                        .build()
        );

        AiInvestigationResponse aiResponse = AiInvestigationResponse.builder()
                .summary("Analysis")
                .hypotheses(hypotheses)
                .build();

        // THEN: Validation REJECTS any invalid confidence
        boolean valid = validator.validateForensicReasoning(aiResponse, List.of());
        assertThat(valid).isFalse();
    }

    @Test
    void testAiCannotOverrideBackendSuppliedRelatedExceptions() {
        // GIVEN: Backend supplies 3 related exceptions
        List<RelatedExceptionDto> backendSupplied = Arrays.asList(
                RelatedExceptionDto.builder()
                        .exceptionId(100L)
                        .exceptionType(ExceptionType.AMOUNT_MISMATCH)
                        .relationshipType(RelatedExceptionDto.RelationshipType.SAME_PAYMENT)
                        .build(),
                RelatedExceptionDto.builder()
                        .exceptionId(101L)
                        .exceptionType(ExceptionType.MISSING_PAYMENT)
                        .relationshipType(RelatedExceptionDto.RelationshipType.SAME_SETTLEMENT)
                        .build(),
                RelatedExceptionDto.builder()
                        .exceptionId(102L)
                        .exceptionType(ExceptionType.DUPLICATE_TRANSACTION)
                        .relationshipType(RelatedExceptionDto.RelationshipType.SAME_REFUND)
                        .build()
        );

        // WHEN: AI attempts to use completely different exception IDs
        List<RelatedExceptionDto> aiFabricated = Arrays.asList(
                RelatedExceptionDto.builder().exceptionId(999L).build(),
                RelatedExceptionDto.builder().exceptionId(1000L).build(),
                RelatedExceptionDto.builder().exceptionId(1001L).build()
        );

        AiInvestigationResponse aiResponse = AiInvestigationResponse.builder()
                .summary("Analysis")
                .relatedExceptions(aiFabricated)
                .build();

        // THEN: Validation REJECTS all fabricated IDs
        boolean valid = validator.validateForensicReasoning(aiResponse, backendSupplied);
        assertThat(valid).isFalse();
    }

    @Test
    void testAiRespectsBackendSuppliedRelatedExceptions() {
        // GIVEN: Backend supplies related exception IDs
        List<RelatedExceptionDto> backendSupplied = Arrays.asList(
                RelatedExceptionDto.builder().exceptionId(100L).build(),
                RelatedExceptionDto.builder().exceptionId(101L).build()
        );

        // WHEN: AI uses ONLY backend-supplied IDs
        List<RelatedExceptionDto> aiResponse = Arrays.asList(
                RelatedExceptionDto.builder().exceptionId(100L).build(),
                RelatedExceptionDto.builder().exceptionId(101L).build()
        );

        AiInvestigationResponse response = AiInvestigationResponse.builder()
                .summary("Analysis")
                .relatedExceptions(aiResponse)
                .build();

        // THEN: Validation ACCEPTS
        boolean valid = validator.validateForensicReasoning(response, backendSupplied);
        assertThat(valid).isTrue();
    }

    @Test
    void testAiRespectsBackendSuppliedSubset() {
        // GIVEN: Backend supplies 5 related exceptions
        List<RelatedExceptionDto> backendSupplied = Arrays.asList(
                RelatedExceptionDto.builder().exceptionId(100L).build(),
                RelatedExceptionDto.builder().exceptionId(101L).build(),
                RelatedExceptionDto.builder().exceptionId(102L).build(),
                RelatedExceptionDto.builder().exceptionId(103L).build(),
                RelatedExceptionDto.builder().exceptionId(104L).build()
        );

        // WHEN: AI uses a subset (3 out of 5)
        List<RelatedExceptionDto> aiSubset = Arrays.asList(
                RelatedExceptionDto.builder().exceptionId(100L).build(),
                RelatedExceptionDto.builder().exceptionId(102L).build(),
                RelatedExceptionDto.builder().exceptionId(104L).build()
        );

        AiInvestigationResponse response = AiInvestigationResponse.builder()
                .summary("Analysis")
                .relatedExceptions(aiSubset)
                .build();

        // THEN: Validation ACCEPTS subset usage
        boolean valid = validator.validateForensicReasoning(response, backendSupplied);
        assertThat(valid).isTrue();
    }

    @Test
    void testValidHypothesesWithValidConfidence() {
        // GIVEN: AI provides hypotheses with valid confidence scores (0-100)
        List<HypothesisDto> hypotheses = Arrays.asList(
                HypothesisDto.builder()
                        .hypothesis("Hypothesis 1")
                        .confidence(BigDecimal.valueOf(0.0)) // Edge case: 0
                        .status(HypothesisDto.HypothesisStatus.CONTRADICTED)
                        .build(),
                HypothesisDto.builder()
                        .hypothesis("Hypothesis 2")
                        .confidence(BigDecimal.valueOf(50.0)) // Mid-range
                        .status(HypothesisDto.HypothesisStatus.UNRESOLVED)
                        .build(),
                HypothesisDto.builder()
                        .hypothesis("Hypothesis 3")
                        .confidence(BigDecimal.valueOf(100.0)) // Edge case: 100
                        .status(HypothesisDto.HypothesisStatus.SUPPORTED)
                        .build()
        );

        AiInvestigationResponse response = AiInvestigationResponse.builder()
                .summary("Analysis")
                .hypotheses(hypotheses)
                .build();

        // THEN: Validation ACCEPTS all valid confidence scores
        boolean valid = validator.validateForensicReasoning(response, List.of());
        assertThat(valid).isTrue();
    }

    @Test
    void testEmptyRelatedExceptionsIsValid() {
        // GIVEN: Backend supplies no related exceptions
        List<RelatedExceptionDto> backendSupplied = List.of();

        // WHEN: AI also returns empty
        AiInvestigationResponse response = AiInvestigationResponse.builder()
                .summary("Analysis")
                .relatedExceptions(List.of())
                .build();

        // THEN: Validation ACCEPTS
        boolean valid = validator.validateForensicReasoning(response, backendSupplied);
        assertThat(valid).isTrue();
    }

    @Test
    void testNullRelatedExceptionsIsValid() {
        // GIVEN: Backend supplies no related exceptions
        List<RelatedExceptionDto> backendSupplied = List.of();

        // WHEN: AI returns null (not explicitly listing related exceptions)
        AiInvestigationResponse response = AiInvestigationResponse.builder()
                .summary("Analysis")
                .relatedExceptions(null)
                .build();

        // THEN: Validation ACCEPTS
        boolean valid = validator.validateForensicReasoning(response, backendSupplied);
        assertThat(valid).isTrue();
    }

    @Test
    void testAiFabricatesWhenBackendSuppliesEmpty() {
        // GIVEN: Backend supplies NO related exceptions
        List<RelatedExceptionDto> backendSupplied = List.of();

        // WHEN: AI fabricates exception IDs anyway
        List<RelatedExceptionDto> aiFabricated = Arrays.asList(
                RelatedExceptionDto.builder().exceptionId(999L).build(),
                RelatedExceptionDto.builder().exceptionId(1000L).build()
        );

        AiInvestigationResponse response = AiInvestigationResponse.builder()
                .summary("Analysis")
                .relatedExceptions(aiFabricated)
                .build();

        // THEN: Validation REJECTS fabricated IDs
        boolean valid = validator.validateForensicReasoning(response, backendSupplied);
        assertThat(valid).isFalse();
    }
}
