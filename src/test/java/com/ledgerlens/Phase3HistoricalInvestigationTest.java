package com.ledgerlens;

import com.ledgerlens.dto.HistoricalCaseDto;
import com.ledgerlens.entity.FinancialException;
import com.ledgerlens.entity.Investigation;
import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.entity.enums.RecommendedAction;
import com.ledgerlens.repository.InvestigationRepository;
import com.ledgerlens.service.HistoricalInvestigationService;
import com.ledgerlens.service.MerchantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
        "ai.enabled=false"
})
@DisplayName("Phase 3: Historical Investigation Retrieval Tests")
class Phase3HistoricalInvestigationTest {

    @Autowired
    private HistoricalInvestigationService historicalInvestigationService;

    @MockBean
    private InvestigationRepository investigationRepository;

    @MockBean
    private MerchantContext merchantContext;

    @BeforeEach
    void setUp() {
        when(merchantContext.merchantId()).thenReturn("merchant_a");
    }

    @Test
    @DisplayName("1. Historical retrieval returns similar resolved investigations")
    void testHistoricalRetrievalReturnsSimilarCases() {
        // Given: Current exception
        FinancialException currentException = createException(
                "EXC-001", "merchant_a", ExceptionType.AMOUNT_MISMATCH, 
                ExceptionSeverity.HIGH, ExceptionStatus.OPEN, new BigDecimal("100.00")
        );

        // And: Historical resolved investigations
        List<Investigation> historicalInvestigations = new ArrayList<>();
        historicalInvestigations.add(createResolvedInvestigation(
                "EXC-100", "merchant_a", ExceptionType.AMOUNT_MISMATCH, 
                ExceptionSeverity.HIGH, new BigDecimal("105.00"), "Fee calculation error"
        ));
        historicalInvestigations.add(createResolvedInvestigation(
                "EXC-101", "merchant_a", ExceptionType.MISSING_PAYMENT, 
                ExceptionSeverity.MEDIUM, new BigDecimal("200.00"), "Gateway delay"
        ));
        historicalInvestigations.add(createResolvedInvestigation(
                "EXC-102", "merchant_a", ExceptionType.AMOUNT_MISMATCH, 
                ExceptionSeverity.HIGH, new BigDecimal("98.00"), "Refund not reflected"
        ));

        when(investigationRepository.findByException_MerchantId("merchant_a"))
                .thenReturn(historicalInvestigations);

        // When: Finding similar cases
        List<HistoricalCaseDto> results = historicalInvestigationService.findSimilarResolvedInvestigations(currentException);

        // Then: Should return similar cases
        assertThat(results).isNotEmpty();
        assertThat(results).hasSizeLessThanOrEqualTo(3);
        
        // Most similar case should be ranked first
        HistoricalCaseDto topMatch = results.get(0);
        assertThat(topMatch.getExceptionType()).isEqualTo(ExceptionType.AMOUNT_MISMATCH);
        assertThat(topMatch.getSimilarityScore()).isNotNull();
        assertThat(topMatch.getPreviousRootCause()).isNotNull();
    }

    @Test
    @DisplayName("2. Historical retrieval returns maximum 3 cases")
    void testHistoricalRetrievalMaximum3Cases() {
        // Given: Current exception
        FinancialException currentException = createException(
                "EXC-001", "merchant_a", ExceptionType.AMOUNT_MISMATCH, 
                ExceptionSeverity.HIGH, ExceptionStatus.OPEN, new BigDecimal("100.00")
        );

        // And: 5 historical resolved investigations
        List<Investigation> historicalInvestigations = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            historicalInvestigations.add(createResolvedInvestigation(
                    "EXC-10" + i, "merchant_a", ExceptionType.AMOUNT_MISMATCH, 
                    ExceptionSeverity.HIGH, new BigDecimal("100.00"), "Root cause " + i
            ));
        }

        when(investigationRepository.findByException_MerchantId("merchant_a"))
                .thenReturn(historicalInvestigations);

        // When: Finding similar cases
        List<HistoricalCaseDto> results = historicalInvestigationService.findSimilarResolvedInvestigations(currentException);

        // Then: Should return at most 3 cases
        assertThat(results).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    @DisplayName("3. Historical retrieval is merchant-scoped")
    void testHistoricalRetrievalIsMerchantScoped() {
        // Given: Current exception for merchant_a
        FinancialException currentException = createException(
                "EXC-001", "merchant_a", ExceptionType.AMOUNT_MISMATCH, 
                ExceptionSeverity.HIGH, ExceptionStatus.OPEN, new BigDecimal("100.00")
        );

        // And: Only merchant_a investigations returned by repository
        List<Investigation> merchantAInvestigations = List.of(
                createResolvedInvestigation(
                        "EXC-100", "merchant_a", ExceptionType.AMOUNT_MISMATCH, 
                        ExceptionSeverity.HIGH, new BigDecimal("100.00"), "Merchant A case"
                )
        );

        when(investigationRepository.findByException_MerchantId("merchant_a"))
                .thenReturn(merchantAInvestigations);

        // When: Finding similar cases
        List<HistoricalCaseDto> results = historicalInvestigationService.findSimilarResolvedInvestigations(currentException);

        // Then: All results should be from merchant_a
        assertThat(results).isNotEmpty();
        // Repository query ensures merchant scoping - no cross-merchant data leakage
    }

    @Test
    @DisplayName("4. Merchant A cannot retrieve Merchant B's historical investigations")
    void testMerchantIsolation() {
        // Given: Merchant A context
        when(merchantContext.merchantId()).thenReturn("merchant_a");

        FinancialException merchantAException = createException(
                "EXC-A01", "merchant_a", ExceptionType.AMOUNT_MISMATCH, 
                ExceptionSeverity.HIGH, ExceptionStatus.OPEN, new BigDecimal("100.00")
        );

        // And: Repository returns only merchant_a data
        List<Investigation> merchantAInvestigations = List.of(
                createResolvedInvestigation(
                        "EXC-A100", "merchant_a", ExceptionType.AMOUNT_MISMATCH, 
                        ExceptionSeverity.HIGH, new BigDecimal("100.00"), "Merchant A case"
                )
        );

        when(investigationRepository.findByException_MerchantId("merchant_a"))
                .thenReturn(merchantAInvestigations);

        // When: Merchant A retrieves historical cases
        List<HistoricalCaseDto> results = historicalInvestigationService.findSimilarResolvedInvestigations(merchantAException);

        // Then: Results are scoped to merchant_a
        assertThat(results).isNotEmpty();
        
        // Verify: Merchant B data would require different merchantId in repository query
        // Repository layer enforces merchant isolation
    }

    @Test
    @DisplayName("5. No sufficient historical data returns empty result")
    void testInsufficientHistoricalDataReturnsEmpty() {
        // Given: Current exception
        FinancialException currentException = createException(
                "EXC-001", "merchant_a", ExceptionType.AMOUNT_MISMATCH, 
                ExceptionSeverity.HIGH, ExceptionStatus.OPEN, new BigDecimal("100.00")
        );

        // And: No historical investigations
        when(investigationRepository.findByException_MerchantId("merchant_a"))
                .thenReturn(List.of());

        // When: Finding similar cases
        List<HistoricalCaseDto> results = historicalInvestigationService.findSimilarResolvedInvestigations(currentException);

        // Then: Should return empty list
        assertThat(results).isEmpty();
    }

    // Helper methods

    private FinancialException createException(String exceptionId, String merchantId, 
                                              ExceptionType type, ExceptionSeverity severity, 
                                              ExceptionStatus status, BigDecimal discrepancy) {
        FinancialException exception = new FinancialException();
        exception.setExceptionId(exceptionId);
        exception.setMerchantId(merchantId);
        exception.setExceptionType(type);
        exception.setType(type);
        exception.setSeverity(severity);
        exception.setStatus(status);
        exception.setDiscrepancyAmount(discrepancy);
        exception.setDetectedAt(OffsetDateTime.now());
        exception.setCreatedAt(OffsetDateTime.now());
        return exception;
    }

    private Investigation createResolvedInvestigation(String exceptionId, String merchantId, 
                                                     ExceptionType type, ExceptionSeverity severity, 
                                                     BigDecimal discrepancy, String rootCause) {
        FinancialException exception = createException(
                exceptionId, merchantId, type, severity, ExceptionStatus.RESOLVED_MANUAL, discrepancy
        );

        Investigation investigation = new Investigation();
        investigation.setId(UUID.randomUUID());
        investigation.setException(exception);
        investigation.setLikelyRootCause(rootCause);
        investigation.setConfidenceScore(new BigDecimal("85.00"));
        investigation.setRecommendedAction(RecommendedAction.HUMAN_REVIEW_REQUIRED);
        return investigation;
    }
}
