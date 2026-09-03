package com.ledgerlens.service;

import com.ledgerlens.dto.SettlementRequestDto;
import com.ledgerlens.dto.SettlementResponseDto;
import com.ledgerlens.entity.Settlement;
import com.ledgerlens.entity.enums.SettlementStatus;
import com.ledgerlens.exception.DuplicateResourceException;
import com.ledgerlens.exception.ResourceNotFoundException;
import com.ledgerlens.repository.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final MerchantContext merchantContext;

    public SettlementService(SettlementRepository settlementRepository, MerchantContext merchantContext) {
        this.settlementRepository = settlementRepository;
        this.merchantContext = merchantContext;
    }

    @Transactional
    public SettlementResponseDto createSettlement(SettlementRequestDto dto) {
        if (settlementRepository.existsBySettlementId(dto.getSettlementId())) {
            throw new DuplicateResourceException("Settlement with settlementId " + dto.getSettlementId() + " already exists");
        }

        Settlement settlement = Settlement.builder()
                .settlementId(dto.getSettlementId())
                .merchantId(merchantContext.merchantId())
                .grossAmount(dto.getGrossAmount())
                .totalRefundAmount(dto.getTotalRefundAmount())
                .totalFeeAmount(dto.getTotalFeeAmount())
                .totalTaxAmount(dto.getTotalTaxAmount())
                .totalAdjustmentAmount(dto.getTotalAdjustmentAmount())
                .netAmount(dto.getNetAmount())
                .actualSettledAmount(dto.getActualSettledAmount())
                .status(dto.getStatus())
                .utr(dto.getUtr())
                .settledAt(dto.getStatus() == SettlementStatus.SETTLED ? OffsetDateTime.now() : null)
                .build();

        Settlement saved = settlementRepository.save(settlement);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public SettlementResponseDto getSettlementBySettlementId(String settlementId) {
        Settlement settlement = settlementRepository.findBySettlementIdAndMerchantId(settlementId, merchantContext.merchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Settlement " + settlementId + " was not found"));
        return mapToResponse(settlement);
    }

    @Transactional(readOnly = true)
    public List<SettlementResponseDto> getAllSettlements() {
        return settlementRepository.findByMerchantId(merchantContext.merchantId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SettlementResponseDto mapToResponse(Settlement settlement) {
        return SettlementResponseDto.builder()
                .id(settlement.getId())
                .settlementId(settlement.getSettlementId())
                .merchantId(settlement.getMerchantId())
                .grossAmount(settlement.getGrossAmount())
                .totalRefundAmount(settlement.getTotalRefundAmount())
                .totalFeeAmount(settlement.getTotalFeeAmount())
                .totalTaxAmount(settlement.getTotalTaxAmount())
                .totalAdjustmentAmount(settlement.getTotalAdjustmentAmount())
                .netAmount(settlement.getNetAmount())
                .actualSettledAmount(settlement.getActualSettledAmount())
                .status(settlement.getStatus())
                .utr(settlement.getUtr())
                .settledAt(settlement.getSettledAt())
                .createdAt(settlement.getCreatedAt())
                .build();
    }
}
