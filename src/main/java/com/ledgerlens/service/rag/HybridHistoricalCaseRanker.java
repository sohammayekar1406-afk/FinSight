package com.ledgerlens.service.rag;

import com.ledgerlens.dto.RagHistoricalCaseDto;
import com.ledgerlens.entity.FinancialException;
import com.ledgerlens.entity.HistoricalInvestigationEmbedding;
import com.ledgerlens.entity.Investigation;
import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Phase 6.4: Hybrid Historical Case Ranker
 * 
 * Blends semantic similarity with deterministic financial metadata signals:
 * - Semantic similarity: 50%
 * - Exception type match: 25%
 * - Severity match: 15%
 * - Amount magnitude closeness: 10%
 * 
 * All weights are named constants and configurable.
 */
@Component
public class HybridHistoricalCaseRanker {

    public static final BigDecimal DEFAULT_SEMANTIC_WEIGHT = new BigDecimal("0.50");
    public static final BigDecimal DEFAULT_EXCEPTION_TYPE_WEIGHT = new BigDecimal("0.25");
    public static final BigDecimal DEFAULT_SEVERITY_WEIGHT = new BigDecimal("0.15");
    public static final BigDecimal DEFAULT_AMOUNT_WEIGHT = new BigDecimal("0.10");

    private final BigDecimal semanticWeight;
    private final BigDecimal exceptionTypeWeight;
    private final BigDecimal severityWeight;
    private final BigDecimal amountWeight;

    public HybridHistoricalCaseRanker() {
        this(DEFAULT_SEMANTIC_WEIGHT, DEFAULT_EXCEPTION_TYPE_WEIGHT, DEFAULT_SEVERITY_WEIGHT, DEFAULT_AMOUNT_WEIGHT);
    }

    public HybridHistoricalCaseRanker(BigDecimal semanticWeight, 
                                     BigDecimal exceptionTypeWeight, 
                                     BigDecimal severityWeight, 
                                     BigDecimal amountWeight) {
        this.semanticWeight = semanticWeight != null ? semanticWeight : DEFAULT_SEMANTIC_WEIGHT;
        this.exceptionTypeWeight = exceptionTypeWeight != null ? exceptionTypeWeight : DEFAULT_EXCEPTION_TYPE_WEIGHT;
        this.severityWeight = severityWeight != null ? severityWeight : DEFAULT_SEVERITY_WEIGHT;
        this.amountWeight = amountWeight != null ? amountWeight : DEFAULT_AMOUNT_WEIGHT;
    }

    public List<RagHistoricalCaseDto> rankCandidates(
            FinancialException currentException,
            List<CandidateWithSimilarity> candidates,
            int limit) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        List<RagHistoricalCaseDto> rankedList = new ArrayList<>();

        for (CandidateWithSimilarity candidate : candidates) {
            HistoricalInvestigationEmbedding embeddingEntity = candidate.embeddingEntity();
            Investigation inv = embeddingEntity.getInvestigation();
            FinancialException histEx = inv.getException();

            BigDecimal semanticScore = candidate.similarityScore();
            BigDecimal typeScore = (currentException.getExceptionType() == histEx.getExceptionType()) ? BigDecimal.ONE : BigDecimal.ZERO;
            BigDecimal severityScore = (currentException.getSeverity() == histEx.getSeverity()) ? BigDecimal.ONE : BigDecimal.ZERO;
            BigDecimal amountScore = calculateAmountSimilarity(currentException.getDiscrepancyAmount(), histEx.getDiscrepancyAmount());

            BigDecimal blendedScore = semanticScore.multiply(semanticWeight)
                    .add(typeScore.multiply(exceptionTypeWeight))
                    .add(severityScore.multiply(severityWeight))
                    .add(amountScore.multiply(amountWeight))
                    .setScale(4, RoundingMode.HALF_UP);

            String breakdown = String.format("Semantic(%.0f%%)=%.2f, Type(%.0f%%)=%.2f, Sev(%.0f%%)=%.2f, Amt(%.0f%%)=%.2f",
                    semanticWeight.doubleValue() * 100, semanticScore.doubleValue(),
                    exceptionTypeWeight.doubleValue() * 100, typeScore.doubleValue(),
                    severityWeight.doubleValue() * 100, severityScore.doubleValue(),
                    amountWeight.doubleValue() * 100, amountScore.doubleValue());

            String relevantEvidence = String.format("Type: %s, Severity: %s%s",
                    histEx.getExceptionType(),
                    histEx.getSeverity(),
                    histEx.getDiscrepancyAmount() != null ? ", Discrepancy: ₹" + histEx.getDiscrepancyAmount() : "");

            RagHistoricalCaseDto dto = RagHistoricalCaseDto.builder()
                    .investigationId(inv.getId().toString())
                    .exceptionId(histEx.getExceptionId())
                    .merchantId(embeddingEntity.getMerchantId())
                    .exceptionType(histEx.getExceptionType())
                    .severity(histEx.getSeverity())
                    .discrepancyAmount(histEx.getDiscrepancyAmount())
                    .previousRootCause(inv.getLikelyRootCause())
                    .previousResolution(inv.getRecommendedAction())
                    .relevantEvidence(relevantEvidence)
                    .semanticSimilarityScore(semanticScore.setScale(2, RoundingMode.HALF_UP))
                    .blendedScore(blendedScore.setScale(2, RoundingMode.HALF_UP))
                    .rankingBreakdown(breakdown)
                    .sourceText(embeddingEntity.getSourceText())
                    .build();

            rankedList.add(dto);
        }

        return rankedList.stream()
                .sorted(Comparator.comparing(RagHistoricalCaseDto::getBlendedScore).reversed())
                .limit(limit > 0 ? limit : 5)
                .collect(Collectors.toList());
    }

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

        return similarity.max(BigDecimal.ZERO).setScale(4, RoundingMode.HALF_UP);
    }

    public record CandidateWithSimilarity(HistoricalInvestigationEmbedding embeddingEntity, BigDecimal similarityScore) {}
}
