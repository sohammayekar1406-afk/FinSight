package com.ledgerlens.service;

import com.ledgerlens.dto.RefundRequestDto;
import com.ledgerlens.dto.RefundResponseDto;
import com.ledgerlens.entity.Payment;
import com.ledgerlens.entity.Refund;
import com.ledgerlens.exception.DuplicateResourceException;
import com.ledgerlens.exception.InvalidReferenceException;
import com.ledgerlens.exception.ResourceNotFoundException;
import com.ledgerlens.repository.PaymentRepository;
import com.ledgerlens.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final MerchantContext merchantContext;

    public RefundService(RefundRepository refundRepository, PaymentRepository paymentRepository, MerchantContext merchantContext) {
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.merchantContext = merchantContext;
    }

    @Transactional
    public RefundResponseDto createRefund(RefundRequestDto dto) {
        if (refundRepository.existsByRefundId(dto.getRefundId())) {
            throw new DuplicateResourceException("Refund with refundId " + dto.getRefundId() + " already exists");
        }

        String merchantId = merchantContext.merchantId();
        Payment payment = paymentRepository.findByPaymentIdAndMerchantId(dto.getPaymentId(), merchantId)
                .orElseThrow(() -> new InvalidReferenceException("Referenced payment " + dto.getPaymentId() + " does not exist"));

        Refund refund = Refund.builder()
                .refundId(dto.getRefundId())
                .payment(payment)
                .merchantId(merchantId)
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .status(dto.getStatus())
                .reason(dto.getReason())
                .processedAt(OffsetDateTime.now())
                .build();

        Refund saved = refundRepository.save(refund);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public RefundResponseDto getRefundByRefundId(String refundId) {
        Refund refund = refundRepository.findByRefundIdAndMerchantId(refundId, merchantContext.merchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Refund " + refundId + " was not found"));
        return mapToResponse(refund);
    }

    @Transactional(readOnly = true)
    public List<RefundResponseDto> getAllRefunds() {
        return refundRepository.findByMerchantId(merchantContext.merchantId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RefundResponseDto mapToResponse(Refund refund) {
        return RefundResponseDto.builder()
                .id(refund.getId())
                .refundId(refund.getRefundId())
                .paymentId(refund.getPayment().getPaymentId())
                .merchantId(refund.getMerchantId())
                .amount(refund.getAmount())
                .currency(refund.getCurrency())
                .status(refund.getStatus())
                .reason(refund.getReason())
                .settlementId(refund.getSettlement() != null ? refund.getSettlement().getSettlementId() : null)
                .createdAt(refund.getCreatedAt())
                .processedAt(refund.getProcessedAt())
                .build();
    }
}
