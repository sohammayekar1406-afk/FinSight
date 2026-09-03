package com.ledgerlens.repository;

import com.ledgerlens.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByOrderId(String orderId);
    boolean existsByOrderId(String orderId);
    Optional<Order> findByOrderIdAndMerchantId(String orderId, String merchantId);
    List<Order> findByMerchantId(String merchantId);
}
