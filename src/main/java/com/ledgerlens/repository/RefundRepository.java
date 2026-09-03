package com.ledgerlens.repository;

import com.ledgerlens.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {
    Optional<Refund> findByRefundId(String refundId);
    boolean existsByRefundId(String refundId);
    List<Refund> findByPayment_PaymentId(String paymentId);
    List<Refund> findBySettlement_SettlementId(String settlementId);
    Optional<Refund> findByRefundIdAndMerchantId(String refundId, String merchantId);
    List<Refund> findByMerchantId(String merchantId);
}
