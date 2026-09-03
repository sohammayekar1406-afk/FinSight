package com.ledgerlens.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Phase 3 Forensic Reasoning: Represents a competing hypothesis for root cause analysis
 */
public class HypothesisDto {

    private String hypothesis;
    private BigDecimal confidence;
    private List<String> supportingEvidence;
    private List<String> contradictingEvidence;
    private HypothesisStatus status;

    public enum HypothesisStatus {
        SUPPORTED,
        WEAKENED,
        CONTRADICTED,
        UNRESOLVED
    }

    public HypothesisDto() {}

    public HypothesisDto(String hypothesis, BigDecimal confidence, List<String> supportingEvidence, 
                        List<String> contradictingEvidence, HypothesisStatus status) {
        this.hypothesis = hypothesis;
        this.confidence = confidence;
        this.supportingEvidence = supportingEvidence;
        this.contradictingEvidence = contradictingEvidence;
        this.status = status;
    }

    public static HypothesisDtoBuilder builder() { return new HypothesisDtoBuilder(); }

    public String getHypothesis() { return hypothesis; }
    public void setHypothesis(String hypothesis) { this.hypothesis = hypothesis; }

    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }

    public List<String> getSupportingEvidence() { return supportingEvidence; }
    public void setSupportingEvidence(List<String> supportingEvidence) { 
        this.supportingEvidence = supportingEvidence; 
    }

    public List<String> getContradictingEvidence() { return contradictingEvidence; }
    public void setContradictingEvidence(List<String> contradictingEvidence) { 
        this.contradictingEvidence = contradictingEvidence; 
    }

    public HypothesisStatus getStatus() { return status; }
    public void setStatus(HypothesisStatus status) { this.status = status; }

    public static class HypothesisDtoBuilder {
        private String hypothesis;
        private BigDecimal confidence;
        private List<String> supportingEvidence;
        private List<String> contradictingEvidence;
        private HypothesisStatus status;

        public HypothesisDtoBuilder hypothesis(String hypothesis) { 
            this.hypothesis = hypothesis; 
            return this; 
        }
        public HypothesisDtoBuilder confidence(BigDecimal confidence) { 
            this.confidence = confidence; 
            return this; 
        }
        public HypothesisDtoBuilder supportingEvidence(List<String> supportingEvidence) { 
            this.supportingEvidence = supportingEvidence; 
            return this; 
        }
        public HypothesisDtoBuilder contradictingEvidence(List<String> contradictingEvidence) { 
            this.contradictingEvidence = contradictingEvidence; 
            return this; 
        }
        public HypothesisDto.HypothesisDtoBuilder status(HypothesisStatus status) { 
            this.status = status; 
            return this; 
        }

        public HypothesisDto build() {
            return new HypothesisDto(hypothesis, confidence, supportingEvidence, 
                    contradictingEvidence, status);
        }
    }
}
