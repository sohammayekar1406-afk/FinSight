package com.ledgerlens.entity;

import com.ledgerlens.entity.enums.ActionTaken;
import com.ledgerlens.entity.enums.RecommendedAction;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "investigations", indexes = {
    @Index(name = "idx_investigations_exception_id", columnList = "exception_id")
})
public class Investigation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exception_id", nullable = false, unique = true)
    private FinancialException exception;

    @Column(name = "likely_root_cause", columnDefinition = "TEXT")
    private String likelyRootCause;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_action", length = 32)
    private RecommendedAction recommendedAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_taken", length = 32)
    private ActionTaken actionTaken;

    @Column(name = "auto_resolved", nullable = false)
    private boolean autoResolved = false;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "human_notes", columnDefinition = "TEXT")
    private String humanNotes;

    @Column(name = "ai_model_version", length = 64)
    private String aiModelVersion = "rule-based-v1.0";

    @Column(name = "supporting_evidence", columnDefinition = "TEXT")
    private String supportingEvidence;

    @CreationTimestamp
    @Column(name = "investigated_at", nullable = false, updatable = false)
    private OffsetDateTime investigatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public Investigation() {}

    public Investigation(UUID id, FinancialException exception, String likelyRootCause, BigDecimal confidenceScore, RecommendedAction recommendedAction, ActionTaken actionTaken, boolean autoResolved, String summary, String humanNotes, String aiModelVersion, String supportingEvidence, OffsetDateTime investigatedAt, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.exception = exception;
        this.likelyRootCause = likelyRootCause;
        this.confidenceScore = confidenceScore;
        this.recommendedAction = recommendedAction;
        this.actionTaken = actionTaken;
        this.autoResolved = autoResolved;
        this.summary = summary;
        this.humanNotes = humanNotes;
        this.aiModelVersion = aiModelVersion != null ? aiModelVersion : "rule-based-v1.0";
        this.supportingEvidence = supportingEvidence;
        this.investigatedAt = investigatedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static InvestigationBuilder builder() { return new InvestigationBuilder(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public FinancialException getException() { return exception; }
    public void setException(FinancialException exception) { this.exception = exception; }
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
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getHumanNotes() { return humanNotes; }
    public void setHumanNotes(String humanNotes) { this.humanNotes = humanNotes; }
    public String getAiModelVersion() { return aiModelVersion; }
    public void setAiModelVersion(String aiModelVersion) { this.aiModelVersion = aiModelVersion; }
    public String getSupportingEvidence() { return supportingEvidence; }
    public void setSupportingEvidence(String supportingEvidence) { this.supportingEvidence = supportingEvidence; }
    public OffsetDateTime getInvestigatedAt() { return investigatedAt; }
    public void setInvestigatedAt(OffsetDateTime investigatedAt) { this.investigatedAt = investigatedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class InvestigationBuilder {
        private UUID id;
        private FinancialException exception;
        private String likelyRootCause;
        private BigDecimal confidenceScore;
        private RecommendedAction recommendedAction;
        private ActionTaken actionTaken;
        private boolean autoResolved = false;
        private String summary;
        private String humanNotes;
        private String aiModelVersion = "rule-based-v1.0";
        private String supportingEvidence;
        private OffsetDateTime investigatedAt;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public InvestigationBuilder id(UUID id) { this.id = id; return this; }
        public InvestigationBuilder exception(FinancialException exception) { this.exception = exception; return this; }
        public InvestigationBuilder likelyRootCause(String likelyRootCause) { this.likelyRootCause = likelyRootCause; return this; }
        public InvestigationBuilder confidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public InvestigationBuilder recommendedAction(RecommendedAction recommendedAction) { this.recommendedAction = recommendedAction; return this; }
        public InvestigationBuilder actionTaken(ActionTaken actionTaken) { this.actionTaken = actionTaken; return this; }
        public InvestigationBuilder autoResolved(boolean autoResolved) { this.autoResolved = autoResolved; return this; }
        public InvestigationBuilder summary(String summary) { this.summary = summary; return this; }
        public InvestigationBuilder humanNotes(String humanNotes) { this.humanNotes = humanNotes; return this; }
        public InvestigationBuilder aiModelVersion(String aiModelVersion) { this.aiModelVersion = aiModelVersion; return this; }
        public InvestigationBuilder supportingEvidence(String supportingEvidence) { this.supportingEvidence = supportingEvidence; return this; }
        public InvestigationBuilder investigatedAt(OffsetDateTime investigatedAt) { this.investigatedAt = investigatedAt; return this; }
        public InvestigationBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public InvestigationBuilder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Investigation build() {
            return new Investigation(id, exception, likelyRootCause, confidenceScore, recommendedAction, actionTaken, autoResolved, summary, humanNotes, aiModelVersion, supportingEvidence, investigatedAt, createdAt, updatedAt);
        }
    }
}
