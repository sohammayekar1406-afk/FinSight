package com.ledgerlens;

import com.ledgerlens.dto.SeedResponseDto;
import com.ledgerlens.repository.*;
import com.ledgerlens.service.SeedDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class CleanSeedReproductionTest {

    @Autowired
    private SeedDataService seedDataService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private FeeRepository feeRepository;

    @Autowired
    private AdjustmentRepository adjustmentRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private FinancialExceptionRepository exceptionRepository;

    @Autowired
    private InvestigationRepository investigationRepository;

    @Autowired
    private HistoricalInvestigationEmbeddingRepository embeddingRepository;

    @Autowired
    private com.ledgerlens.service.ReconciliationService reconciliationService;

    @Autowired
    private com.ledgerlens.service.InvestigationService investigationService;

    @Autowired
    private com.ledgerlens.service.DashboardService dashboardService;

    @Test
    void testCleanSeedReproduction() {
        System.out.println("=== CLEAN SEED REPRODUCTION START ===");
        System.out.println("BEFORE SEED:");
        System.out.println("Orders: " + orderRepository.count());
        System.out.println("Payments: " + paymentRepository.count());
        System.out.println("Refunds: " + refundRepository.count());
        System.out.println("Fees: " + feeRepository.count());
        System.out.println("Adjustments: " + adjustmentRepository.count());
        System.out.println("Settlements: " + settlementRepository.count());
        System.out.println("FinancialExceptions: " + exceptionRepository.count());
        System.out.println("Investigations: " + investigationRepository.count());
        System.out.println("Embeddings: " + embeddingRepository.count());

        // Perform ONLY seedDemoData
        SeedResponseDto seedResponse = seedDataService.seedDemoData();
        System.out.println("SEED RESPONSE: " + seedResponse.getMessage());

        System.out.println("AFTER SEED ONLY:");
        System.out.println("Orders: " + orderRepository.count());
        System.out.println("Payments: " + paymentRepository.count());
        System.out.println("Refunds: " + refundRepository.count());
        System.out.println("Fees: " + feeRepository.count());
        System.out.println("Adjustments: " + adjustmentRepository.count());
        System.out.println("Settlements: " + settlementRepository.count());
        System.out.println("FinancialExceptions: " + exceptionRepository.count());
        System.out.println("Investigations: " + investigationRepository.count());
        System.out.println("Embeddings: " + embeddingRepository.count());

        assertThat(exceptionRepository.findByMerchantId("merchant_a")).isEmpty();
        assertThat(investigationRepository.findByException_MerchantId("merchant_a")).isEmpty();
        assertThat(embeddingRepository.findByMerchantId("merchant_a")).isEmpty();

        System.out.println("=== CLEAN SEED REPRODUCTION END ===");
    }

    @Test
    void seedOnly_transactionDetail_hasNoCurrentInvestigation() {
        // Set security context to admin for merchant_a
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "admin", "N/A", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        // 1. Seed demo data
        SeedResponseDto seedResponse = seedDataService.seedDemoData();
        assertThat(seedResponse).isNotNull();

        // 2. Transactions exist
        assertThat(orderRepository.count()).isGreaterThanOrEqualTo(80);
        assertThat(paymentRepository.count()).isGreaterThanOrEqualTo(80);

        // 3. Prior to reconciliation, ZERO exceptions and ZERO investigations exist
        assertThat(exceptionRepository.findByMerchantId("merchant_a")).isEmpty();
        assertThat(investigationRepository.findByException_MerchantId("merchant_a")).isEmpty();
        assertThat(embeddingRepository.findByMerchantId("merchant_a")).isEmpty();

        // 4. Any attempt to query investigation for non-existent exception yields not found
        org.junit.jupiter.api.Assertions.assertThrows(com.ledgerlens.exception.ResourceNotFoundException.class, () -> {
            investigationService.getInvestigation("exp_non_existent");
        });
        org.junit.jupiter.api.Assertions.assertThrows(com.ledgerlens.exception.ResourceNotFoundException.class, () -> {
            investigationService.getInvestigation("pay_1001");
        });

        // 5. Run reconciliation - exceptions are created with OPEN status
        com.ledgerlens.dto.ReconciliationResultDto reconResult = reconciliationService.reconcileAll();
        assertThat(reconResult.getExceptionsCreated()).isGreaterThan(0);

        java.util.List<com.ledgerlens.entity.FinancialException> openExceptions = exceptionRepository.findByMerchantId("merchant_a");
        assertThat(openExceptions).isNotEmpty();
        for (com.ledgerlens.entity.FinancialException ex : openExceptions) {
            assertThat(ex.getStatus()).isEqualTo(com.ledgerlens.entity.enums.ExceptionStatus.OPEN);
            // Verify NO investigation exists for this exception yet!
            org.junit.jupiter.api.Assertions.assertThrows(com.ledgerlens.exception.ResourceNotFoundException.class, () -> {
                investigationService.getInvestigation(ex.getExceptionId());
            });
        }
        assertThat(investigationRepository.findByException_MerchantId("merchant_a")).isEmpty();

        // 6. Explicitly investigate ONLY the first exception
        com.ledgerlens.entity.FinancialException targetException = openExceptions.get(0);
        com.ledgerlens.dto.InvestigationResponseDto invResponse = investigationService.investigateException(targetException.getExceptionId());
        assertThat(invResponse).isNotNull();
        assertThat(invResponse.getInvestigationId()).isNotNull();

        // 7. Now target exception has an investigation, but others STILL DO NOT
        com.ledgerlens.dto.InvestigationResponseDto fetched = investigationService.getInvestigation(targetException.getExceptionId());
        assertThat(fetched.getInvestigationId()).isEqualTo(invResponse.getInvestigationId());

        if (openExceptions.size() > 1) {
            com.ledgerlens.entity.FinancialException secondException = openExceptions.get(1);
            org.junit.jupiter.api.Assertions.assertThrows(com.ledgerlens.exception.ResourceNotFoundException.class, () -> {
                investigationService.getInvestigation(secondException.getExceptionId());
            });
        }
    }

    @Test
    void testCleanState_AndLifecycleTransitions() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "admin", "N/A", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        // ==========================================
        // STATE 1 — CLEAN DATABASE (0 DEMO RECORDS)
        // ==========================================
        seedDataService.clearDemoData();

        assertThat(orderRepository.findByMerchantId("merchant_a")).isEmpty();
        assertThat(paymentRepository.findByMerchantId("merchant_a")).isEmpty();
        assertThat(refundRepository.findByMerchantId("merchant_a")).isEmpty();
        assertThat(feeRepository.findByMerchantId("merchant_a")).isEmpty();
        assertThat(adjustmentRepository.findByMerchantId("merchant_a")).isEmpty();
        assertThat(settlementRepository.findByMerchantId("merchant_a")).isEmpty();
        assertThat(exceptionRepository.findByMerchantId("merchant_a")).isEmpty();
        assertThat(investigationRepository.findByException_MerchantId("merchant_a")).isEmpty();
        assertThat(embeddingRepository.findByMerchantId("merchant_a")).isEmpty();

        com.ledgerlens.dto.DashboardStatsDto cleanStats = dashboardService.getDashboardStats();
        assertThat(cleanStats.getTotalTransactions()).isEqualTo(0);
        assertThat(cleanStats.getSuccessfulPayments()).isEqualTo(0);
        assertThat(cleanStats.getTotalSettlements()).isEqualTo(0);
        assertThat(cleanStats.getTotalSettlementsAmount()).isEqualByComparingTo(java.math.BigDecimal.ZERO);
        assertThat(cleanStats.getUnreconciledAmount()).isEqualByComparingTo(java.math.BigDecimal.ZERO);
        assertThat(cleanStats.getOpenExceptionsCount()).isEqualTo(0);
        assertThat(cleanStats.isHasReconciled()).isFalse();

        // ==========================================
        // STATE 2 — AFTER "SEED DATA" ONLY
        // ==========================================
        seedDataService.seedDemoData();

        assertThat(orderRepository.findByMerchantId("merchant_a")).isNotEmpty();
        assertThat(paymentRepository.findByMerchantId("merchant_a")).isNotEmpty();
        assertThat(settlementRepository.findByMerchantId("merchant_a")).isNotEmpty();
        assertThat(refundRepository.findByMerchantId("merchant_a")).isNotEmpty();
        assertThat(feeRepository.findByMerchantId("merchant_a")).isNotEmpty();

        // Raw records exist, BUT 0 exceptions and 0 investigations!
        assertThat(exceptionRepository.findByMerchantId("merchant_a")).isEmpty();
        assertThat(investigationRepository.findByException_MerchantId("merchant_a")).isEmpty();
        assertThat(embeddingRepository.findByMerchantId("merchant_a")).isEmpty();

        com.ledgerlens.dto.DashboardStatsDto seededStats = dashboardService.getDashboardStats();
        assertThat(seededStats.getTotalTransactions()).isGreaterThan(0);
        assertThat(seededStats.getSuccessfulPayments()).isGreaterThan(0);
        assertThat(seededStats.getTotalSettlements()).isGreaterThan(0);
        assertThat(seededStats.getTotalSettlementsAmount()).isGreaterThan(java.math.BigDecimal.ZERO);
        assertThat(seededStats.getOpenExceptionsCount()).isEqualTo(0);
        assertThat(seededStats.getUnreconciledAmount()).isEqualByComparingTo(java.math.BigDecimal.ZERO);
        assertThat(seededStats.isHasReconciled()).isFalse(); // MUST NOT claim reconciliation has run

        // ==========================================
        // STATE 3 — AFTER "RUN RECONCILIATION"
        // ==========================================
        com.ledgerlens.dto.ReconciliationResultDto reconResult = reconciliationService.reconcileAll();
        assertThat(reconResult.getExceptionsCreated()).isGreaterThan(0);

        java.util.List<com.ledgerlens.entity.FinancialException> exceptions = exceptionRepository.findByMerchantId("merchant_a");
        assertThat(exceptions).isNotEmpty();
        // Investigations must still be 0!
        assertThat(investigationRepository.findByException_MerchantId("merchant_a")).isEmpty();

        com.ledgerlens.dto.DashboardStatsDto reconStats = dashboardService.getDashboardStats();
        assertThat(reconStats.isHasReconciled()).isTrue();
        assertThat(reconStats.getOpenExceptionsCount()).isEqualTo(exceptions.size());
        assertThat(reconStats.getUnreconciledAmount()).isGreaterThan(java.math.BigDecimal.ZERO);
        // Settlements amount must match raw settlements dynamically
        assertThat(reconStats.getTotalSettlementsAmount()).isEqualTo(seededStats.getTotalSettlementsAmount());

        // ==========================================
        // STATE 4 — AFTER EXPLICIT INVESTIGATION
        // ==========================================
        String exId = exceptions.get(0).getExceptionId();
        com.ledgerlens.dto.InvestigationResponseDto invDto = investigationService.investigateException(exId);
        assertThat(invDto).isNotNull();
        assertThat(investigationRepository.findByException_MerchantId("merchant_a")).hasSize(1);

        // ==========================================
        // RESET BACK TO CLEAN STATE
        // ==========================================
        seedDataService.clearDemoData();
        assertThat(orderRepository.findByMerchantId("merchant_a")).isEmpty();
        assertThat(settlementRepository.findByMerchantId("merchant_a")).isEmpty();
        assertThat(exceptionRepository.findByMerchantId("merchant_a")).isEmpty();
        assertThat(investigationRepository.findByException_MerchantId("merchant_a")).isEmpty();
        assertThat(dashboardService.getDashboardStats().isHasReconciled()).isFalse();
    }
}
