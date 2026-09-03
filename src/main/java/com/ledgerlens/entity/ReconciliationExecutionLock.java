package com.ledgerlens.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A single durable mutex shared by all application instances for global runs. */
@Entity
@Table(name = "reconciliation_execution_locks")
public class ReconciliationExecutionLock {
    public static final String GLOBAL_LOCK_ID = "GLOBAL_RECONCILIATION";

    @Id
    private String id;

    public ReconciliationExecutionLock() { }
    public ReconciliationExecutionLock(String id) { this.id = id; }
    public String getId() { return id; }
}
