package com.ledgerlens.repository;

import com.ledgerlens.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.order LEFT JOIN FETCH p.settlement WHERE p.paymentId = :paymentId AND p.merchantId = :merchantId")
    Optional<Payment> findByPaymentIdAndMerchantId(@Param("paymentId") String paymentId, @Param("merchantId") String merchantId);

    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.order LEFT JOIN FETCH p.settlement WHERE p.merchantId = :merchantId ORDER BY p.createdAt DESC")
    List<Payment> findByMerchantId(@Param("merchantId") String merchantId);
}
