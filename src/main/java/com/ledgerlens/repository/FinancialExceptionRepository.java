package com.ledgerlens.repository;

import com.ledgerlens.entity.FinancialException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Repository
public interface FinancialExceptionRepository extends JpaRepository<FinancialException, UUID> {
    Optional<FinancialException> findByExceptionId(String exceptionId);
    boolean existsByExceptionId(String exceptionId);
    Optional<FinancialException> findByExceptionIdAndMerchantId(String exceptionId, String merchantId);
    List<FinancialException> findByMerchantId(String merchantId);
    Page<FinancialException> findByMerchantId(String merchantId, Pageable pageable);
    boolean existsByDeduplicationKey(String deduplicationKey);
    boolean existsByExceptionTypeAndStatusNotAndStatusNotAndOrderAndPaymentAndRefundAndSettlement(
            com.ledgerlens.entity.enums.ExceptionType exceptionType,
            com.ledgerlens.entity.enums.ExceptionStatus status1,
            com.ledgerlens.entity.enums.ExceptionStatus status2,
            com.ledgerlens.entity.Order order,
            com.ledgerlens.entity.Payment payment,
            com.ledgerlens.entity.Refund refund,
            com.ledgerlens.entity.Settlement settlement
    );
}
