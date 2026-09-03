package com.ledgerlens.repository;

import com.ledgerlens.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeeRepository extends JpaRepository<Fee, UUID> {
    List<Fee> findByPayment_PaymentId(String paymentId);
    List<Fee> findByRefund_RefundId(String refundId);
    java.util.Optional<Fee> findByIdAndMerchantId(UUID id, String merchantId);
    List<Fee> findByMerchantId(String merchantId);
}
