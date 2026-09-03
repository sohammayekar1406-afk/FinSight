package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.ActionTaken;
import com.ledgerlens.entity.enums.RecommendedAction;

import java.math.BigDecimal;

public class InvestigationAnalysis {

    private String summary;
    private String likelyRootCause;
    private BigDecimal confidenceScore;
    private RecommendedAction recommendedAction;
    private ActionTaken actionTaken;
    private boolean autoResolved;
    private InvestigationEvidenceDto evidence;

    public InvestigationAnalysis() {}

    public InvestigationAnalysis(String summary, String likelyRootCause, BigDecimal confidenceScore, RecommendedAction recommendedAction, ActionTaken actionTaken, boolean autoResolved, InvestigationEvidenceDto evidence) {
        this.summary = summary;
        this.likelyRootCause = likelyRootCause;
        this.confidenceScore = confidenceScore;
        this.recommendedAction = recommendedAction;
        this.actionTaken = actionTaken;
        this.autoResolved = autoResolved;
        this.evidence = evidence;
    }

    public static InvestigationAnalysisBuilder builder() { return new InvestigationAnalysisBuilder(); }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getLikelyRootCause() { return likelyRootCause; }
    public void setLikelyRootCause(String likelyRootCause) { this.likelyRootCause = likelyRootCause; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }
    public RecommendedAction getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(RecommendedAction recommendedAction) { this.recommendedAction = recommendedAction; }
    public ActionTaken getActionTaken() { return actionTaken; }
    public void setActionTaken(ActionTaken actionTaken) { this.actionTaken = actionTaken; }
    public boolean isAutoResolved() { return autoResolved; }
    public void setAutoResolved(boolean autoResolved) { this.autoResolved = autoResolved; }
    public InvestigationEvidenceDto getEvidence() { return evidence; }
    public void setEvidence(InvestigationEvidenceDto evidence) { this.evidence = evidence; }

    public static class InvestigationAnalysisBuilder {
        private String summary;
        private String likelyRootCause;
        private BigDecimal confidenceScore;
        private RecommendedAction recommendedAction;
        private ActionTaken actionTaken;
        private boolean autoResolved = false;
        private InvestigationEvidenceDto evidence;

        public InvestigationAnalysisBuilder summary(String summary) { this.summary = summary; return this; }
        public InvestigationAnalysisBuilder likelyRootCause(String likelyRootCause) { this.likelyRootCause = likelyRootCause; return this; }
        public InvestigationAnalysisBuilder confidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public InvestigationAnalysisBuilder recommendedAction(RecommendedAction recommendedAction) { this.recommendedAction = recommendedAction; return this; }
        public InvestigationAnalysisBuilder actionTaken(ActionTaken actionTaken) { this.actionTaken = actionTaken; return this; }
        public InvestigationAnalysisBuilder autoResolved(boolean autoResolved) { this.autoResolved = autoResolved; return this; }
        public InvestigationAnalysisBuilder evidence(InvestigationEvidenceDto evidence) { this.evidence = evidence; return this; }

        public InvestigationAnalysis build() {
            return new InvestigationAnalysis(summary, likelyRootCause, confidenceScore, recommendedAction, actionTaken, autoResolved, evidence);
        }
    }
}
