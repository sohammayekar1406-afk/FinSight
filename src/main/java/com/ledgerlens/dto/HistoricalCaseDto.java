package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.entity.enums.RecommendedAction;

import java.math.BigDecimal;

public class HistoricalCaseDto {

    private String investigationId;
    private ExceptionType exceptionType;
    private BigDecimal similarityScore;
    private String previousRootCause;
    private RecommendedAction previousResolution;
    private String relevantEvidence;

    public HistoricalCaseDto() {}

    public HistoricalCaseDto(String investigationId, ExceptionType exceptionType, BigDecimal similarityScore, 
                            String previousRootCause, RecommendedAction previousResolution, String relevantEvidence) {
        this.investigationId = investigationId;
        this.exceptionType = exceptionType;
        this.similarityScore = similarityScore;
        this.previousRootCause = previousRootCause;
        this.previousResolution = previousResolution;
        this.relevantEvidence = relevantEvidence;
    }

    public static HistoricalCaseDtoBuilder builder() { return new HistoricalCaseDtoBuilder(); }

    public String getInvestigationId() { return investigationId; }
    public void setInvestigationId(String investigationId) { this.investigationId = investigationId; }

    public ExceptionType getExceptionType() { return exceptionType; }
    public void setExceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; }

    public BigDecimal getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(BigDecimal similarityScore) { this.similarityScore = similarityScore; }

    public String getPreviousRootCause() { return previousRootCause; }
    public void setPreviousRootCause(String previousRootCause) { this.previousRootCause = previousRootCause; }

    public RecommendedAction getPreviousResolution() { return previousResolution; }
    public void setPreviousResolution(RecommendedAction previousResolution) { this.previousResolution = previousResolution; }

    public String getRelevantEvidence() { return relevantEvidence; }
    public void setRelevantEvidence(String relevantEvidence) { this.relevantEvidence = relevantEvidence; }

    public static class HistoricalCaseDtoBuilder {
        private String investigationId;
        private ExceptionType exceptionType;
        private BigDecimal similarityScore;
        private String previousRootCause;
        private RecommendedAction previousResolution;
        private String relevantEvidence;

        public HistoricalCaseDtoBuilder investigationId(String investigationId) { 
            this.investigationId = investigationId; 
            return this; 
        }
        public HistoricalCaseDtoBuilder exceptionType(ExceptionType exceptionType) { 
            this.exceptionType = exceptionType; 
            return this; 
        }
        public HistoricalCaseDtoBuilder similarityScore(BigDecimal similarityScore) { 
            this.similarityScore = similarityScore; 
            return this; 
        }
        public HistoricalCaseDtoBuilder previousRootCause(String previousRootCause) { 
            this.previousRootCause = previousRootCause; 
            return this; 
        }
        public HistoricalCaseDtoBuilder previousResolution(RecommendedAction previousResolution) { 
            this.previousResolution = previousResolution; 
            return this; 
        }
        public HistoricalCaseDtoBuilder relevantEvidence(String relevantEvidence) { 
            this.relevantEvidence = relevantEvidence; 
            return this; 
        }

        public HistoricalCaseDto build() {
            return new HistoricalCaseDto(investigationId, exceptionType, similarityScore, 
                    previousRootCause, previousResolution, relevantEvidence);
        }
    }
}
