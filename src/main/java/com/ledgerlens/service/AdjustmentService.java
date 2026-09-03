package com.ledgerlens.service;

import com.ledgerlens.dto.AdjustmentRequestDto;
import com.ledgerlens.dto.AdjustmentResponseDto;
import com.ledgerlens.entity.Adjustment;
import com.ledgerlens.entity.Payment;
import com.ledgerlens.entity.Settlement;
import com.ledgerlens.exception.DuplicateResourceException;
import com.ledgerlens.exception.InvalidReferenceException;
import com.ledgerlens.exception.ResourceNotFoundException;
import com.ledgerlens.repository.AdjustmentRepository;
import com.ledgerlens.repository.PaymentRepository;
import com.ledgerlens.repository.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdjustmentService {

    private final AdjustmentRepository adjustmentRepository;
    private final SettlementRepository settlementRepository;
    private final PaymentRepository paymentRepository;
    private final MerchantContext merchantContext;

    public AdjustmentService(AdjustmentRepository adjustmentRepository, SettlementRepository settlementRepository, PaymentRepository paymentRepository, MerchantContext merchantContext) {
        this.adjustmentRepository = adjustmentRepository;
        this.settlementRepository = settlementRepository;
        this.paymentRepository = paymentRepository;
        this.merchantContext = merchantContext;
    }

    @Transactional
    public AdjustmentResponseDto createAdjustment(AdjustmentRequestDto dto) {
        if (adjustmentRepository.existsByAdjustmentId(dto.getAdjustmentId())) {
            throw new DuplicateResourceException("Adjustment with adjustmentId " + dto.getAdjustmentId() + " already exists");
        }

        Settlement settlement = null;
        if (dto.getSettlementId() != null && !dto.getSettlementId().isBlank()) {
            settlement = settlementRepository.findBySettlementIdAndMerchantId(dto.getSettlementId(), merchantContext.merchantId())
                    .orElseThrow(() -> new InvalidReferenceException("Referenced settlement " + dto.getSettlementId() + " does not exist"));
        }

        Payment payment = null;
        if (dto.getPaymentId() != null && !dto.getPaymentId().isBlank()) {
            payment = paymentRepository.findByPaymentIdAndMerchantId(dto.getPaymentId(), merchantContext.merchantId())
                    .orElseThrow(() -> new InvalidReferenceException("Referenced payment " + dto.getPaymentId() + " does not exist"));
        }

        Adjustment adjustment = Adjustment.builder()
                .adjustmentId(dto.getAdjustmentId())
                .merchantId(merchantContext.merchantId())
                .settlement(settlement)
                .payment(payment)
                .amount(dto.getAmount())
                .type(dto.getType())
                .description(dto.getDescription())
                .build();

        Adjustment saved = adjustmentRepository.save(adjustment);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public AdjustmentResponseDto getAdjustmentByAdjustmentId(String adjustmentId) {
        Adjustment adjustment = adjustmentRepository.findByAdjustmentIdAndMerchantId(adjustmentId, merchantContext.merchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Adjustment " + adjustmentId + " was not found"));
        return mapToResponse(adjustment);
    }

    @Transactional(readOnly = true)
    public List<AdjustmentResponseDto> getAllAdjustments() {
        return adjustmentRepository.findByMerchantId(merchantContext.merchantId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AdjustmentResponseDto mapToResponse(Adjustment adjustment) {
        return AdjustmentResponseDto.builder()
                .id(adjustment.getId())
                .adjustmentId(adjustment.getAdjustmentId())
                .merchantId(adjustment.getMerchantId())
                .settlementId(adjustment.getSettlement() != null ? adjustment.getSettlement().getSettlementId() : null)
                .paymentId(adjustment.getPayment() != null ? adjustment.getPayment().getPaymentId() : null)
                .amount(adjustment.getAmount())
                .type(adjustment.getType())
                .description(adjustment.getDescription())
                .createdAt(adjustment.getCreatedAt())
                .build();
    }
}
