package com.ledgerlens.service;

import com.ledgerlens.dto.DashboardStatsDto;
import com.ledgerlens.dto.FinancialExceptionResponseDto;
import com.ledgerlens.entity.*;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.entity.enums.PaymentStatus;
import com.ledgerlens.entity.enums.SettlementStatus;
import com.ledgerlens.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final FeeRepository feeRepository;
    private final SettlementRepository settlementRepository;
    private final FinancialExceptionRepository exceptionRepository;
    private final FinancialExceptionService exceptionService;
    private final MerchantContext merchantContext;

    public DashboardService(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            RefundRepository refundRepository,
            FeeRepository feeRepository,
            SettlementRepository settlementRepository,
            FinancialExceptionRepository exceptionRepository,
            FinancialExceptionService exceptionService,
            MerchantContext merchantContext) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.feeRepository = feeRepository;
        this.settlementRepository = settlementRepository;
        this.exceptionRepository = exceptionRepository;
        this.exceptionService = exceptionService;
        this.merchantContext = merchantContext;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        String merchantId = merchantContext.merchantId();
        
        List<Order> orders = orderRepository.findByMerchantId(merchantId);
        long totalOrders = orders.size();
        
        List<Payment> payments = paymentRepository.findByMerchantId(merchantId);
        long totalPaymentsCount = payments.size();
        long totalTransactions = totalOrders + totalPaymentsCount;

        long successfulPayments = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .count();

        List<Refund> refunds = refundRepository.findByMerchantId(merchantId);
        long refundsCount = refunds.size();
        BigDecimal totalRefundsAmount = refunds.stream()
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Fee> fees = feeRepository.findByMerchantId(merchantId);
        BigDecimal totalFeesAmount = fees.stream()
                .map(Fee::getTotalFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Settlement> settlements = settlementRepository.findByMerchantId(merchantId);
        long totalSettlements = settlements.size();
        BigDecimal totalSettlementsAmount = settlements.stream()
                .map(s -> s.getActualSettledAmount() != null ? s.getActualSettledAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> settlementOverview = new HashMap<>();
        settlementOverview.put("SETTLED", settlements.stream().filter(s -> s.getStatus() == SettlementStatus.SETTLED).count());
        settlementOverview.put("DISCREPANT", settlements.stream().filter(s -> s.getStatus() == SettlementStatus.DISCREPANT).count());
        settlementOverview.put("PENDING", settlements.stream().filter(s -> s.getStatus() == SettlementStatus.CREATED || s.getStatus() == SettlementStatus.INITIATED).count());

        List<FinancialException> exceptions = exceptionRepository.findByMerchantId(merchantId);

        long openExceptionsCount = exceptions.stream()
                .filter(e -> e.getStatus() == ExceptionStatus.OPEN || e.getStatus() == ExceptionStatus.INVESTIGATING)
                .count();

        BigDecimal unreconciledAmount = exceptions.stream()
                .filter(e -> e.getStatus() == ExceptionStatus.OPEN || e.getStatus() == ExceptionStatus.INVESTIGATING)
                .map(e -> e.getDiscrepancyAmount() != null ? e.getDiscrepancyAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> severityBreakdown = new HashMap<>();
        severityBreakdown.put("CRITICAL", exceptions.stream().filter(e -> e.getSeverity() != null && e.getSeverity().name().equalsIgnoreCase("CRITICAL")).count());
        severityBreakdown.put("HIGH", exceptions.stream().filter(e -> e.getSeverity() != null && e.getSeverity().name().equalsIgnoreCase("HIGH")).count());
        severityBreakdown.put("MEDIUM", exceptions.stream().filter(e -> e.getSeverity() != null && e.getSeverity().name().equalsIgnoreCase("MEDIUM")).count());
        severityBreakdown.put("LOW", exceptions.stream().filter(e -> e.getSeverity() != null && e.getSeverity().name().equalsIgnoreCase("LOW")).count());

        List<FinancialExceptionResponseDto> recentExceptions = exceptionService.getAllExceptions();

        return DashboardStatsDto.builder()
                .totalTransactions(totalTransactions)
                .successfulPayments(successfulPayments)
                .refundsCount(refundsCount)
                .totalRefundsAmount(totalRefundsAmount)
                .totalFeesAmount(totalFeesAmount)
                .totalSettlements(totalSettlements)
                .totalSettlementsAmount(totalSettlementsAmount)
                .unreconciledAmount(unreconciledAmount)
                .openExceptionsCount(openExceptionsCount)
                .severityBreakdown(severityBreakdown)
                .settlementOverview(settlementOverview)
                .recentExceptions(recentExceptions)
                .build();
    }
}
