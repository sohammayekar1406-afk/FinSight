package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.entity.enums.RecommendedAction;

import java.math.BigDecimal;

public class RagHistoricalCaseDto {

    private String investigationId;
    private String exceptionId;
    private String merchantId;
    private ExceptionType exceptionType;
    private ExceptionSeverity severity;
    private BigDecimal discrepancyAmount;
    private String previousRootCause;
    private RecommendedAction previousResolution;
    private String relevantEvidence;
    private BigDecimal semanticSimilarityScore;
    private BigDecimal blendedScore;
    private String rankingBreakdown;
    private String sourceText;

    public RagHistoricalCaseDto() {}

    public RagHistoricalCaseDto(String investigationId, String exceptionId, String merchantId, 
                                ExceptionType exceptionType, ExceptionSeverity severity, 
                                BigDecimal discrepancyAmount, String previousRootCause, 
                                RecommendedAction previousResolution, String relevantEvidence, 
                                BigDecimal semanticSimilarityScore, BigDecimal blendedScore, 
                                String rankingBreakdown, String sourceText) {
        this.investigationId = investigationId;
        this.exceptionId = exceptionId;
        this.merchantId = merchantId;
        this.exceptionType = exceptionType;
        this.severity = severity;
        this.discrepancyAmount = discrepancyAmount;
        this.previousRootCause = previousRootCause;
        this.previousResolution = previousResolution;
        this.relevantEvidence = relevantEvidence;
        this.semanticSimilarityScore = semanticSimilarityScore;
        this.blendedScore = blendedScore;
        this.rankingBreakdown = rankingBreakdown;
        this.sourceText = sourceText;
    }

    public static RagHistoricalCaseDtoBuilder builder() {
        return new RagHistoricalCaseDtoBuilder();
    }

    public String getInvestigationId() { return investigationId; }
    public void setInvestigationId(String investigationId) { this.investigationId = investigationId; }
    public String getExceptionId() { return exceptionId; }
    public void setExceptionId(String exceptionId) { this.exceptionId = exceptionId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public ExceptionType getExceptionType() { return exceptionType; }
    public void setExceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; }
    public ExceptionSeverity getSeverity() { return severity; }
    public void setSeverity(ExceptionSeverity severity) { this.severity = severity; }
    public BigDecimal getDiscrepancyAmount() { return discrepancyAmount; }
    public void setDiscrepancyAmount(BigDecimal discrepancyAmount) { this.discrepancyAmount = discrepancyAmount; }
    public String getPreviousRootCause() { return previousRootCause; }
    public void setPreviousRootCause(String previousRootCause) { this.previousRootCause = previousRootCause; }
    public RecommendedAction getPreviousResolution() { return previousResolution; }
    public void setPreviousResolution(RecommendedAction previousResolution) { this.previousResolution = previousResolution; }
    public String getRelevantEvidence() { return relevantEvidence; }
    public void setRelevantEvidence(String relevantEvidence) { this.relevantEvidence = relevantEvidence; }
    public BigDecimal getSemanticSimilarityScore() { return semanticSimilarityScore; }
    public void setSemanticSimilarityScore(BigDecimal semanticSimilarityScore) { this.semanticSimilarityScore = semanticSimilarityScore; }
    public BigDecimal getBlendedScore() { return blendedScore; }
    public void setBlendedScore(BigDecimal blendedScore) { this.blendedScore = blendedScore; }
    public String getRankingBreakdown() { return rankingBreakdown; }
    public void setRankingBreakdown(String rankingBreakdown) { this.rankingBreakdown = rankingBreakdown; }
    public String getSourceText() { return sourceText; }
    public void setSourceText(String sourceText) { this.sourceText = sourceText; }

    public static class RagHistoricalCaseDtoBuilder {
        private String investigationId;
        private String exceptionId;
        private String merchantId;
        private ExceptionType exceptionType;
        private ExceptionSeverity severity;
        private BigDecimal discrepancyAmount;
        private String previousRootCause;
        private RecommendedAction previousResolution;
        private String relevantEvidence;
        private BigDecimal semanticSimilarityScore;
        private BigDecimal blendedScore;
        private String rankingBreakdown;
        private String sourceText;

        public RagHistoricalCaseDtoBuilder investigationId(String investigationId) { this.investigationId = investigationId; return this; }
        public RagHistoricalCaseDtoBuilder exceptionId(String exceptionId) { this.exceptionId = exceptionId; return this; }
        public RagHistoricalCaseDtoBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public RagHistoricalCaseDtoBuilder exceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; return this; }
        public RagHistoricalCaseDtoBuilder severity(ExceptionSeverity severity) { this.severity = severity; return this; }
        public RagHistoricalCaseDtoBuilder discrepancyAmount(BigDecimal discrepancyAmount) { this.discrepancyAmount = discrepancyAmount; return this; }
        public RagHistoricalCaseDtoBuilder previousRootCause(String previousRootCause) { this.previousRootCause = previousRootCause; return this; }
        public RagHistoricalCaseDtoBuilder previousResolution(RecommendedAction previousResolution) { this.previousResolution = previousResolution; return this; }
        public RagHistoricalCaseDtoBuilder relevantEvidence(String relevantEvidence) { this.relevantEvidence = relevantEvidence; return this; }
        public RagHistoricalCaseDtoBuilder semanticSimilarityScore(BigDecimal semanticSimilarityScore) { this.semanticSimilarityScore = semanticSimilarityScore; return this; }
        public RagHistoricalCaseDtoBuilder blendedScore(BigDecimal blendedScore) { this.blendedScore = blendedScore; return this; }
        public RagHistoricalCaseDtoBuilder rankingBreakdown(String rankingBreakdown) { this.rankingBreakdown = rankingBreakdown; return this; }
        public RagHistoricalCaseDtoBuilder sourceText(String sourceText) { this.sourceText = sourceText; return this; }

        public RagHistoricalCaseDto build() {
            return new RagHistoricalCaseDto(investigationId, exceptionId, merchantId, exceptionType, severity, discrepancyAmount, previousRootCause, previousResolution, relevantEvidence, semanticSimilarityScore, blendedScore, rankingBreakdown, sourceText);
        }
    }
}
