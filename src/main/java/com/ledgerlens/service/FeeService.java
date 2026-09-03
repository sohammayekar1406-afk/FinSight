package com.ledgerlens.service;

import com.ledgerlens.dto.FeeRequestDto;
import com.ledgerlens.dto.FeeResponseDto;
import com.ledgerlens.entity.Fee;
import com.ledgerlens.entity.Payment;
import com.ledgerlens.entity.Refund;
import com.ledgerlens.exception.InvalidReferenceException;
import com.ledgerlens.exception.ResourceNotFoundException;
import com.ledgerlens.repository.FeeRepository;
import com.ledgerlens.repository.PaymentRepository;
import com.ledgerlens.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FeeService {

    private final FeeRepository feeRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final MerchantContext merchantContext;

    public FeeService(FeeRepository feeRepository, PaymentRepository paymentRepository, RefundRepository refundRepository, MerchantContext merchantContext) {
        this.feeRepository = feeRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.merchantContext = merchantContext;
    }

    @Transactional
    public FeeResponseDto createFee(FeeRequestDto dto) {
        Payment payment = null;
        if (dto.getPaymentId() != null && !dto.getPaymentId().isBlank()) {
            payment = paymentRepository.findByPaymentIdAndMerchantId(dto.getPaymentId(), merchantContext.merchantId())
                    .orElseThrow(() -> new InvalidReferenceException("Referenced payment " + dto.getPaymentId() + " does not exist"));
        }

        Refund refund = null;
        if (dto.getRefundId() != null && !dto.getRefundId().isBlank()) {
            refund = refundRepository.findByRefundIdAndMerchantId(dto.getRefundId(), merchantContext.merchantId())
                    .orElseThrow(() -> new InvalidReferenceException("Referenced refund " + dto.getRefundId() + " does not exist"));
        }

        Fee fee = Fee.builder()
                .payment(payment)
                .refund(refund)
                .merchantId(merchantContext.merchantId())
                .feeAmount(dto.getFeeAmount())
                .taxAmount(dto.getTaxAmount())
                .totalFee(dto.getTotalFee())
                .feeRate(dto.getFeeRate())
                .currency(dto.getCurrency())
                .build();

        Fee saved = feeRepository.save(fee);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public FeeResponseDto getFeeById(UUID id) {
        Fee fee = feeRepository.findByIdAndMerchantId(id, merchantContext.merchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Fee record with id " + id + " was not found"));
        return mapToResponse(fee);
    }

    @Transactional(readOnly = true)
    public List<FeeResponseDto> getAllFees() {
        return feeRepository.findByMerchantId(merchantContext.merchantId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private FeeResponseDto mapToResponse(Fee fee) {
        return FeeResponseDto.builder()
                .id(fee.getId())
                .paymentId(fee.getPayment() != null ? fee.getPayment().getPaymentId() : null)
                .refundId(fee.getRefund() != null ? fee.getRefund().getRefundId() : null)
                .merchantId(fee.getMerchantId())
                .feeAmount(fee.getFeeAmount())
                .taxAmount(fee.getTaxAmount())
                .totalFee(fee.getTotalFee())
                .feeRate(fee.getFeeRate())
                .currency(fee.getCurrency())
                .createdAt(fee.getCreatedAt())
                .build();
    }
}
