package com.ledgerlens.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_logs_merchant_created", columnList = "merchant_id, created_at"),
    @Index(name = "idx_audit_logs_performed_by", columnList = "performed_by")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "merchant_id", length = 64)
    private String merchantId;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(name = "performed_by", nullable = false, length = 64)
    private String performedBy;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public AuditLog() {}

    public AuditLog(UUID id, String entityType, UUID entityId, String merchantId, String action, String performedBy, String details, OffsetDateTime createdAt) {
        this.id = id;
        this.entityType = entityType;
        this.entityId = entityId;
        this.merchantId = merchantId;
        this.action = action;
        this.performedBy = performedBy;
        this.details = details;
        this.createdAt = createdAt;
    }

    public static AuditLogBuilder builder() { return new AuditLogBuilder(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public static class AuditLogBuilder {
        private UUID id;
        private String entityType;
        private UUID entityId;
        private String merchantId;
        private String action;
        private String performedBy;
        private String details;
        private OffsetDateTime createdAt;

        public AuditLogBuilder id(UUID id) { this.id = id; return this; }
        public AuditLogBuilder entityType(String entityType) { this.entityType = entityType; return this; }
        public AuditLogBuilder entityId(UUID entityId) { this.entityId = entityId; return this; }
        public AuditLogBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public AuditLogBuilder action(String action) { this.action = action; return this; }
        public AuditLogBuilder performedBy(String performedBy) { this.performedBy = performedBy; return this; }
        public AuditLogBuilder details(String details) { this.details = details; return this; }
        public AuditLogBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public AuditLog build() {
            return new AuditLog(id, entityType, entityId, merchantId, action, performedBy, details, createdAt);
        }
    }
}
