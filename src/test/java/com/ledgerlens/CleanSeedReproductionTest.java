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

        // 8. Re-seed demo data - guarantees reset of all exceptions and investigations
        seedDataService.seedDemoData();
        assertThat(exceptionRepository.findByMerchantId("merchant_a")).isEmpty();
        assertThat(investigationRepository.findByException_MerchantId("merchant_a")).isEmpty();
        assertThat(embeddingRepository.findByMerchantId("merchant_a")).isEmpty();
    }
}
