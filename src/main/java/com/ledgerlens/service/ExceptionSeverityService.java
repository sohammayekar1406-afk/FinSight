package com.ledgerlens.service;

import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ExceptionSeverityService {

    private static final BigDecimal HUNDRED = new BigDecimal("100.00");
    private static final BigDecimal ONE_THOUSAND = new BigDecimal("1000.00");
    private static final BigDecimal TEN_THOUSAND = new BigDecimal("10000.00");

    public ExceptionSeverity calculateSeverity(ExceptionType exceptionType, BigDecimal discrepancyAmount) {
        if (discrepancyAmount != null && discrepancyAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal absAmount = discrepancyAmount.abs();
            if (absAmount.compareTo(HUNDRED) < 0) {
                return ExceptionSeverity.LOW;
            } else if (absAmount.compareTo(ONE_THOUSAND) < 0) {
                return ExceptionSeverity.MEDIUM;
            } else if (absAmount.compareTo(TEN_THOUSAND) < 0) {
                return ExceptionSeverity.HIGH;
            } else {
                return ExceptionSeverity.CRITICAL;
            }
        }

        return switch (exceptionType) {
            case MISSING_PAYMENT, MISSING_SETTLEMENT, DUPLICATE_TRANSACTION -> ExceptionSeverity.HIGH;
            case UNKNOWN_TRANSACTION -> ExceptionSeverity.CRITICAL;
            case UNEXPECTED_FEE, DISCREPANT_REFUND -> ExceptionSeverity.MEDIUM;
            default -> ExceptionSeverity.LOW;
        };
    }
}
