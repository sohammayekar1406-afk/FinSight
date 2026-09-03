package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.ActionTaken;
import com.ledgerlens.entity.enums.RecommendedAction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class InvestigationResponseDto {

    private String exceptionId;
    private String investigationId;
    private String summary;
    private String likelyRootCause;
    private BigDecimal confidenceScore;
    private RecommendedAction recommendedAction;
    private ActionTaken actionTaken;
    private boolean autoResolved;
    private boolean aiUsed;
    private String analysisSource;
    private OffsetDateTime investigatedAt;
    private InvestigationEvidenceDto evidence;

    public InvestigationResponseDto() {}

    public InvestigationResponseDto(String exceptionId, String investigationId, String summary, String likelyRootCause, BigDecimal confidenceScore, RecommendedAction recommendedAction, ActionTaken actionTaken, boolean autoResolved, boolean aiUsed, String analysisSource, OffsetDateTime investigatedAt, InvestigationEvidenceDto evidence) {
        this.exceptionId = exceptionId;
        this.investigationId = investigationId;
        this.summary = summary;
        this.likelyRootCause = likelyRootCause;
        this.confidenceScore = confidenceScore;
        this.recommendedAction = recommendedAction;
        this.actionTaken = actionTaken;
        this.autoResolved = autoResolved;
        this.aiUsed = aiUsed;
        this.analysisSource = analysisSource;
        this.investigatedAt = investigatedAt;
        this.evidence = evidence;
    }

    public static InvestigationResponseDtoBuilder builder() { return new InvestigationResponseDtoBuilder(); }

    public String getExceptionId() { return exceptionId; }
    public void setExceptionId(String exceptionId) { this.exceptionId = exceptionId; }

    public String getInvestigationId() { return investigationId; }
    public void setInvestigationId(String investigationId) { this.investigationId = investigationId; }

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

    public boolean isAiUsed() { return aiUsed; }
    public void setAiUsed(boolean aiUsed) { this.aiUsed = aiUsed; }

    public String getAnalysisSource() { return analysisSource; }
    public void setAnalysisSource(String analysisSource) { this.analysisSource = analysisSource; }

    public OffsetDateTime getInvestigatedAt() { return investigatedAt; }
    public void setInvestigatedAt(OffsetDateTime investigatedAt) { this.investigatedAt = investigatedAt; }

    public InvestigationEvidenceDto getEvidence() { return evidence; }
    public void setEvidence(InvestigationEvidenceDto evidence) { this.evidence = evidence; }

    public static class InvestigationResponseDtoBuilder {
        private String exceptionId;
        private String investigationId;
        private String summary;
        private String likelyRootCause;
        private BigDecimal confidenceScore;
        private RecommendedAction recommendedAction;
        private ActionTaken actionTaken;
        private boolean autoResolved = false;
        private boolean aiUsed = false;
        private String analysisSource = "RULE_BASED_FALLBACK";
        private OffsetDateTime investigatedAt;
        private InvestigationEvidenceDto evidence;

        public InvestigationResponseDtoBuilder exceptionId(String exceptionId) { this.exceptionId = exceptionId; return this; }
        public InvestigationResponseDtoBuilder investigationId(String investigationId) { this.investigationId = investigationId; return this; }
        public InvestigationResponseDtoBuilder summary(String summary) { this.summary = summary; return this; }
        public InvestigationResponseDtoBuilder likelyRootCause(String likelyRootCause) { this.likelyRootCause = likelyRootCause; return this; }
        public InvestigationResponseDtoBuilder confidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public InvestigationResponseDtoBuilder recommendedAction(RecommendedAction recommendedAction) { this.recommendedAction = recommendedAction; return this; }
        public InvestigationResponseDtoBuilder actionTaken(ActionTaken actionTaken) { this.actionTaken = actionTaken; return this; }
        public InvestigationResponseDtoBuilder autoResolved(boolean autoResolved) { this.autoResolved = autoResolved; return this; }
        public InvestigationResponseDtoBuilder aiUsed(boolean aiUsed) { this.aiUsed = aiUsed; return this; }
        public InvestigationResponseDtoBuilder analysisSource(String analysisSource) { this.analysisSource = analysisSource; return this; }
        public InvestigationResponseDtoBuilder investigatedAt(OffsetDateTime investigatedAt) { this.investigatedAt = investigatedAt; return this; }
        public InvestigationResponseDtoBuilder evidence(InvestigationEvidenceDto evidence) { this.evidence = evidence; return this; }

        public InvestigationResponseDto build() {
            return new InvestigationResponseDto(exceptionId, investigationId, summary, likelyRootCause, confidenceScore, recommendedAction, actionTaken, autoResolved, aiUsed, analysisSource, investigatedAt, evidence);
        }
    }
}
