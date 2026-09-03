package com.ledgerlens.repository;

import com.ledgerlens.entity.Adjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdjustmentRepository extends JpaRepository<Adjustment, UUID> {
    Optional<Adjustment> findByAdjustmentId(String adjustmentId);
    boolean existsByAdjustmentId(String adjustmentId);
    List<Adjustment> findBySettlement_SettlementId(String settlementId);
    List<Adjustment> findByPayment_PaymentId(String paymentId);
    Optional<Adjustment> findByAdjustmentIdAndMerchantId(String adjustmentId, String merchantId);
    List<Adjustment> findByMerchantId(String merchantId);
}
