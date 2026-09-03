package com.ledgerlens.service;

import com.ledgerlens.dto.HistoricalCaseDto;
import com.ledgerlens.entity.FinancialException;
import com.ledgerlens.entity.Investigation;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.repository.InvestigationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Phase 3: Historical Investigation Retrieval Service
 * 
 * Provides merchant-scoped historical investigation retrieval with deterministic similarity scoring.
 * Returns at most 3 similar resolved investigations from the same merchant.
 * 
 * Similarity is based on explainable signals:
 * - Exception type (exact match = high score)
 * - Severity match
 * - Amount mismatch magnitude similarity
 * 
 * SECURITY: All queries are merchant-scoped using MerchantContext
 */
@Service
public class HistoricalInvestigationService {

    private static final int MAX_HISTORICAL_RESULTS = 3;
    private static final BigDecimal SIMILARITY_THRESHOLD = new BigDecimal("0.30"); // 30% minimum similarity

    private final InvestigationRepository investigationRepository;
    private final MerchantContext merchantContext;

    public HistoricalInvestigationService(InvestigationRepository investigationRepository, 
                                         MerchantContext merchantContext) {
        this.investigationRepository = investigationRepository;
        this.merchantContext = merchantContext;
    }

    /**
     * Retrieves similar resolved investigations from the same merchant.
     * 
     * @param currentException The current exception being investigated
     * @return List of at most 3 similar historical cases (empty if none found)
     */
    public List<HistoricalCaseDto> findSimilarResolvedInvestigations(FinancialException currentException) {
        if (currentException == null) {
            return List.of();
        }

        String merchantId = merchantContext.merchantId();
        
        // Fetch all investigations for this merchant (merchant-scoped query)
        List<Investigation> allMerchantInvestigations = investigationRepository
                .findByException_MerchantId(merchantId);

        // Filter for resolved cases only and exclude current exception
        List<Investigation> resolvedInvestigations = allMerchantInvestigations.stream()
                .filter(inv -> inv.getException() != null)
                .filter(inv -> !inv.getException().getExceptionId().equals(currentException.getExceptionId()))
                .filter(inv -> isResolved(inv.getException()))
                .collect(Collectors.toList());

        if (resolvedInvestigations.isEmpty()) {
            return List.of();
        }

        // Calculate similarity scores
        List<ScoredInvestigation> scoredInvestigations = resolvedInvestigations.stream()
                .map(inv -> new ScoredInvestigation(
                        inv, 
                        calculateSimilarityScore(currentException, inv.getException())
                ))
                .filter(scored -> scored.score.compareTo(SIMILARITY_THRESHOLD) >= 0)
                .sorted((a, b) -> b.score.compareTo(a.score)) // Descending order
                .limit(MAX_HISTORICAL_RESULTS)
                .collect(Collectors.toList());

        // Convert to DTOs
        return scoredInvestigations.stream()
                .map(scored -> mapToHistoricalCaseDto(scored.investigation, scored.score))
                .collect(Collectors.toList());
    }

    /**
     * Deterministic similarity scoring based on explainable financial signals.
     * 
     * Scoring factors:
     * - Exception type match: 40%
     * - Severity match: 30%
     * - Amount magnitude similarity: 30%
     */
    private BigDecimal calculateSimilarityScore(FinancialException current, FinancialException historical) {
        BigDecimal score = BigDecimal.ZERO;

        // Factor 1: Exception Type Match (40%)
        if (current.getExceptionType() == historical.getExceptionType()) {
            score = score.add(new BigDecimal("0.40"));
        }

        // Factor 2: Severity Match (30%)
        if (current.getSeverity() == historical.getSeverity()) {
            score = score.add(new BigDecimal("0.30"));
        }

        // Factor 3: Amount Magnitude Similarity (30%)
        BigDecimal amountSimilarity = calculateAmountSimilarity(
                current.getDiscrepancyAmount(), 
                historical.getDiscrepancyAmount()
        );
        score = score.add(amountSimilarity.multiply(new BigDecimal("0.30")));

        return score.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates similarity between two amounts (0.0 to 1.0 scale).
     * Uses relative difference: 1 - (|diff| / max)
     */
    private BigDecimal calculateAmountSimilarity(BigDecimal amount1, BigDecimal amount2) {
        if (amount1 == null || amount2 == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal abs1 = amount1.abs();
        BigDecimal abs2 = amount2.abs();

        if (abs1.compareTo(BigDecimal.ZERO) == 0 && abs2.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }

        BigDecimal maxAmount = abs1.max(abs2);
        if (maxAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }

        BigDecimal diff = abs1.subtract(abs2).abs();
        BigDecimal relativeDiff = diff.divide(maxAmount, 4, RoundingMode.HALF_UP);
        
        BigDecimal similarity = BigDecimal.ONE.subtract(relativeDiff);
        return similarity.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isResolved(FinancialException exception) {
        ExceptionStatus status = exception.getStatus();
        return status == ExceptionStatus.RESOLVED_AUTO 
            || status == ExceptionStatus.RESOLVED_MANUAL;
    }

    private HistoricalCaseDto mapToHistoricalCaseDto(Investigation inv, BigDecimal score) {
        String relevantEvidence = buildRelevantEvidence(inv);

        return HistoricalCaseDto.builder()
                .investigationId(inv.getId().toString())
                .exceptionType(inv.getException().getExceptionType())
                .similarityScore(score)
                .previousRootCause(inv.getLikelyRootCause())
                .previousResolution(inv.getRecommendedAction())
                .relevantEvidence(relevantEvidence)
                .build();
    }

    private String buildRelevantEvidence(Investigation inv) {
        FinancialException ex = inv.getException();
        StringBuilder evidence = new StringBuilder();

        evidence.append("Type: ").append(ex.getExceptionType())
                .append(", Severity: ").append(ex.getSeverity());

        if (ex.getDiscrepancyAmount() != null) {
            evidence.append(", Discrepancy: ₹").append(ex.getDiscrepancyAmount());
        }

        return evidence.toString();
    }

    /**
     * Internal class for scored investigations during similarity ranking
     */
    private static class ScoredInvestigation {
        final Investigation investigation;
        final BigDecimal score;

        ScoredInvestigation(Investigation investigation, BigDecimal score) {
            this.investigation = investigation;
            this.score = score;
        }
    }
}
