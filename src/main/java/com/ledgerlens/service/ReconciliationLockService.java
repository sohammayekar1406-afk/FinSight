package com.ledgerlens.service;

import com.ledgerlens.entity.ReconciliationExecutionLock;
import com.ledgerlens.repository.ReconciliationExecutionLockRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ensures the durable global lock row exists before reconciliation requests arrive. */
@Service
public class ReconciliationLockService {
    private final ReconciliationExecutionLockRepository repository;
    public ReconciliationLockService(ReconciliationExecutionLockRepository repository) { this.repository = repository; }

    @Transactional
    public void ensureLockExists() {
        if (!repository.existsById(ReconciliationExecutionLock.GLOBAL_LOCK_ID)) {
            try {
                repository.saveAndFlush(new ReconciliationExecutionLock(ReconciliationExecutionLock.GLOBAL_LOCK_ID));
            } catch (DataIntegrityViolationException ignored) {
                // Another node initialized the same singleton row first.
            }
        }
    }
}
