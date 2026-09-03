package com.ledgerlens.repository;

import com.ledgerlens.entity.Investigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestigationRepository extends JpaRepository<Investigation, UUID> {
    Optional<Investigation> findByException_ExceptionId(String exceptionId);
    Optional<Investigation> findByException_ExceptionIdAndException_MerchantId(String exceptionId, String merchantId);
    List<Investigation> findByException_MerchantId(String merchantId);
}
