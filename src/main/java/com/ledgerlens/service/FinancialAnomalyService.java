package com.ledgerlens.service;

import com.ledgerlens.dto.SoftAnomalyDto;
import com.ledgerlens.entity.FinancialException;
import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.repository.FinancialExceptionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase 3: Deterministic Soft Anomaly / Trend Detection Service
 * 
 * Provides deterministic, explainable financial anomaly detection.
 * This is NOT a replacement for deterministic reconciliation rules.
 * It identifies broader trends that individual transaction rules may miss.
 * 
 * Current implementation: RECENT_MISMATCH_RATE vs HISTORICAL_BASELINE
 * 
 * SECURITY: All calculations are merchant-scoped using MerchantContext
 */
@Service
public class FinancialAnomalyService {

    private static final int RECENT_DAYS_WINDOW = 7;  // Recent trend window
    private static final int HISTORICAL_DAYS_WINDOW = 90;  // Historical baseline window
    private static final BigDecimal ANOMALY_THRESHOLD = new BigDecimal("2.0"); // 2x deviation threshold

    private final FinancialExceptionRepository exceptionRepository;
    private final MerchantContext merchantContext;

    public FinancialAnomalyService(FinancialExceptionRepository exceptionRepository, 
                                  MerchantContext merchantContext) {
        this.exceptionRepository = exceptionRepository;
        this.merchantContext = merchantContext;
    }

    /**
     * Detects soft anomalies for the current exception context.
     * 
     * Calculates:
     * - Historical mismatch rate (last 90 days, excluding recent 7 days)
     * - Recent mismatch rate (last 7 days)
     * - Deviation factor
     * 
     * @param currentException The current exception being investigated
     * @return List of detected anomalies (empty if insufficient data or no anomaly)
     */
    public List<SoftAnomalyDto> detectAnomalies(FinancialException currentException) {
        List<SoftAnomalyDto> anomalies = new ArrayList<>();

        String merchantId = merchantContext.merchantId();
        
        // Fetch all merchant exceptions (merchant-scoped)
        List<FinancialException> allExceptions = exceptionRepository.findByMerchantId(merchantId);

        if (allExceptions.size() < 10) {
            // Insufficient data for meaningful anomaly detection
            anomalies.add(buildInsufficientDataAnomaly());
            return anomalies;
        }

        // Calculate time windows
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime recentStart = now.minusDays(RECENT_DAYS_WINDOW);
        OffsetDateTime historicalStart = now.minusDays(HISTORICAL_DAYS_WINDOW);
        OffsetDateTime historicalEnd = recentStart; // Historical ends where recent begins

        // Count exceptions in each window
        long historicalCount = allExceptions.stream()
                .filter(ex -> ex.getDetectedAt().isAfter(historicalStart) 
                           && ex.getDetectedAt().isBefore(historicalEnd))
                .count();

        long recentCount = allExceptions.stream()
                .filter(ex -> ex.getDetectedAt().isAfter(recentStart))
                .count();

        if (historicalCount == 0) {
            // No historical baseline available
            anomalies.add(buildInsufficientDataAnomaly());
            return anomalies;
        }

        // Calculate mismatch rates (exceptions per day)
        BigDecimal historicalDays = new BigDecimal(HISTORICAL_DAYS_WINDOW - RECENT_DAYS_WINDOW);
        BigDecimal recentDays = new BigDecimal(RECENT_DAYS_WINDOW);

        BigDecimal historicalRate = new BigDecimal(historicalCount)
                .divide(historicalDays, 4, RoundingMode.HALF_UP);
        BigDecimal recentRate = new BigDecimal(recentCount)
                .divide(recentDays, 4, RoundingMode.HALF_UP);

        // Calculate deviation
        BigDecimal deviation = BigDecimal.ZERO;
        if (historicalRate.compareTo(BigDecimal.ZERO) > 0) {
            deviation = recentRate.divide(historicalRate, 2, RoundingMode.HALF_UP);
        }

        boolean anomalyDetected = deviation.compareTo(ANOMALY_THRESHOLD) >= 0;

        String explanation = buildExplanation(
                historicalRate, recentRate, deviation, anomalyDetected, 
                historicalCount, recentCount
        );

        SoftAnomalyDto anomaly = SoftAnomalyDto.builder()
                .anomalyDetected(anomalyDetected)
                .metric("exception_rate")
                .baseline(historicalRate)
                .currentValue(recentRate)
                .deviation(deviation)
                .explanation(explanation)
                .build();

        anomalies.add(anomaly);
        return anomalies;
    }

    private String buildExplanation(BigDecimal historicalRate, BigDecimal recentRate, 
                                   BigDecimal deviation, boolean anomalyDetected,
                                   long historicalCount, long recentCount) {
        StringBuilder explanation = new StringBuilder();

        explanation.append("Historical baseline: ")
                .append(historicalRate.setScale(2, RoundingMode.HALF_UP))
                .append(" exceptions/day over ")
                .append(HISTORICAL_DAYS_WINDOW - RECENT_DAYS_WINDOW)
                .append(" days (")
                .append(historicalCount)
                .append(" total). ");

        explanation.append("Recent rate: ")
                .append(recentRate.setScale(2, RoundingMode.HALF_UP))
                .append(" exceptions/day over last ")
                .append(RECENT_DAYS_WINDOW)
                .append(" days (")
                .append(recentCount)
                .append(" total). ");

        if (anomalyDetected) {
            explanation.append("⚠ Anomaly detected: Recent exception rate is approximately ")
                    .append(deviation)
                    .append("x the historical baseline.");
        } else {
            explanation.append("✓ No significant anomaly detected (")
                    .append(deviation)
                    .append("x deviation within normal range).");
        }

        return explanation.toString();
    }

    private SoftAnomalyDto buildInsufficientDataAnomaly() {
        return SoftAnomalyDto.builder()
                .anomalyDetected(false)
                .metric("exception_rate")
                .baseline(BigDecimal.ZERO)
                .currentValue(BigDecimal.ZERO)
                .deviation(BigDecimal.ZERO)
                .explanation("Insufficient historical data for anomaly detection. " +
                           "At least 10 exceptions required across " + HISTORICAL_DAYS_WINDOW + " days.")
                .build();
    }
}
