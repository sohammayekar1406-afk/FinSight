package com.ledgerlens.repository;

import com.ledgerlens.entity.ReconciliationExecutionLock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReconciliationExecutionLockRepository extends JpaRepository<ReconciliationExecutionLock, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from ReconciliationExecutionLock l where l.id = :id")
    Optional<ReconciliationExecutionLock> findByIdForUpdate(String id);
}
