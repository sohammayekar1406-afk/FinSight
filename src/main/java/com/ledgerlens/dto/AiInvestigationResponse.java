package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.ActionTaken;
import com.ledgerlens.entity.enums.RecommendedAction;

import java.math.BigDecimal;
import java.util.List;

public class AiInvestigationResponse {

    private String summary;
    private String likelyRootCause;
    private String supportingEvidence;
    private BigDecimal confidenceScore;
    private RecommendedAction recommendedAction;
    private ActionTaken actionTaken;
    private boolean autoResolved;
    private boolean requiresHumanReview;
    private String missingEvidence;
    
    // Phase 3: AI Investigation Enhancements
    private List<HistoricalCaseDto> similarHistoricalCases;
    private List<SoftAnomalyDto> softAnomalies;
    private String uncertaintyNotes;
    private String limitations;
    
    // Phase 3 Forensic Reasoning: Competing hypotheses and evidence analysis
    private List<HypothesisDto> hypotheses;
    private List<ContradictionDto> contradictions;
    private List<EvidenceRequestDto> additionalEvidenceRequired;
    private List<RelatedExceptionDto> relatedExceptions;

    // Phase 6: Hybrid RAG Historical Cases
    private List<RagHistoricalCaseDto> ragHistoricalCases;

    public AiInvestigationResponse() {}

    public AiInvestigationResponse(String summary, String likelyRootCause, String supportingEvidence, BigDecimal confidenceScore, 
                                  RecommendedAction recommendedAction, ActionTaken actionTaken, boolean autoResolved, 
                                  boolean requiresHumanReview, String missingEvidence, List<HistoricalCaseDto> similarHistoricalCases, 
                                  List<SoftAnomalyDto> softAnomalies, String uncertaintyNotes, String limitations,
                                  List<HypothesisDto> hypotheses, List<ContradictionDto> contradictions,
                                  List<EvidenceRequestDto> additionalEvidenceRequired, List<RelatedExceptionDto> relatedExceptions) {
        this(summary, likelyRootCause, supportingEvidence, confidenceScore, recommendedAction, actionTaken, autoResolved,
             requiresHumanReview, missingEvidence, similarHistoricalCases, softAnomalies, uncertaintyNotes, limitations,
             hypotheses, contradictions, additionalEvidenceRequired, relatedExceptions, List.of());
    }

    public AiInvestigationResponse(String summary, String likelyRootCause, String supportingEvidence, BigDecimal confidenceScore, 
                                  RecommendedAction recommendedAction, ActionTaken actionTaken, boolean autoResolved, 
                                  boolean requiresHumanReview, String missingEvidence, List<HistoricalCaseDto> similarHistoricalCases, 
                                  List<SoftAnomalyDto> softAnomalies, String uncertaintyNotes, String limitations,
                                  List<HypothesisDto> hypotheses, List<ContradictionDto> contradictions,
                                  List<EvidenceRequestDto> additionalEvidenceRequired, List<RelatedExceptionDto> relatedExceptions,
                                  List<RagHistoricalCaseDto> ragHistoricalCases) {
        this.summary = summary;
        this.likelyRootCause = likelyRootCause;
        this.supportingEvidence = supportingEvidence;
        this.confidenceScore = confidenceScore;
        this.recommendedAction = recommendedAction;
        this.actionTaken = actionTaken;
        this.autoResolved = autoResolved;
        this.requiresHumanReview = requiresHumanReview;
        this.missingEvidence = missingEvidence;
        this.similarHistoricalCases = similarHistoricalCases;
        this.softAnomalies = softAnomalies;
        this.uncertaintyNotes = uncertaintyNotes;
        this.limitations = limitations;
        this.hypotheses = hypotheses;
        this.contradictions = contradictions;
        this.additionalEvidenceRequired = additionalEvidenceRequired;
        this.relatedExceptions = relatedExceptions;
        this.ragHistoricalCases = ragHistoricalCases;
    }

    public static AiInvestigationResponseBuilder builder() { return new AiInvestigationResponseBuilder(); }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getLikelyRootCause() { return likelyRootCause; }
    public void setLikelyRootCause(String likelyRootCause) { this.likelyRootCause = likelyRootCause; }

    public String getSupportingEvidence() { return supportingEvidence; }
    public void setSupportingEvidence(String supportingEvidence) { this.supportingEvidence = supportingEvidence; }

    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }

    public RecommendedAction getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(RecommendedAction recommendedAction) { this.recommendedAction = recommendedAction; }

    public ActionTaken getActionTaken() { return actionTaken; }
    public void setActionTaken(ActionTaken actionTaken) { this.actionTaken = actionTaken; }

    public boolean isAutoResolved() { return autoResolved; }
    public void setAutoResolved(boolean autoResolved) { this.autoResolved = autoResolved; }

    public boolean isRequiresHumanReview() { return requiresHumanReview; }
    public void setRequiresHumanReview(boolean requiresHumanReview) { this.requiresHumanReview = requiresHumanReview; }

    public String getMissingEvidence() { return missingEvidence; }
    public void setMissingEvidence(String missingEvidence) { this.missingEvidence = missingEvidence; }

    public List<HistoricalCaseDto> getSimilarHistoricalCases() { return similarHistoricalCases; }
    public void setSimilarHistoricalCases(List<HistoricalCaseDto> similarHistoricalCases) { 
        this.similarHistoricalCases = similarHistoricalCases; 
    }

    public List<SoftAnomalyDto> getSoftAnomalies() { return softAnomalies; }
    public void setSoftAnomalies(List<SoftAnomalyDto> softAnomalies) { this.softAnomalies = softAnomalies; }

    public String getUncertaintyNotes() { return uncertaintyNotes; }
    public void setUncertaintyNotes(String uncertaintyNotes) { this.uncertaintyNotes = uncertaintyNotes; }

    public String getLimitations() { return limitations; }
    public void setLimitations(String limitations) { this.limitations = limitations; }

    public List<HypothesisDto> getHypotheses() { return hypotheses; }
    public void setHypotheses(List<HypothesisDto> hypotheses) { this.hypotheses = hypotheses; }

    public List<ContradictionDto> getContradictions() { return contradictions; }
    public void setContradictions(List<ContradictionDto> contradictions) { this.contradictions = contradictions; }

    public List<EvidenceRequestDto> getAdditionalEvidenceRequired() { return additionalEvidenceRequired; }
    public void setAdditionalEvidenceRequired(List<EvidenceRequestDto> additionalEvidenceRequired) { 
        this.additionalEvidenceRequired = additionalEvidenceRequired; 
    }

    public List<RelatedExceptionDto> getRelatedExceptions() { return relatedExceptions; }
    public void setRelatedExceptions(List<RelatedExceptionDto> relatedExceptions) { 
        this.relatedExceptions = relatedExceptions; 
    }

    public List<RagHistoricalCaseDto> getRagHistoricalCases() { return ragHistoricalCases; }
    public void setRagHistoricalCases(List<RagHistoricalCaseDto> ragHistoricalCases) { 
        this.ragHistoricalCases = ragHistoricalCases; 
    }

    public static class AiInvestigationResponseBuilder {
        private String summary;
        private String likelyRootCause;
        private String supportingEvidence;
        private BigDecimal confidenceScore;
        private RecommendedAction recommendedAction;
        private ActionTaken actionTaken;
        private boolean autoResolved;
        private boolean requiresHumanReview;
        private String missingEvidence;
        private List<HistoricalCaseDto> similarHistoricalCases;
        private List<SoftAnomalyDto> softAnomalies;
        private String uncertaintyNotes;
        private String limitations;
        private List<HypothesisDto> hypotheses;
        private List<ContradictionDto> contradictions;
        private List<EvidenceRequestDto> additionalEvidenceRequired;
        private List<RelatedExceptionDto> relatedExceptions;
        private List<RagHistoricalCaseDto> ragHistoricalCases;

        public AiInvestigationResponseBuilder summary(String summary) { this.summary = summary; return this; }
        public AiInvestigationResponseBuilder likelyRootCause(String likelyRootCause) { this.likelyRootCause = likelyRootCause; return this; }
        public AiInvestigationResponseBuilder supportingEvidence(String supportingEvidence) { this.supportingEvidence = supportingEvidence; return this; }
        public AiInvestigationResponseBuilder confidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public AiInvestigationResponseBuilder recommendedAction(RecommendedAction recommendedAction) { this.recommendedAction = recommendedAction; return this; }
        public AiInvestigationResponseBuilder actionTaken(ActionTaken actionTaken) { this.actionTaken = actionTaken; return this; }
        public AiInvestigationResponseBuilder autoResolved(boolean autoResolved) { this.autoResolved = autoResolved; return this; }
        public AiInvestigationResponseBuilder requiresHumanReview(boolean requiresHumanReview) { this.requiresHumanReview = requiresHumanReview; return this; }
        public AiInvestigationResponseBuilder missingEvidence(String missingEvidence) { this.missingEvidence = missingEvidence; return this; }
        public AiInvestigationResponseBuilder similarHistoricalCases(List<HistoricalCaseDto> similarHistoricalCases) { 
            this.similarHistoricalCases = similarHistoricalCases; 
            return this; 
        }
        public AiInvestigationResponseBuilder softAnomalies(List<SoftAnomalyDto> softAnomalies) { 
            this.softAnomalies = softAnomalies; 
            return this; 
        }
        public AiInvestigationResponseBuilder uncertaintyNotes(String uncertaintyNotes) { 
            this.uncertaintyNotes = uncertaintyNotes; 
            return this; 
        }
        public AiInvestigationResponseBuilder limitations(String limitations) { 
            this.limitations = limitations; 
            return this; 
        }
        public AiInvestigationResponseBuilder hypotheses(List<HypothesisDto> hypotheses) { 
            this.hypotheses = hypotheses; 
            return this; 
        }
        public AiInvestigationResponseBuilder contradictions(List<ContradictionDto> contradictions) { 
            this.contradictions = contradictions; 
            return this; 
        }
        public AiInvestigationResponseBuilder additionalEvidenceRequired(List<EvidenceRequestDto> additionalEvidenceRequired) { 
            this.additionalEvidenceRequired = additionalEvidenceRequired; 
            return this; 
        }
        public AiInvestigationResponseBuilder relatedExceptions(List<RelatedExceptionDto> relatedExceptions) { 
            this.relatedExceptions = relatedExceptions; 
            return this; 
        }
        public AiInvestigationResponseBuilder ragHistoricalCases(List<RagHistoricalCaseDto> ragHistoricalCases) { 
            this.ragHistoricalCases = ragHistoricalCases; 
            return this; 
        }

        public AiInvestigationResponse build() {
            return new AiInvestigationResponse(summary, likelyRootCause, supportingEvidence, confidenceScore, 
                    recommendedAction, actionTaken, autoResolved, requiresHumanReview, missingEvidence, 
                    similarHistoricalCases, softAnomalies, uncertaintyNotes, limitations,
                    hypotheses, contradictions, additionalEvidenceRequired, relatedExceptions, ragHistoricalCases);
        }
    }
}
