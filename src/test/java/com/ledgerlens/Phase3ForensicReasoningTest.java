package com.ledgerlens;

import com.ledgerlens.dto.*;
import com.ledgerlens.entity.*;
import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.service.RelatedExceptionService;
import com.ledgerlens.service.MerchantContext;
import com.ledgerlens.service.ai.FinancialAmountValidator;
import com.ledgerlens.repository.FinancialExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Phase 3 Forensic Reasoning Tests
 * 
 * Verifies:
 * 1. Hypothesis generation and validation
 * 2. Contradiction detection
 * 3. Missing evidence identification (structured EvidenceRequestDto)
 * 4. Cross-exception correlation (merchant-scoped, max 5 results)
 * 5. Validation prevents fabricated exception IDs
 * 6. Hypothesis confidence validation (0-100)
 */
@ExtendWith(MockitoExtension.class)
class Phase3ForensicReasoningTest {

    @Mock
    private FinancialExceptionRepository exceptionRepository;

    @Mock
    private MerchantContext merchantContext;

    private RelatedExceptionService relatedExceptionService;
    private FinancialAmountValidator validator;

    @BeforeEach
    void setUp() {
        relatedExceptionService = new RelatedExceptionService(exceptionRepository, merchantContext);
        validator = new FinancialAmountValidator();
        
        lenient().when(merchantContext.merchantId()).thenReturn("merch_001");
        lenient().when(exceptionRepository.findByMerchantId(anyString())).thenReturn(List.of());
    }

    @Test
    void testHypothesisDto_Creation() {
        // GIVEN: A hypothesis with supporting and contradicting evidence
        HypothesisDto hypothesis = HypothesisDto.builder()
                .hypothesis("Settlement batch was processed twice due to retry logic")
                .confidence(BigDecimal.valueOf(75.0))
                .supportingEvidence(Arrays.asList(
                        "Duplicate settlement IDs found",
                        "Timestamps within 2 seconds"
                ))
                .contradictingEvidence(Arrays.asList(
                        "Transaction IDs are unique"
                ))
                .status(HypothesisDto.HypothesisStatus.SUPPORTED)
                .build();

        // THEN: Hypothesis structure is correct
        assertThat(hypothesis.getHypothesis()).contains("Settlement batch");
        assertThat(hypothesis.getConfidence()).isEqualByComparingTo(BigDecimal.valueOf(75.0));
        assertThat(hypothesis.getSupportingEvidence()).hasSize(2);
        assertThat(hypothesis.getContradictingEvidence()).hasSize(1);
        assertThat(hypothesis.getStatus()).isEqualTo(HypothesisDto.HypothesisStatus.SUPPORTED);
    }

    @Test
    void testContradictionDto_Creation() {
        // GIVEN: A contradiction between two pieces of evidence
        ContradictionDto contradiction = ContradictionDto.builder()
                .contradiction("Settlement amount conflicts with transaction total")
                .evidenceA("Settlement record shows $1000.00")
                .evidenceB("Transaction sum equals $999.50")
                .severity(ContradictionDto.ContradictionSeverity.HIGH)
                .resolution("Discrepancy of $0.50 identified as rounding error")
                .unresolved(false)
                .build();

        // THEN: Contradiction structure is correct
        assertThat(contradiction.getContradiction()).contains("Settlement amount conflicts");
        assertThat(contradiction.getEvidenceA()).contains("$1000.00");
        assertThat(contradiction.getEvidenceB()).contains("$999.50");
        assertThat(contradiction.getSeverity()).isEqualTo(ContradictionDto.ContradictionSeverity.HIGH);
        assertThat(contradiction.isUnresolved()).isFalse();
    }

    @Test
    void testEvidenceRequestDto_Creation() {
        // GIVEN: A structured request for missing evidence
        EvidenceRequestDto request = EvidenceRequestDto.builder()
                .evidenceType("GATEWAY_RECONCILIATION_REPORT")
                .description("Payment gateway reconciliation file for 2024-01-15")
                .reason("Need to verify if payment was actually received by gateway")
                .expectedImpact("Would definitively confirm or refute duplicate payment hypothesis")
                .build();

        // THEN: Evidence request is structured
        assertThat(request.getEvidenceType()).isEqualTo("GATEWAY_RECONCILIATION_REPORT");
        assertThat(request.getDescription()).contains("2024-01-15");
        assertThat(request.getReason()).contains("verify if payment was actually received");
        assertThat(request.getExpectedImpact()).contains("definitively confirm or refute");
    }

    @Test
    void testRelatedExceptionDto_Creation() {
        // GIVEN: A related exception with relationship type
        RelatedExceptionDto related = RelatedExceptionDto.builder()
                .exceptionId(100L)
                .exceptionType(ExceptionType.AMOUNT_MISMATCH)
                .merchantId("merch_001")
                .amount(BigDecimal.valueOf(500.00))
                .createdAt(LocalDateTime.now().minusHours(2))
                .relationshipType(RelatedExceptionDto.RelationshipType.SAME_PAYMENT)
                .relationshipReason("Same paymentId: pay_abc123")
                .build();

        // THEN: Related exception structure is correct
        assertThat(related.getExceptionId()).isEqualTo(100L);
        assertThat(related.getExceptionType()).isEqualTo(ExceptionType.AMOUNT_MISMATCH);
        assertThat(related.getMerchantId()).isEqualTo("merch_001");
        assertThat(related.getRelationshipType()).isEqualTo(RelatedExceptionDto.RelationshipType.SAME_PAYMENT);
        assertThat(related.getRelationshipReason()).contains("pay_abc123");
    }

    @Test
    void testRelatedExceptionService_FindsBySamePaymentId() {
        // GIVEN: Two exceptions with the same payment, same merchant
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setPaymentId("pay_123");
        
        FinancialException exception1 = createException(UUID.randomUUID(), "merch_001", payment, null, null, null);
        FinancialException exception2 = createException(UUID.randomUUID(), "merch_001", payment, null, null, null);

        when(exceptionRepository.findByMerchantId(anyString()))
                .thenReturn(Arrays.asList(exception1, exception2));

        // WHEN: Finding related exceptions
        List<RelatedExceptionDto> related = relatedExceptionService.findRelatedExceptions(exception1);

        // THEN: Found related exception with SAME_PAYMENT relationship
        assertThat(related).hasSizeGreaterThan(0);
        assertThat(related.get(0).getRelationshipType()).isEqualTo(RelatedExceptionDto.RelationshipType.SAME_PAYMENT);
        assertThat(related.get(0).getMerchantId()).isEqualTo("merch_001");
    }

    @Test
    void testRelatedExceptionService_FindsBySameSettlementId() {
        // GIVEN: Two exceptions with the same settlement, same merchant
        Settlement settlement = new Settlement();
        settlement.setId(UUID.randomUUID());
        settlement.setSettlementId("settle_456");
        
        FinancialException exception1 = createException(UUID.randomUUID(), "merch_001", null, settlement, null, null);
        FinancialException exception2 = createException(UUID.randomUUID(), "merch_001", null, settlement, null, null);

        when(exceptionRepository.findByMerchantId(anyString()))
                .thenReturn(Arrays.asList(exception1, exception2));

        // WHEN: Finding related exceptions
        List<RelatedExceptionDto> related = relatedExceptionService.findRelatedExceptions(exception1);

        // THEN: Found related exception with SAME_SETTLEMENT relationship
        assertThat(related).hasSizeGreaterThan(0);
        assertThat(related.get(0).getRelationshipType()).isEqualTo(RelatedExceptionDto.RelationshipType.SAME_SETTLEMENT);
    }

    @Test
    void testRelatedExceptionService_FindsBySameRefundId() {
        // GIVEN: Two exceptions with the same refund, same merchant
        Refund refund = new Refund();
        refund.setId(UUID.randomUUID());
        refund.setRefundId("refund_789");
        
        FinancialException exception1 = createException(UUID.randomUUID(), "merch_001", null, null, refund, null);
        FinancialException exception2 = createException(UUID.randomUUID(), "merch_001", null, null, refund, null);

        when(exceptionRepository.findByMerchantId(anyString()))
                .thenReturn(Arrays.asList(exception1, exception2));

        // WHEN: Finding related exceptions
        List<RelatedExceptionDto> related = relatedExceptionService.findRelatedExceptions(exception1);

        // THEN: Found related exception with SAME_REFUND relationship
        assertThat(related).hasSizeGreaterThan(0);
        assertThat(related.get(0).getRelationshipType()).isEqualTo(RelatedExceptionDto.RelationshipType.SAME_REFUND);
    }

    @Test
    void testRelatedExceptionService_FindsBySameExceptionType() {
        // GIVEN: Two exceptions with the same type, same merchant, recent
        FinancialException exception1 = createException(UUID.randomUUID(), "merch_001", null, null, null, ExceptionType.AMOUNT_MISMATCH);
        FinancialException exception2 = createException(UUID.randomUUID(), "merch_001", null, null, null, ExceptionType.AMOUNT_MISMATCH);

        when(exceptionRepository.findByMerchantId(anyString()))
                .thenReturn(Arrays.asList(exception1, exception2));

        // WHEN: Finding related exceptions
        List<RelatedExceptionDto> related = relatedExceptionService.findRelatedExceptions(exception1);

        // THEN: Found related exception with SAME_EXCEPTION_TYPE relationship
        assertThat(related).hasSizeGreaterThan(0);
        assertThat(related.get(0).getRelationshipType()).isEqualTo(RelatedExceptionDto.RelationshipType.SAME_EXCEPTION_TYPE);
    }

    @Test
    void testRelatedExceptionService_MerchantIsolation() {
        // GIVEN: Exceptions from different merchants with same payment
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setPaymentId("pay_123");
        
        FinancialException exception1 = createException(UUID.randomUUID(), "merch_001", payment, null, null, null);
        // exception2 is different merchant, should not be returned

        when(exceptionRepository.findByMerchantId("merch_001"))
                .thenReturn(Arrays.asList(exception1)); // Only same merchant

        // WHEN: Finding related exceptions
        List<RelatedExceptionDto> related = relatedExceptionService.findRelatedExceptions(exception1);

        // THEN: Does NOT find cross-merchant exception (empty because only one exception in merchant)
        assertThat(related).isEmpty();
    }

    @Test
    void testRelatedExceptionService_MaxFiveResults() {
        // GIVEN: 10 exceptions with same type
        FinancialException exception1 = createException(UUID.randomUUID(), "merch_001", null, null, null, ExceptionType.AMOUNT_MISMATCH);
        List<FinancialException> manyExceptions = new ArrayList<>();
        manyExceptions.add(exception1);
        for (int i = 0; i < 10; i++) {
            manyExceptions.add(createException(UUID.randomUUID(), "merch_001", null, null, null, ExceptionType.AMOUNT_MISMATCH));
        }

        when(exceptionRepository.findByMerchantId(anyString()))
                .thenReturn(manyExceptions);

        // WHEN: Finding related exceptions
        List<RelatedExceptionDto> related = relatedExceptionService.findRelatedExceptions(exception1);

        // THEN: Returns max 5 results
        assertThat(related).hasSizeLessThanOrEqualTo(5);
    }

    @Test
    void testFinancialAmountValidator_RejectsFlabricatedExceptionIds() {
        // GIVEN: Backend supplies related exception IDs [100, 101]
        List<RelatedExceptionDto> suppliedRelated = Arrays.asList(
                RelatedExceptionDto.builder().exceptionId(100L).merchantId("merch_001").build(),
                RelatedExceptionDto.builder().exceptionId(101L).merchantId("merch_001").build()
        );

        // AND: AI response fabricates exception ID 999
        List<RelatedExceptionDto> aiRelated = Arrays.asList(
                RelatedExceptionDto.builder().exceptionId(100L).merchantId("merch_001").build(),
                RelatedExceptionDto.builder().exceptionId(999L).merchantId("merch_001").build() // FABRICATED
        );

        AiInvestigationResponse aiResponse = AiInvestigationResponse.builder()
                .summary("Analysis")
                .relatedExceptions(aiRelated)
                .build();

        // WHEN: Validating forensic reasoning
        boolean valid = validator.validateForensicReasoning(aiResponse, suppliedRelated);

        // THEN: Validation FAILS due to fabricated ID
        assertThat(valid).isFalse();
    }

    @Test
    void testFinancialAmountValidator_AcceptsValidRelatedExceptions() {
        // GIVEN: Backend supplies related exception IDs [100, 101]
        List<RelatedExceptionDto> suppliedRelated = Arrays.asList(
                RelatedExceptionDto.builder().exceptionId(100L).merchantId("merch_001").build(),
                RelatedExceptionDto.builder().exceptionId(101L).merchantId("merch_001").build()
        );

        // AND: AI response uses only supplied IDs
        List<RelatedExceptionDto> aiRelated = Arrays.asList(
                RelatedExceptionDto.builder().exceptionId(100L).merchantId("merch_001").build(),
                RelatedExceptionDto.builder().exceptionId(101L).merchantId("merch_001").build()
        );

        AiInvestigationResponse aiResponse = AiInvestigationResponse.builder()
                .summary("Analysis")
                .relatedExceptions(aiRelated)
                .build();

        // WHEN: Validating forensic reasoning
        boolean valid = validator.validateForensicReasoning(aiResponse, suppliedRelated);

        // THEN: Validation PASSES
        assertThat(valid).isTrue();
    }

    @Test
    void testFinancialAmountValidator_RejectsInvalidHypothesisConfidence() {
        // GIVEN: Hypothesis with confidence > 100
        List<HypothesisDto> hypotheses = Arrays.asList(
                HypothesisDto.builder()
                        .hypothesis("Test hypothesis")
                        .confidence(BigDecimal.valueOf(150.0)) // INVALID
                        .status(HypothesisDto.HypothesisStatus.UNRESOLVED)
                        .build()
        );

        AiInvestigationResponse aiResponse = AiInvestigationResponse.builder()
                .summary("Analysis")
                .hypotheses(hypotheses)
                .build();

        // WHEN: Validating forensic reasoning
        boolean valid = validator.validateForensicReasoning(aiResponse, List.of());

        // THEN: Validation FAILS due to invalid confidence
        assertThat(valid).isFalse();
    }

    @Test
    void testFinancialAmountValidator_AcceptsValidHypothesisConfidence() {
        // GIVEN: Hypotheses with valid confidence (0-100)
        List<HypothesisDto> hypotheses = Arrays.asList(
                HypothesisDto.builder()
                        .hypothesis("Hypothesis 1")
                        .confidence(BigDecimal.valueOf(75.0))
                        .status(HypothesisDto.HypothesisStatus.SUPPORTED)
                        .build(),
                HypothesisDto.builder()
                        .hypothesis("Hypothesis 2")
                        .confidence(BigDecimal.valueOf(25.0))
                        .status(HypothesisDto.HypothesisStatus.WEAKENED)
                        .build()
        );

        AiInvestigationResponse aiResponse = AiInvestigationResponse.builder()
                .summary("Analysis")
                .hypotheses(hypotheses)
                .build();

        // WHEN: Validating forensic reasoning
        boolean valid = validator.validateForensicReasoning(aiResponse, List.of());

        // THEN: Validation PASSES
        assertThat(valid).isTrue();
    }

    @Test
    void testAiInvestigationResponse_ContainsForensicFields() {
        // GIVEN: Complete AI response with all forensic fields
        AiInvestigationResponse response = AiInvestigationResponse.builder()
                .summary("Analysis summary")
                .likelyRootCause("Root cause identified")
                .hypotheses(Arrays.asList(
                        HypothesisDto.builder()
                                .hypothesis("Hypothesis 1")
                                .confidence(BigDecimal.valueOf(80.0))
                                .status(HypothesisDto.HypothesisStatus.SUPPORTED)
                                .build()
                ))
                .contradictions(Arrays.asList(
                        ContradictionDto.builder()
                                .contradiction("Evidence conflict")
                                .evidenceA("Evidence A")
                                .evidenceB("Evidence B")
                                .severity(ContradictionDto.ContradictionSeverity.MEDIUM)
                                .build()
                ))
                .additionalEvidenceRequired(Arrays.asList(
                        EvidenceRequestDto.builder()
                                .evidenceType("TRANSACTION_LOG")
                                .description("Full transaction log")
                                .reason("Need to verify sequence")
                                .build()
                ))
                .relatedExceptions(Arrays.asList(
                        RelatedExceptionDto.builder()
                                .exceptionId(100L)
                                .relationshipType(RelatedExceptionDto.RelationshipType.SAME_PAYMENT)
                                .build()
                ))
                .build();

        // THEN: All forensic fields are present
        assertThat(response.getHypotheses()).hasSize(1);
        assertThat(response.getContradictions()).hasSize(1);
        assertThat(response.getAdditionalEvidenceRequired()).hasSize(1);
        assertThat(response.getRelatedExceptions()).hasSize(1);
    }

    private FinancialException createException(UUID id, String merchantId, Payment payment, 
                                              Settlement settlement, Refund refund, ExceptionType type) {
        FinancialException exception = new FinancialException();
        exception.setId(id);
        exception.setMerchantId(merchantId);
        exception.setPayment(payment);
        exception.setSettlement(settlement);
        exception.setRefund(refund);
        exception.setExceptionType(type != null ? type : ExceptionType.AMOUNT_MISMATCH);
        exception.setExpectedAmount(BigDecimal.valueOf(100.00));
        exception.setActualAmount(BigDecimal.valueOf(100.00));
        exception.setDiscrepancyAmount(BigDecimal.ZERO);
        exception.setSeverity(ExceptionSeverity.MEDIUM);
        exception.setStatus(ExceptionStatus.OPEN);
        exception.setCreatedAt(OffsetDateTime.now().minusHours(1));
        return exception;
    }
}
