package com.ledgerlens;

import com.ledgerlens.dto.SoftAnomalyDto;
import com.ledgerlens.entity.FinancialException;
import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.repository.FinancialExceptionRepository;
import com.ledgerlens.service.FinancialAnomalyService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
        "ai.enabled=false"
})
@DisplayName("Phase 3: Anomaly Detection Tests")
class Phase3AnomalyDetectionTest {

    @Autowired
    private FinancialAnomalyService anomalyService;

    @MockBean
    private FinancialExceptionRepository exceptionRepository;

    @MockBean
    private MerchantContext merchantContext;

    @BeforeEach
    void setUp() {
        when(merchantContext.merchantId()).thenReturn("merchant_a");
    }

    @Test
    @DisplayName("6. Anomaly detector calculates recent mismatch rate vs baseline deterministically")
    void testAnomalyDetectionCalculation() {
        // Given: Current exception
        FinancialException currentException = createException("EXC-001", "merchant_a");

        // And: Historical exceptions showing increased recent rate
        List<FinancialException> allExceptions = new ArrayList<>();
        
        // Historical baseline: 2 exceptions over 83 days (90-7)
        allExceptions.add(createExceptionAt("EXC-H01", "merchant_a", OffsetDateTime.now().minusDays(85)));
        allExceptions.add(createExceptionAt("EXC-H02", "merchant_a", OffsetDateTime.now().minusDays(84)));
        
        // Recent spike: 8 exceptions in last 7 days
        for (int i = 1; i <= 8; i++) {
            allExceptions.add(createExceptionAt("EXC-R0" + i, "merchant_a", OffsetDateTime.now().minusDays(i)));
        }

        when(exceptionRepository.findByMerchantId("merchant_a")).thenReturn(allExceptions);

        // When: Detecting anomalies
        List<SoftAnomalyDto> anomalies = anomalyService.detectAnomalies(currentException);

        // Then: Should detect anomaly
        assertThat(anomalies).isNotEmpty();
        
        SoftAnomalyDto anomaly = anomalies.get(0);
        assertThat(anomaly.getMetric()).isEqualTo("exception_rate");
        assertThat(anomaly.isAnomalyDetected()).isTrue();
        assertThat(anomaly.getBaseline()).isNotNull();
        assertThat(anomaly.getCurrentValue()).isNotNull();
        assertThat(anomaly.getDeviation()).isGreaterThan(BigDecimal.ONE);
        assertThat(anomaly.getExplanation()).contains("Anomaly detected");
    }

    @Test
    @DisplayName("7. Insufficient data does not fabricate an anomaly")
    void testInsufficientDataHandling() {
        // Given: Current exception
        FinancialException currentException = createException("EXC-001", "merchant_a");

        // And: Only 5 exceptions (below minimum threshold of 10)
        List<FinancialException> fewExceptions = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            fewExceptions.add(createExceptionAt("EXC-0" + i, "merchant_a", OffsetDateTime.now().minusDays(i)));
        }

        when(exceptionRepository.findByMerchantId("merchant_a")).thenReturn(fewExceptions);

        // When: Detecting anomalies
        List<SoftAnomalyDto> anomalies = anomalyService.detectAnomalies(currentException);

        // Then: Should return insufficient data indicator
        assertThat(anomalies).isNotEmpty();
        
        SoftAnomalyDto anomaly = anomalies.get(0);
        assertThat(anomaly.isAnomalyDetected()).isFalse();
        assertThat(anomaly.getExplanation()).contains("Insufficient historical data");
        assertThat(anomaly.getBaseline()).isEqualTo(BigDecimal.ZERO);
        assertThat(anomaly.getCurrentValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Anomaly detection is merchant-scoped")
    void testAnomalyDetectionMerchantScoped() {
        // Given: Current exception for merchant_a
        FinancialException currentException = createException("EXC-A01", "merchant_a");

        // And: Sufficient merchant_a exceptions
        List<FinancialException> merchantAExceptions = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            merchantAExceptions.add(createExceptionAt("EXC-A" + i, "merchant_a", OffsetDateTime.now().minusDays(i * 5)));
        }

        when(exceptionRepository.findByMerchantId("merchant_a")).thenReturn(merchantAExceptions);

        // When: Detecting anomalies
        List<SoftAnomalyDto> anomalies = anomalyService.detectAnomalies(currentException);

        // Then: Results should be based only on merchant_a data
        assertThat(anomalies).isNotEmpty();
        // Repository query ensures merchant scoping
    }

    // Helper methods

    private FinancialException createException(String exceptionId, String merchantId) {
        return createExceptionAt(exceptionId, merchantId, OffsetDateTime.now());
    }

    private FinancialException createExceptionAt(String exceptionId, String merchantId, OffsetDateTime detectedAt) {
        FinancialException exception = new FinancialException();
        exception.setExceptionId(exceptionId);
        exception.setMerchantId(merchantId);
        exception.setExceptionType(ExceptionType.AMOUNT_MISMATCH);
        exception.setType(ExceptionType.AMOUNT_MISMATCH);
        exception.setSeverity(ExceptionSeverity.HIGH);
        exception.setStatus(ExceptionStatus.OPEN);
        exception.setDiscrepancyAmount(new BigDecimal("100.00"));
        exception.setDetectedAt(detectedAt);
        exception.setCreatedAt(detectedAt);
        return exception;
    }
}
