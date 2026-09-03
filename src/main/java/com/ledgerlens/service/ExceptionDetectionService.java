package com.ledgerlens.service;

import com.ledgerlens.entity.*;
import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.repository.FinancialExceptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

@Service
public class ExceptionDetectionService {

    private final FinancialExceptionRepository exceptionRepository;
    private final ExceptionSeverityService severityService;

    public ExceptionDetectionService(FinancialExceptionRepository exceptionRepository, ExceptionSeverityService severityService) {
        this.exceptionRepository = exceptionRepository;
        this.severityService = severityService;
    }

    @Transactional
    public boolean detectAndCreateException(
            String merchantId,
            ExceptionType type,
            Order order,
            Payment payment,
            Refund refund,
            Settlement settlement,
            BigDecimal expectedAmount,
            BigDecimal actualAmount,
            BigDecimal discrepancyAmount,
            String description) {

        // The pre-check avoids unnecessary writes; the unique key below is the concurrency guarantee.
        boolean alreadyExists = exceptionRepository.existsByExceptionTypeAndStatusNotAndStatusNotAndOrderAndPaymentAndRefundAndSettlement(
                type, ExceptionStatus.RESOLVED_AUTO, ExceptionStatus.RESOLVED_MANUAL, order, payment, refund, settlement
        );

        if (alreadyExists) {
            return false;
        }

        ExceptionSeverity severity = severityService.calculateSeverity(type, discrepancyAmount);
        String generatedExceptionId = "exp_" + type.name().toLowerCase() + "_" + UUID.randomUUID().toString().substring(0, 8);

        FinancialException financialException = FinancialException.builder()
                .exceptionId(generatedExceptionId)
                .merchantId(merchantId != null ? merchantId : "UNKNOWN")
                .exceptionType(type)
                .severity(severity)
                .status(ExceptionStatus.OPEN)
                .expectedAmount(expectedAmount)
                .actualAmount(actualAmount)
                .discrepancyAmount(discrepancyAmount)
                .order(order)
                .payment(payment)
                .refund(refund)
                .settlement(settlement)
                .description(description)
                .build();
        financialException.setDeduplicationKey(deduplicationKey(type, order, payment, refund, settlement));

        try {
            exceptionRepository.saveAndFlush(financialException);
            return true;
        } catch (org.springframework.dao.DataIntegrityViolationException duplicate) {
            // The unique database index won the race. The finding already exists.
            return false;
        }
    }

    private boolean matchesEntity(Object exEntity, Object currentEntity) {
        if (exEntity == null && currentEntity == null) return true;
        if (exEntity == null || currentEntity == null) return false;
        if (exEntity instanceof Order o1 && currentEntity instanceof Order o2) {
            return o1.getId().equals(o2.getId());
        }
        if (exEntity instanceof Payment p1 && currentEntity instanceof Payment p2) {
            return p1.getId().equals(p2.getId());
        }
        if (exEntity instanceof Refund r1 && currentEntity instanceof Refund r2) {
            return r1.getId().equals(r2.getId());
        }
        if (exEntity instanceof Settlement s1 && currentEntity instanceof Settlement s2) {
            return s1.getId().equals(s2.getId());
        }
        return false;
    }

    private String deduplicationKey(ExceptionType type, Order order, Payment payment, Refund refund, Settlement settlement) {
        String context = type.name() + "|" + entityKey(order) + "|" + entityKey(payment) + "|"
                + entityKey(refund) + "|" + entityKey(settlement);
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(context.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private String entityKey(Object entity) {
        if (entity instanceof Order value) return String.valueOf(value.getId());
        if (entity instanceof Payment value) return String.valueOf(value.getId());
        if (entity instanceof Refund value) return String.valueOf(value.getId());
        if (entity instanceof Settlement value) return String.valueOf(value.getId());
        return "-";
    }
}
