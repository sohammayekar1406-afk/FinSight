package com.ledgerlens.repository;

import com.ledgerlens.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByPaymentId(String paymentId);
    boolean existsByPaymentId(String paymentId);
    List<Payment> findByOrder_OrderId(String orderId);
    List<Payment> findBySettlement_SettlementId(String settlementId);
    Optional<Payment> findByPaymentIdAndMerchantId(String paymentId, String merchantId);
    List<Payment> findByMerchantId(String merchantId);
}
