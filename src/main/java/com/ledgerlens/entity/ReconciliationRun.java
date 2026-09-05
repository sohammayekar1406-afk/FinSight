package com.ledgerlens.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Stores the response for an idempotent global reconciliation request. */
@Entity
@Table(name = "reconciliation_runs", indexes = @Index(name = "idx_reconciliation_runs_key", columnList = "idempotency_key", unique = true))
public class ReconciliationRun {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Lob
    @Column(name = "result_payload", nullable = false)
    private String resultPayload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public ReconciliationRun() { }
    public ReconciliationRun(String idempotencyKey, String resultPayload) { this.idempotencyKey = idempotencyKey; this.resultPayload = resultPayload; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getResultPayload() { return resultPayload; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
