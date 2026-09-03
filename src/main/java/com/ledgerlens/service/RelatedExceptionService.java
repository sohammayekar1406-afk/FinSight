package com.ledgerlens.service;

import com.ledgerlens.dto.RelatedExceptionDto;
import com.ledgerlens.entity.FinancialException;
import com.ledgerlens.repository.FinancialExceptionRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Phase 3 Forensic Reasoning: Cross-Exception Correlation Service
 * 
 * Identifies related exceptions within the same merchant for correlation analysis.
 * All queries are merchant-scoped and bounded.
 * 
 * SECURITY: Merchant-scoped, no cross-merchant correlation
 */
@Service
public class RelatedExceptionService {

    private static final int MAX_RELATED_EXCEPTIONS = 5;

    private final FinancialExceptionRepository exceptionRepository;
    private final MerchantContext merchantContext;

    public RelatedExceptionService(FinancialExceptionRepository exceptionRepository, 
                                  MerchantContext merchantContext) {
        this.exceptionRepository = exceptionRepository;
        this.merchantContext = merchantContext;
    }

    /**
     * Finds related exceptions for the current exception within the same merchant.
     * 
     * Possible relationships:
     * - Same payment
     * - Same settlement
     * - Same refund
     * - Same exception type
     * 
     * @param currentException The current exception being investigated
     * @return List of at most 5 related exceptions (empty if none found)
     */
    public List<RelatedExceptionDto> findRelatedExceptions(FinancialException currentException) {
        if (currentException == null) {
            return List.of();
        }

        String merchantId = merchantContext.merchantId();
        
        // Fetch all merchant exceptions (merchant-scoped query)
        List<FinancialException> allMerchantExceptions = exceptionRepository.findByMerchantId(merchantId);

        List<RelatedExceptionDto> related = new ArrayList<>();

        // Find exceptions related by payment
        if (currentException.getPayment() != null) {
            related.addAll(findByPayment(currentException, allMerchantExceptions));
        }

        // Find exceptions related by settlement
        if (currentException.getSettlement() != null) {
            related.addAll(findBySettlement(currentException, allMerchantExceptions));
        }

        // Find exceptions related by refund
        if (currentException.getRefund() != null) {
            related.addAll(findByRefund(currentException, allMerchantExceptions));
        }

        // Find exceptions of same type (if no other relationships found)
        if (related.isEmpty()) {
            related.addAll(findByExceptionType(currentException, allMerchantExceptions));
        }

        // Limit to max results and ensure no duplicates
        return related.stream()
                .distinct()
                .limit(MAX_RELATED_EXCEPTIONS)
                .collect(Collectors.toList());
    }

    private List<RelatedExceptionDto> findByPayment(FinancialException current, List<FinancialException> all) {
        return all.stream()
                .filter(ex -> ex.getPayment() != null)
                .filter(ex -> ex.getPayment().getId().equals(current.getPayment().getId()))
                .filter(ex -> !ex.getId().equals(current.getId()))
                .limit(3)
                .map(ex -> convertToDto(ex, RelatedExceptionDto.RelationshipType.SAME_PAYMENT, 
                        "Payment ID: " + ex.getPayment().getPaymentId()))
                .collect(Collectors.toList());
    }

    private List<RelatedExceptionDto> findBySettlement(FinancialException current, List<FinancialException> all) {
        return all.stream()
                .filter(ex -> ex.getSettlement() != null)
                .filter(ex -> ex.getSettlement().getId().equals(current.getSettlement().getId()))
                .filter(ex -> !ex.getId().equals(current.getId()))
                .limit(3)
                .map(ex -> convertToDto(ex, RelatedExceptionDto.RelationshipType.SAME_SETTLEMENT,
                        "Settlement ID: " + ex.getSettlement().getSettlementId()))
                .collect(Collectors.toList());
    }

    private List<RelatedExceptionDto> findByRefund(FinancialException current, List<FinancialException> all) {
        return all.stream()
                .filter(ex -> ex.getRefund() != null)
                .filter(ex -> ex.getRefund().getId().equals(current.getRefund().getId()))
                .filter(ex -> !ex.getId().equals(current.getId()))
                .limit(3)
                .map(ex -> convertToDto(ex, RelatedExceptionDto.RelationshipType.SAME_REFUND,
                        "Refund ID: " + ex.getRefund().getRefundId()))
                .collect(Collectors.toList());
    }

    private List<RelatedExceptionDto> findByExceptionType(FinancialException current, List<FinancialException> all) {
        return all.stream()
                .filter(ex -> ex.getExceptionType() == current.getExceptionType())
                .filter(ex -> !ex.getId().equals(current.getId()))
                .limit(2)
                .map(ex -> convertToDto(ex, RelatedExceptionDto.RelationshipType.SAME_EXCEPTION_TYPE,
                        "Type: " + ex.getExceptionType()))
                .collect(Collectors.toList());
    }

    private RelatedExceptionDto convertToDto(FinancialException exception, 
                                            RelatedExceptionDto.RelationshipType relationshipType,
                                            String reason) {
        // Convert internal UUID to Long for DTO (use hash of UUID)
        Long exceptionIdLong = exception.getId() != null ? 
                (long) exception.getId().hashCode() : null;
        
        return RelatedExceptionDto.builder()
                .exceptionId(exceptionIdLong)
                .exceptionType(exception.getExceptionType())
                .merchantId(exception.getMerchantId())
                .amount(exception.getExpectedAmount() != null ? exception.getExpectedAmount() : exception.getActualAmount())
                .createdAt(exception.getCreatedAt() != null ? 
                        exception.getCreatedAt().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime() : null)
                .relationshipType(relationshipType)
                .relationshipReason(reason)
                .build();
    }
}
