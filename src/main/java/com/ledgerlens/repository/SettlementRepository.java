package com.ledgerlens.repository;

import com.ledgerlens.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    Optional<Settlement> findBySettlementId(String settlementId);
    boolean existsBySettlementId(String settlementId);
    Optional<Settlement> findBySettlementIdAndMerchantId(String settlementId, String merchantId);
    List<Settlement> findByMerchantId(String merchantId);
}
