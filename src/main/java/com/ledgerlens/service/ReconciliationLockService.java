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

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void ensureLockExists(String lockId) {
        if (!repository.existsById(lockId)) {
            try {
                repository.saveAndFlush(new ReconciliationExecutionLock(lockId));
            } catch (DataIntegrityViolationException ignored) {
                // Another node or transaction initialized the same singleton row first.
            }
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void ensureLockExists() {
        ensureLockExists(ReconciliationExecutionLock.GLOBAL_LOCK_ID);
        ensureLockExists("MERCHANT:merchant_a");
        ensureLockExists("MERCHANT:merchant_b");
    }
}
