package com.ledgerlens.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Phase 3.5: Evidence Graph Node
 * 
 * Represents a single node in the transaction evidence graph.
 * Each node is a financial entity with provenance and relationship information.
 */
public class EvidenceNodeDto {

    public enum EntityType {
        ORDER,
        PAYMENT,
        REFUND,
        FEE,
        ADJUSTMENT,
        SETTLEMENT,
        EXCEPTION
    }

    public enum AvailabilityStatus {
        FOUND,              // Entity exists and was retrieved
        MISSING,            // Entity should exist but doesn't
        NOT_APPLICABLE,     // Entity is not relevant for this exception type
        UNAVAILABLE         // Entity might exist but cannot be retrieved
    }

    private EntityType entityType;
    private String entityId;
    private AvailabilityStatus availability;
    private String relationshipToException;  // e.g., "PRIMARY_PAYMENT", "LINKED_REFUND", "SETTLEMENT"
    private String source;  // e.g., "payments table", "settlements table"
    private BigDecimal amount;
    private String currency;
    private String status;
    private OffsetDateTime timestamp;
    private String relevanceReason;  // Why this evidence is relevant

    public EvidenceNodeDto() {}

    public EvidenceNodeDto(EntityType entityType, String entityId, AvailabilityStatus availability,
                          String relationshipToException, String source, BigDecimal amount,
                          String currency, String status, OffsetDateTime timestamp, String relevanceReason) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.availability = availability;
        this.relationshipToException = relationshipToException;
        this.source = source;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.timestamp = timestamp;
        this.relevanceReason = relevanceReason;
    }

    public static EvidenceNodeDtoBuilder builder() { return new EvidenceNodeDtoBuilder(); }

    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public AvailabilityStatus getAvailability() { return availability; }
    public void setAvailability(AvailabilityStatus availability) { this.availability = availability; }

    public String getRelationshipToException() { return relationshipToException; }
    public void setRelationshipToException(String relationshipToException) { this.relationshipToException = relationshipToException; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

    public String getRelevanceReason() { return relevanceReason; }
    public void setRelevanceReason(String relevanceReason) { this.relevanceReason = relevanceReason; }

    public static class EvidenceNodeDtoBuilder {
        private EntityType entityType;
        private String entityId;
        private AvailabilityStatus availability;
        private String relationshipToException;
        private String source;
        private BigDecimal amount;
        private String currency;
        private String status;
        private OffsetDateTime timestamp;
        private String relevanceReason;

        public EvidenceNodeDtoBuilder entityType(EntityType entityType) { this.entityType = entityType; return this; }
        public EvidenceNodeDtoBuilder entityId(String entityId) { this.entityId = entityId; return this; }
        public EvidenceNodeDtoBuilder availability(AvailabilityStatus availability) { this.availability = availability; return this; }
        public EvidenceNodeDtoBuilder relationshipToException(String relationshipToException) { this.relationshipToException = relationshipToException; return this; }
        public EvidenceNodeDtoBuilder source(String source) { this.source = source; return this; }
        public EvidenceNodeDtoBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public EvidenceNodeDtoBuilder currency(String currency) { this.currency = currency; return this; }
        public EvidenceNodeDtoBuilder status(String status) { this.status = status; return this; }
        public EvidenceNodeDtoBuilder timestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; return this; }
        public EvidenceNodeDtoBuilder relevanceReason(String relevanceReason) { this.relevanceReason = relevanceReason; return this; }

        public EvidenceNodeDto build() {
            return new EvidenceNodeDto(entityType, entityId, availability, relationshipToException, source,
                    amount, currency, status, timestamp, relevanceReason);
        }
    }
}
