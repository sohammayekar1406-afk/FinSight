package com.ledgerlens;

import com.ledgerlens.dto.EvidenceGraphDto;
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

/**
 * Phase 6.7: Mandatory Merchant Isolation RAG Tests
 * 
 * Verifies that:
 * 1. Semantic retrieval never returns another merchant's historical investigation, root cause, resolution,
 *    exception ID, or embedding, even when that other merchant's case is semantically identical.
 * 2. Merchant isolation is strictly enforced at the SQL / Repository query layer.
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Phase 6.7: Merchant Isolation RAG Tests")
class Phase6MerchantIsolationRagTest {

    @Autowired private HistoricalInvestigationEmbeddingRepository embeddingRepository;
    @Autowired private InvestigationRepository investigationRepository;
    @Autowired private FinancialExceptionRepository exceptionRepository;
    @Autowired private HistoricalInvestigationEmbeddingService embeddingService;
    @Autowired private SemanticHistoricalRetrievalService retrievalService;
    @Autowired private SeedDataService seedDataService;

    private static final String MERCHANT_A = "merchant_a";
    private static final String MERCHANT_B = "merchant_b";

    private FinancialException merchantAException;
    private FinancialException merchantBException;
    private Investigation merchantAInvestigation;
    private Investigation merchantBInvestigation;

    @BeforeEach
    @Transactional
    void setUp() {
        seedDataService.seedDemoData();
        embeddingRepository.deleteAll();

        // Create identical defect scenarios for Merchant A and Merchant B
        merchantAException = FinancialException.builder()
                .exceptionId("exp_iso_a_" + UUID.randomUUID().toString().substring(0, 8))
                .merchantId(MERCHANT_A)
                .exceptionType(ExceptionType.AMOUNT_MISMATCH)
                .severity(ExceptionSeverity.HIGH)
                .status(ExceptionStatus.RESOLVED_AUTO)
                .discrepancyAmount(new BigDecimal("150.00"))
                .expectedAmount(new BigDecimal("1000.00"))
                .actualAmount(new BigDecimal("850.00"))
                .description("Gateway promo fee discrepancy of 150 INR")
                .detectedAt(OffsetDateTime.now())
                .resolvedAt(OffsetDateTime.now())
                .build();
        exceptionRepository.save(merchantAException);

        merchantAInvestigation = Investigation.builder()
                .exception(merchantAException)
                .likelyRootCause("Merchant A Promo coupon code discount misconfigured at payment gateway")
                .confidenceScore(new BigDecimal("95.00"))
                .recommendedAction(RecommendedAction.MANUAL_ADJUSTMENT)
                .summary("Merchant A investigated promo discount mismatch")
                .autoResolved(true)
                .build();
        investigationRepository.save(merchantAInvestigation);

        merchantBException = FinancialException.builder()
                .exceptionId("exp_iso_b_" + UUID.randomUUID().toString().substring(0, 8))
                .merchantId(MERCHANT_B)
                .exceptionType(ExceptionType.AMOUNT_MISMATCH)
                .severity(ExceptionSeverity.HIGH)
                .status(ExceptionStatus.RESOLVED_AUTO)
                .discrepancyAmount(new BigDecimal("150.00"))
                .expectedAmount(new BigDecimal("1000.00"))
                .actualAmount(new BigDecimal("850.00"))
                .description("Gateway promo fee discrepancy of 150 INR")
                .detectedAt(OffsetDateTime.now())
                .resolvedAt(OffsetDateTime.now())
                .build();
        exceptionRepository.save(merchantBException);

        merchantBInvestigation = Investigation.builder()
                .exception(merchantBException)
                .likelyRootCause("Merchant B Confidential Banking API token mismatch and promo fee leak")
                .confidenceScore(new BigDecimal("98.00"))
                .recommendedAction(RecommendedAction.HUMAN_REVIEW_REQUIRED)
                .summary("Merchant B proprietary settlement anomaly")
                .autoResolved(true)
                .build();
        investigationRepository.save(merchantBInvestigation);

        // Generate and persist embeddings for both merchants
        embeddingService.embedAndPersistResolvedInvestigation(merchantAInvestigation);
        embeddingService.embedAndPersistResolvedInvestigation(merchantBInvestigation);
    }

    @Test
    @Transactional
    @DisplayName("Merchant A semantic retrieval never leaks Merchant B data despite identical exception metadata")
    void testMerchantANeverRetrievesMerchantBEmbeddings() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("operator", "pwd", List.of(new SimpleGrantedAuthority("ROLE_OPERATOR")))
        );

        FinancialException currentCase = FinancialException.builder()
                .exceptionId("exp_current_a_" + UUID.randomUUID().toString().substring(0, 8))
                .merchantId(MERCHANT_A)
                .exceptionType(ExceptionType.AMOUNT_MISMATCH)
                .severity(ExceptionSeverity.HIGH)
                .discrepancyAmount(new BigDecimal("150.00"))
                .description("Gateway promo fee discrepancy of 150 INR")
                .build();

        EvidenceGraphDto graph = EvidenceGraphDto.builder().nodes(List.of()).build();

        List<RagHistoricalCaseDto> results = retrievalService.findSimilarResolvedCases(currentCase, graph);

        assertThat(results).isNotEmpty();
        for (RagHistoricalCaseDto result : results) {
            assertThat(result.getMerchantId()).isEqualTo(MERCHANT_A);
            assertThat(result.getExceptionId()).isNotEqualTo(merchantBException.getExceptionId());
            assertThat(result.getPreviousRootCause()).doesNotContain("Merchant B");
            assertThat(result.getPreviousRootCause()).doesNotContain("Confidential Banking API token");
        }
    }

    @Test
    @Transactional
    @DisplayName("Merchant B semantic retrieval never leaks Merchant A data")
    void testMerchantBNeverRetrievesMerchantAEmbeddings() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("merchant_b_operator", "pwd", List.of(new SimpleGrantedAuthority("ROLE_OPERATOR")))
        );

        FinancialException currentCase = FinancialException.builder()
                .exceptionId("exp_current_b_" + UUID.randomUUID().toString().substring(0, 8))
                .merchantId(MERCHANT_B)
                .exceptionType(ExceptionType.AMOUNT_MISMATCH)
                .severity(ExceptionSeverity.HIGH)
                .discrepancyAmount(new BigDecimal("150.00"))
                .description("Gateway promo fee discrepancy of 150 INR")
                .build();

        EvidenceGraphDto graph = EvidenceGraphDto.builder().nodes(List.of()).build();

        List<RagHistoricalCaseDto> results = retrievalService.findSimilarResolvedCases(currentCase, graph);

        assertThat(results).isNotEmpty();
        for (RagHistoricalCaseDto result : results) {
            assertThat(result.getMerchantId()).isEqualTo(MERCHANT_B);
            assertThat(result.getExceptionId()).isNotEqualTo(merchantAException.getExceptionId());
            assertThat(result.getPreviousRootCause()).doesNotContain("Merchant A");
        }
    }

    @Test
    @Transactional
    @DisplayName("Repository layer SQL query enforces merchant isolation at query level")
    void testRepositoryLayerQueryEnforcesMerchantIsolation() {
        List<HistoricalInvestigationEmbedding> aEmbeddings = 
                embeddingRepository.findResolvedEmbeddingsByMerchant(MERCHANT_A, "exp_none");
        
        List<HistoricalInvestigationEmbedding> bEmbeddings = 
                embeddingRepository.findResolvedEmbeddingsByMerchant(MERCHANT_B, "exp_none");

        assertThat(aEmbeddings).allMatch(e -> MERCHANT_A.equals(e.getMerchantId()));
        assertThat(bEmbeddings).allMatch(e -> MERCHANT_B.equals(e.getMerchantId()));

        assertThat(aEmbeddings).noneMatch(e -> MERCHANT_B.equals(e.getMerchantId()));
        assertThat(bEmbeddings).noneMatch(e -> MERCHANT_A.equals(e.getMerchantId()));
    }
}
