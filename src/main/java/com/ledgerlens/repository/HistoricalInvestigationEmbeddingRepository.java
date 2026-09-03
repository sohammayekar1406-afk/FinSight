package com.ledgerlens.repository;

import com.ledgerlens.entity.HistoricalInvestigationEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HistoricalInvestigationEmbeddingRepository extends JpaRepository<HistoricalInvestigationEmbedding, UUID> {

    List<HistoricalInvestigationEmbedding> findByMerchantId(String merchantId);

    Optional<HistoricalInvestigationEmbedding> findByInvestigation_Id(UUID investigationId);

    @Query("SELECT e FROM HistoricalInvestigationEmbedding e " +
           "JOIN FETCH e.investigation inv " +
           "JOIN FETCH inv.exception ex " +
           "WHERE e.merchantId = :merchantId " +
           "AND ex.exceptionId != :excludeExceptionId " +
           "AND (ex.status = com.ledgerlens.entity.enums.ExceptionStatus.RESOLVED_AUTO " +
           "     OR ex.status = com.ledgerlens.entity.enums.ExceptionStatus.RESOLVED_MANUAL)")
    List<HistoricalInvestigationEmbedding> findResolvedEmbeddingsByMerchant(
            @Param("merchantId") String merchantId,
            @Param("excludeExceptionId") String excludeExceptionId);

    void deleteByInvestigation_Id(UUID investigationId);
}
