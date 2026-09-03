package com.ledgerlens;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgerlens.config.AiProperties;
import com.ledgerlens.dto.InvestigationAnalysis;
import com.ledgerlens.dto.InvestigationEvidenceDto;
import com.ledgerlens.dto.InvestigationResponseDto;
import com.ledgerlens.entity.FinancialException;
import com.ledgerlens.entity.Investigation;
import com.ledgerlens.entity.enums.*;
import com.ledgerlens.exception.ResourceNotFoundException;
import com.ledgerlens.repository.AuditLogRepository;
import com.ledgerlens.repository.FinancialExceptionRepository;
import com.ledgerlens.repository.InvestigationRepository;
import com.ledgerlens.service.EvidenceCollectionService;
import com.ledgerlens.service.FinancialAnomalyService;
import com.ledgerlens.service.HistoricalInvestigationService;
import com.ledgerlens.service.InvestigationService;
import com.ledgerlens.service.MerchantContext;
import com.ledgerlens.service.RelatedExceptionService;
import com.ledgerlens.service.ai.AiInvestigationAnalyzer;
import com.ledgerlens.service.ai.FinancialAmountValidator;
import com.ledgerlens.service.analyzer.InvestigationAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class InvestigationServiceTest {

    @Mock private FinancialExceptionRepository exceptionRepository;
    @Mock private InvestigationRepository investigationRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private EvidenceCollectionService evidenceCollectionService;
    @Mock private InvestigationAnalyzer ruleBasedAnalyzer;
    @Mock private AiInvestigationAnalyzer aiInvestigationAnalyzer;
    @Mock private MerchantContext merchantContext;
    @Mock private HistoricalInvestigationService historicalInvestigationService;
    @Mock private FinancialAnomalyService financialAnomalyService;
    @Mock private RelatedExceptionService relatedExceptionService;
    @Mock private com.ledgerlens.service.EvidenceGraphService evidenceGraphService;
    @Mock private com.ledgerlens.service.rag.HistoricalInvestigationEmbeddingService historicalInvestigationEmbeddingService;
    @Mock private com.ledgerlens.service.rag.SemanticHistoricalRetrievalService semanticHistoricalRetrievalService;
    @Spy private FinancialAmountValidator financialAmountValidator = new FinancialAmountValidator();
    @Spy private AiProperties aiProperties = new AiProperties();
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private InvestigationService investigationService;

    private FinancialException sampleException;
    private InvestigationEvidenceDto sampleEvidence;
    private InvestigationAnalysis sampleAnalysis;

    @BeforeEach
    void setUp() {
        // Phase 3: Setup merchant context and services with lenient mocking
        lenient().when(merchantContext.merchantId()).thenReturn("merch_001");
        lenient().when(historicalInvestigationService.findSimilarResolvedInvestigations(any())).thenReturn(List.of());
        lenient().when(financialAnomalyService.detectAnomalies(any())).thenReturn(List.of());
        lenient().when(relatedExceptionService.findRelatedExceptions(any())).thenReturn(List.of());
        // Phase 3.5: EvidenceGraphService mocks
        lenient().when(evidenceGraphService.buildEvidenceGraph(any(), any())).thenReturn(com.ledgerlens.dto.EvidenceGraphDto.builder().nodes(List.of()).totalNodesRetrieved(0).foundNodes(0).missingNodes(0).build());
        lenient().when(evidenceGraphService.calculateSufficiency(any(), any())).thenReturn(com.ledgerlens.dto.EvidenceSufficiencyDto.builder().sufficiencyScore(new java.math.BigDecimal("80")).assessment("SUFFICIENT").foundEvidence(List.of()).missingEvidence(List.of()).reasoning("Sufficient").build());
        // Phase 6: RAG mocks
        lenient().when(semanticHistoricalRetrievalService.findSimilarResolvedCases(any(), any())).thenReturn(List.of());

        sampleException = FinancialException.builder()
                .id(UUID.randomUUID())
                .exceptionId("exp_1002")
                .merchantId("merch_001")
                .exceptionType(ExceptionType.AMOUNT_MISMATCH)
                .severity(ExceptionSeverity.MEDIUM)
                .status(ExceptionStatus.OPEN)
                .discrepancyAmount(new BigDecimal("500.00"))
                .expectedAmount(new BigDecimal("976.40"))
                .actualAmount(new BigDecimal("476.40"))
                .description("Settlement mismatch")
                .detectedAt(OffsetDateTime.now())
                .build();

        sampleEvidence = InvestigationEvidenceDto.builder()
                .lineage("Order ord_1002 -> Settlement set_1002")
                .build();

        sampleAnalysis = InvestigationAnalysis.builder()
                .summary("Shortfall of 500")
                .likelyRootCause("500 unexplained")
                .confidenceScore(new BigDecimal("96.00"))
                .recommendedAction(RecommendedAction.HUMAN_REVIEW_REQUIRED)
                .actionTaken(ActionTaken.SENT_TO_HUMAN)
                .autoResolved(false)
                .evidence(sampleEvidence)
                .build();
    }

    @Test
    void testInvestigateExceptionPersistenceAndAuditLog() {
        when(exceptionRepository.findByExceptionIdAndMerchantId("exp_1002", "merch_001")).thenReturn(Optional.of(sampleException));
        when(investigationRepository.findByException_ExceptionIdAndException_MerchantId("exp_1002", "merch_001")).thenReturn(Optional.empty());
        when(evidenceCollectionService.collectEvidence(sampleException)).thenReturn(sampleEvidence);
        when(ruleBasedAnalyzer.analyze(sampleEvidence)).thenReturn(sampleAnalysis);

        when(investigationRepository.save(any())).thenAnswer(invocation -> {
            Investigation inv = invocation.getArgument(0);
            inv.setId(UUID.randomUUID());
            return inv;
        });

        InvestigationResponseDto response = investigationService.investigateException("exp_1002");

        assertNotNull(response);
        assertEquals("exp_1002", response.getExceptionId());
        assertEquals("96.00", response.getConfidenceScore().toString());
        assertEquals(RecommendedAction.HUMAN_REVIEW_REQUIRED, response.getRecommendedAction());
        assertFalse(response.isAiUsed());
        assertEquals("RULE_BASED_FALLBACK", response.getAnalysisSource());

        verify(investigationRepository).save(any());
        verify(auditLogRepository).save(any());
    }

    @Test
    void testInvestigateExceptionIdempotency() {
        Investigation existingInvestigation = Investigation.builder()
                .id(UUID.randomUUID())
                .exception(sampleException)
                .summary("Existing investigation")
                .likelyRootCause("Already investigated")
                .confidenceScore(new BigDecimal("96.00"))
                .recommendedAction(RecommendedAction.HUMAN_REVIEW_REQUIRED)
                .actionTaken(ActionTaken.SENT_TO_HUMAN)
                .aiModelVersion("rule-based-v1.0")
                .investigatedAt(OffsetDateTime.now())
                .build();

        when(exceptionRepository.findByExceptionIdAndMerchantId("exp_1002", "merch_001")).thenReturn(Optional.of(sampleException));
        when(investigationRepository.findByException_ExceptionIdAndException_MerchantId("exp_1002", "merch_001")).thenReturn(Optional.of(existingInvestigation));
        when(evidenceCollectionService.collectEvidence(sampleException)).thenReturn(sampleEvidence);

        InvestigationResponseDto response = investigationService.investigateException("exp_1002");

        assertNotNull(response);
        assertEquals("exp_1002", response.getExceptionId());
        verify(investigationRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void testInvestigateNonExistentExceptionThrowsException() {
        when(exceptionRepository.findByExceptionIdAndMerchantId("non_existent", "merch_001")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> investigationService.investigateException("non_existent"));
    }

    @Test
    void testResolveExceptionManuallySuccess() {
        Investigation existingInvestigation = Investigation.builder()
                .id(UUID.randomUUID())
                .exception(sampleException)
                .summary("Shortfall of 500")
                .likelyRootCause("500 unexplained")
                .confidenceScore(new BigDecimal("96.00"))
                .recommendedAction(RecommendedAction.HUMAN_REVIEW_REQUIRED)
                .actionTaken(ActionTaken.SENT_TO_HUMAN)
                .aiModelVersion("rule-based-v1.0")
                .investigatedAt(OffsetDateTime.now())
                .build();

        when(exceptionRepository.findByExceptionIdAndMerchantId("exp_1002", "merch_001")).thenReturn(Optional.of(sampleException));
        when(investigationRepository.findByException_ExceptionIdAndException_MerchantId("exp_1002", "merch_001")).thenReturn(Optional.of(existingInvestigation));
        when(evidenceCollectionService.collectEvidence(sampleException)).thenReturn(sampleEvidence);

        InvestigationResponseDto response = investigationService.resolveExceptionManually("exp_1002");

        assertNotNull(response);
        assertEquals(ExceptionStatus.RESOLVED_MANUAL, sampleException.getStatus());
        assertEquals(ActionTaken.MANUALLY_OVERRIDDEN, existingInvestigation.getActionTaken());
        assertNotNull(sampleException.getResolvedAt());

        verify(exceptionRepository).save(sampleException);
        verify(investigationRepository).save(existingInvestigation);
        verify(auditLogRepository).save(argThat(audit ->
            "HUMAN_REVIEW_RESOLVED".equals(audit.getAction()) && "HUMAN_OPERATOR".equals(audit.getPerformedBy())
        ));
    }
}
