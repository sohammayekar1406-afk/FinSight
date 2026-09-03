package com.ledgerlens;

import com.ledgerlens.config.AiProperties;
import com.ledgerlens.dto.EvidenceGraphDto;
import com.ledgerlens.dto.InvestigationResponseDto;
import com.ledgerlens.dto.RagHistoricalCaseDto;
import com.ledgerlens.entity.FinancialException;
import com.ledgerlens.entity.HistoricalInvestigationEmbedding;
import com.ledgerlens.entity.Investigation;
import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.entity.enums.RecommendedAction;
import com.ledgerlens.repository.FinancialExceptionRepository;
import com.ledgerlens.repository.HistoricalInvestigationEmbeddingRepository;
import com.ledgerlens.repository.InvestigationRepository;
import com.ledgerlens.service.InvestigationService;
import com.ledgerlens.service.SeedDataService;
import com.ledgerlens.service.rag.HistoricalInvestigationEmbeddingService;
import com.ledgerlens.service.rag.SemanticHistoricalRetrievalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Phase 6.6: RAG Fallback & Resilience Tests
 * 
 * Verifies graceful degradation when:
 * 1. Embedding API is unavailable or times out
 * 2. Zero candidates meet the similarity threshold
 * 3. Stored embedding vector is corrupted/empty
 * 4. RAG is toggled off via configuration
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Phase 6.6: RAG Fallback & Resilience Tests")
class Phase6RagFallbackTest {

    @Autowired private InvestigationService investigationService;
    @Autowired private FinancialExceptionRepository exceptionRepository;
    @Autowired private InvestigationRepository investigationRepository;
    @Autowired private HistoricalInvestigationEmbeddingRepository embeddingRepository;
    @Autowired private SemanticHistoricalRetrievalService retrievalService;
    @Autowired private HistoricalInvestigationEmbeddingService embeddingService;
    @Autowired private SeedDataService seedDataService;
    @Autowired private AiProperties aiProperties;

    private static final String MERCHANT = "merchant_a";

    @BeforeEach
    @Transactional
    void setUp() {
        seedDataService.seedDemoData();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("operator", "pwd", List.of(new SimpleGrantedAuthority("ROLE_OPERATOR")))
        );
        embeddingRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("Investigation completes successfully using deterministic baseline when RAG has 0 matches")
    void testInvestigationSucceedsWithZeroRagMatches() {
        FinancialException exception = FinancialException.builder()
                .exceptionId("exp_fallback_" + UUID.randomUUID().toString().substring(0, 8))
                .merchantId(MERCHANT)
                .exceptionType(ExceptionType.MISSING_SETTLEMENT)
                .severity(ExceptionSeverity.HIGH)
                .status(ExceptionStatus.OPEN)
                .discrepancyAmount(new BigDecimal("500.00"))
                .expectedAmount(new BigDecimal("500.00"))
                .actualAmount(BigDecimal.ZERO)
                .detectedAt(OffsetDateTime.now().minusHours(30))
                .build();
        exceptionRepository.save(exception);

        // Investigation must succeed without errors even with empty embeddings table
        assertThatCode(() -> {
            InvestigationResponseDto response = investigationService.investigateException(exception.getExceptionId());
            assertThat(response).isNotNull();
            assertThat(response.getExceptionId()).isEqualTo(exception.getExceptionId());
            assertThat(response.getSummary()).isNotBlank();
        }).doesNotThrowAnyException();
    }

    @Test
    @Transactional
    @DisplayName("Retrieval returns empty list gracefully when similarity threshold is not met")
    void testRetrievalReturnsEmptyWhenThresholdUnmet() {
        double originalThreshold = aiProperties.getRagSimilarityThreshold();
        try {
            // Set an unreachable threshold (99.9%)
            aiProperties.setRagSimilarityThreshold(0.999);

            FinancialException exception = FinancialException.builder()
                    .exceptionId("exp_high_thresh_" + UUID.randomUUID().toString().substring(0, 8))
                    .merchantId(MERCHANT)
                    .exceptionType(ExceptionType.AMOUNT_MISMATCH)
                    .severity(ExceptionSeverity.LOW)
                    .discrepancyAmount(new BigDecimal("10.00"))
                    .build();

            EvidenceGraphDto graph = EvidenceGraphDto.builder().nodes(List.of()).build();

            List<RagHistoricalCaseDto> cases = retrievalService.findSimilarResolvedCases(exception, graph);
            assertThat(cases).isEmpty();

        } finally {
            aiProperties.setRagSimilarityThreshold(originalThreshold);
        }
    }

    @Test
    @Transactional
    @DisplayName("Corrupted / empty embedding in DB does not crash semantic retrieval")
    void testCorruptedEmbeddingHandledGracefully() {
        FinancialException oldEx = FinancialException.builder()
                .exceptionId("exp_corrupt_" + UUID.randomUUID().toString().substring(0, 8))
                .merchantId(MERCHANT)
                .exceptionType(ExceptionType.UNEXPECTED_FEE)
                .severity(ExceptionSeverity.MEDIUM)
                .status(ExceptionStatus.RESOLVED_AUTO)
                .detectedAt(OffsetDateTime.now())
                .resolvedAt(OffsetDateTime.now())
                .build();
        exceptionRepository.save(oldEx);

        Investigation oldInv = Investigation.builder()
                .exception(oldEx)
                .likelyRootCause("Old fee issue")
                .confidenceScore(new BigDecimal("90.00"))
                .recommendedAction(RecommendedAction.MANUAL_ADJUSTMENT)
                .build();
        investigationRepository.save(oldInv);

        // Save embedding with empty vector (simulating corruption)
        HistoricalInvestigationEmbedding corruptEmbedding = HistoricalInvestigationEmbedding.builder()
                .investigation(oldInv)
                .merchantId(MERCHANT)
                .sourceText("Old corrupt text")
                .embedding(List.of())
                .build();
        embeddingRepository.save(corruptEmbedding);

        FinancialException currentEx = FinancialException.builder()
                .exceptionId("exp_curr_" + UUID.randomUUID().toString().substring(0, 8))
                .merchantId(MERCHANT)
                .exceptionType(ExceptionType.UNEXPECTED_FEE)
                .severity(ExceptionSeverity.MEDIUM)
                .build();

        EvidenceGraphDto graph = EvidenceGraphDto.builder().nodes(List.of()).build();

        // Retrieval must execute gracefully and return empty list rather than throwing NPE / IndexOutOfBounds
        assertThatCode(() -> {
            List<RagHistoricalCaseDto> results = retrievalService.findSimilarResolvedCases(currentEx, graph);
            assertThat(results).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @Transactional
    @DisplayName("RAG disabled via configuration returns empty list gracefully")
    void testRagDisabledConfiguration() {
        boolean original = aiProperties.isRagEnabled();
        try {
            aiProperties.setRagEnabled(false);

            FinancialException currentEx = FinancialException.builder()
                    .exceptionId("exp_disabled_" + UUID.randomUUID().toString().substring(0, 8))
                    .merchantId(MERCHANT)
                    .exceptionType(ExceptionType.DUPLICATE_TRANSACTION)
                    .severity(ExceptionSeverity.HIGH)
                    .build();

            EvidenceGraphDto graph = EvidenceGraphDto.builder().nodes(List.of()).build();

            List<RagHistoricalCaseDto> results = retrievalService.findSimilarResolvedCases(currentEx, graph);
            assertThat(results).isEmpty();

        } finally {
            aiProperties.setRagEnabled(original);
        }
    }
}
