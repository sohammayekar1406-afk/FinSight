package com.ledgerlens.repository;

import com.ledgerlens.entity.ReconciliationRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRun, UUID> {
    Optional<ReconciliationRun> findByIdempotencyKey(String idempotencyKey);
    java.util.List<ReconciliationRun> findByIdempotencyKeyStartingWith(String prefix);
    long countByIdempotencyKeyStartingWith(String prefix);
}
