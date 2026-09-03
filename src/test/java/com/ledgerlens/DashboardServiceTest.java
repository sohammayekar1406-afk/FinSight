package com.ledgerlens;

import com.ledgerlens.dto.DashboardStatsDto;
import com.ledgerlens.entity.FinancialException;
import com.ledgerlens.entity.Payment;
import com.ledgerlens.entity.Settlement;
import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.entity.enums.PaymentStatus;
import com.ledgerlens.entity.enums.SettlementStatus;
import com.ledgerlens.repository.*;
import com.ledgerlens.service.DashboardService;
import com.ledgerlens.service.FinancialExceptionService;
import com.ledgerlens.service.MerchantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private RefundRepository refundRepository;
    @Mock private FeeRepository feeRepository;
    @Mock private SettlementRepository settlementRepository;
    @Mock private FinancialExceptionRepository exceptionRepository;
    @Mock private FinancialExceptionService exceptionService;
    @Mock private MerchantContext merchantContext;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void testGetDashboardStats() {
        when(merchantContext.merchantId()).thenReturn("merchant_a");

        // DashboardService uses findByMerchantId — stub all five list repos
        com.ledgerlens.entity.Order o1 = com.ledgerlens.entity.Order.builder().orderId("o1").merchantId("merchant_a").amount(BigDecimal.TEN).currency("INR").status(com.ledgerlens.entity.enums.OrderStatus.PAID).build();
        com.ledgerlens.entity.Order o2 = com.ledgerlens.entity.Order.builder().orderId("o2").merchantId("merchant_a").amount(BigDecimal.TEN).currency("INR").status(com.ledgerlens.entity.enums.OrderStatus.PAID).build();
        com.ledgerlens.entity.Order o3 = com.ledgerlens.entity.Order.builder().orderId("o3").merchantId("merchant_a").amount(BigDecimal.TEN).currency("INR").status(com.ledgerlens.entity.enums.OrderStatus.PAID).build();
        com.ledgerlens.entity.Order o4 = com.ledgerlens.entity.Order.builder().orderId("o4").merchantId("merchant_a").amount(BigDecimal.TEN).currency("INR").status(com.ledgerlens.entity.enums.OrderStatus.PAID).build();
        com.ledgerlens.entity.Order o5 = com.ledgerlens.entity.Order.builder().orderId("o5").merchantId("merchant_a").amount(BigDecimal.TEN).currency("INR").status(com.ledgerlens.entity.enums.OrderStatus.PAID).build();
        when(orderRepository.findByMerchantId("merchant_a")).thenReturn(List.of(o1, o2, o3, o4, o5));

        Payment p1 = Payment.builder().status(PaymentStatus.SUCCESS).build();
        Payment p2 = Payment.builder().status(PaymentStatus.FAILED).build();
        when(paymentRepository.findByMerchantId("merchant_a")).thenReturn(List.of(p1, p2));

        when(refundRepository.findByMerchantId("merchant_a")).thenReturn(List.of());
        when(feeRepository.findByMerchantId("merchant_a")).thenReturn(List.of());

        Settlement s1 = Settlement.builder().status(SettlementStatus.SETTLED).actualSettledAmount(new BigDecimal("976.40")).build();
        Settlement s2 = Settlement.builder().status(SettlementStatus.DISCREPANT).actualSettledAmount(new BigDecimal("476.40")).build();
        when(settlementRepository.findByMerchantId("merchant_a")).thenReturn(List.of(s1, s2));

        FinancialException ex1 = FinancialException.builder()
                .severity(ExceptionSeverity.MEDIUM)
                .status(ExceptionStatus.OPEN)
                .discrepancyAmount(new BigDecimal("500.00"))
                .build();
        when(exceptionRepository.findByMerchantId("merchant_a")).thenReturn(List.of(ex1));
        when(exceptionService.getAllExceptions()).thenReturn(List.of());

        DashboardStatsDto stats = dashboardService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(7L, stats.getTotalTransactions()); // 5 orders + 2 payments
        assertEquals(1L, stats.getSuccessfulPayments());
        assertEquals(2L, stats.getTotalSettlements());
        assertEquals(new BigDecimal("1452.80"), stats.getTotalSettlementsAmount());
        assertEquals(1L, stats.getOpenExceptionsCount());
        assertEquals(new BigDecimal("500.00"), stats.getUnreconciledAmount());
        assertEquals(1L, stats.getSeverityBreakdown().get("MEDIUM"));
        assertEquals(1L, stats.getSettlementOverview().get("DISCREPANT"));
    }
}
