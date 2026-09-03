package com.ledgerlens.service;

import com.ledgerlens.dto.FinancialExceptionResponseDto;
import com.ledgerlens.dto.PagedResponseDto;
import com.ledgerlens.entity.FinancialException;
import com.ledgerlens.exception.ResourceNotFoundException;
import com.ledgerlens.repository.FinancialExceptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinancialExceptionService {

    private final FinancialExceptionRepository exceptionRepository;
    private final MerchantContext merchantContext;

    public FinancialExceptionService(FinancialExceptionRepository exceptionRepository, MerchantContext merchantContext) {
        this.exceptionRepository = exceptionRepository;
        this.merchantContext = merchantContext;
    }

    @Transactional(readOnly = true)
    public FinancialExceptionResponseDto getExceptionByExceptionId(String exceptionId) {
        FinancialException ex = exceptionRepository.findByExceptionIdAndMerchantId(exceptionId, merchantContext.merchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Exception " + exceptionId + " was not found"));
        return mapToResponse(ex);
    }

    @Transactional(readOnly = true)
    public List<FinancialExceptionResponseDto> getAllExceptions() {
        return exceptionRepository.findByMerchantId(merchantContext.merchantId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponseDto<FinancialExceptionResponseDto> getExceptionsPaged(org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<FinancialException> page = exceptionRepository.findByMerchantId(merchantContext.merchantId(), pageable);
        List<FinancialExceptionResponseDto> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponseDto.<FinancialExceptionResponseDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private FinancialExceptionResponseDto mapToResponse(FinancialException ex) {
        return FinancialExceptionResponseDto.builder()
                .id(ex.getId())
                .exceptionId(ex.getExceptionId())
                .merchantId(ex.getMerchantId())
                .exceptionType(ex.getExceptionType())
                .severity(ex.getSeverity())
                .status(ex.getStatus())
                .expectedAmount(ex.getExpectedAmount())
                .actualAmount(ex.getActualAmount())
                .discrepancyAmount(ex.getDiscrepancyAmount())
                .orderId(ex.getOrder() != null ? ex.getOrder().getOrderId() : null)
                .paymentId(ex.getPayment() != null ? ex.getPayment().getPaymentId() : null)
                .refundId(ex.getRefund() != null ? ex.getRefund().getRefundId() : null)
                .settlementId(ex.getSettlement() != null ? ex.getSettlement().getSettlementId() : null)
                .description(ex.getDescription())
                .detectedAt(ex.getDetectedAt())
                .createdAt(ex.getCreatedAt())
                .resolvedAt(ex.getResolvedAt())
                .build();
    }
}
