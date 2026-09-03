package com.ledgerlens;

import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.service.ExceptionSeverityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionSeverityServiceTest {

    private ExceptionSeverityService severityService;

    @BeforeEach
    void setUp() {
        severityService = new ExceptionSeverityService();
    }

    @Test
    void testLowSeverity() {
        ExceptionSeverity severity = severityService.calculateSeverity(ExceptionType.AMOUNT_MISMATCH, new BigDecimal("50.00"));
        assertEquals(ExceptionSeverity.LOW, severity);
    }

    @Test
    void testMediumSeverity() {
        ExceptionSeverity severity = severityService.calculateSeverity(ExceptionType.AMOUNT_MISMATCH, new BigDecimal("500.00"));
        assertEquals(ExceptionSeverity.MEDIUM, severity);
    }

    @Test
    void testHighSeverity() {
        ExceptionSeverity severity = severityService.calculateSeverity(ExceptionType.AMOUNT_MISMATCH, new BigDecimal("5000.00"));
        assertEquals(ExceptionSeverity.HIGH, severity);
    }

    @Test
    void testCriticalSeverity() {
        ExceptionSeverity severity = severityService.calculateSeverity(ExceptionType.AMOUNT_MISMATCH, new BigDecimal("15000.00"));
        assertEquals(ExceptionSeverity.CRITICAL, severity);
    }

    @Test
    void testNonMonetaryDefaults() {
        ExceptionSeverity missingSettlement = severityService.calculateSeverity(ExceptionType.MISSING_SETTLEMENT, null);
        assertEquals(ExceptionSeverity.HIGH, missingSettlement);

        ExceptionSeverity unknownTx = severityService.calculateSeverity(ExceptionType.UNKNOWN_TRANSACTION, null);
        assertEquals(ExceptionSeverity.CRITICAL, unknownTx);
    }
}
