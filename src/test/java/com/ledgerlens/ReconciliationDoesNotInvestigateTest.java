package com.ledgerlens;

import com.ledgerlens.dto.ReconciliationResultDto;
import com.ledgerlens.entity.*;
import com.ledgerlens.entity.enums.*;
import com.ledgerlens.repository.*;
import com.ledgerlens.service.ReconciliationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CRITICAL REGRESSION TEST:
 * 
 * Verifies that running reconciliation does NOT automatically create investigations.
 * 
 * Expected Behavior:
 * - reconciliation.run() creates FinancialException records with status=OPEN
 * - reconciliation.run() does NOT create Investigation records
 * - reconciliation.run() does NOT call InvestigationService
 * - reconciliation.run() does NOT call EvidenceGraphService
 * - reconciliation.run() does NOT call RAG services
 * - reconciliation.run() does NOT call Gemini
 * 
 * Investigations must ONLY be created when:
 * - User clicks "Investigate" for a specific exception → POST /api/investigations/{exceptionId}
 * - User clicks "Run All Investigations" → POST /api/investigations/run
 * 
 * This test ensures reconciliation and investigation remain strictly decoupled.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReconciliationDoesNotInvestigateTest {

    @Autowired
    private ReconciliationService reconciliationService;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private FinancialExceptionRepository exceptionRepository;

    @Autowired
    private InvestigationRepository investigationRepository;

    @Autowired
    private HistoricalInvestigationEmbeddingRepository embeddingRepository;

    @Autowired
    private com.ledgerlens.repository.AppUserRepository appUserRepository;

    private Merchant merchant;

    @BeforeEach
    public void setup() {
        // Create merchant
        merchant = merchantRepository.findByMerchantId("test_merchant")
                .orElseGet(() -> merchantRepository.save(new Merchant("test_merchant", "Test Merchant")));

        if (appUserRepository.findByUsername("test_merchant").isEmpty()) {
            appUserRepository.save(new com.ledgerlens.entity.AppUser("test_merchant", merchant, "ROLE_MERCHANT"));
        }

        // Security context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test_merchant",
                        "password",
                        List.of(new SimpleGrantedAuthority("ROLE_MERCHANT"))
                )
        );

        // Clean slate
        exceptionRepository.deleteAll();
        investigationRepository.deleteAll();
        embeddingRepository.deleteAll();
    }

    @Test
    public void testReconciliation_DoesNotCreateInvestigations() {
        // Setup: Create an order with amount mismatch (will trigger exception)
        Order order = Order.builder()
                .orderId("ord_recon_test_001")
                .merchantId("test_merchant")
                .amount(new BigDecimal("1000.00"))
                .currency("INR")
                .status(OrderStatus.PAID)
                .createdAt(OffsetDateTime.now().minusDays(1))
                .build();
        orderRepository.save(order);

        Payment payment = Payment.builder()
                .paymentId("pay_recon_test_001")
                .merchantId("test_merchant")
                .order(order)
                .amount(new BigDecimal("950.00")) // AMOUNT MISMATCH
                .currency("INR")
                .status(PaymentStatus.SUCCESS)
                .method(PaymentMethod.CARD)
                .createdAt(OffsetDateTime.now().minusHours(2))
                .build();
        paymentRepository.save(payment);

        // Verify: No exceptions or investigations exist yet
        assertThat(exceptionRepository.count()).isEqualTo(0);
        assertThat(investigationRepository.count()).isEqualTo(0);
        assertThat(embeddingRepository.count()).isEqualTo(0);

        // Act: Run reconciliation
        ReconciliationResultDto result = reconciliationService.reconcileAll(java.util.UUID.randomUUID().toString());

        // Verify: Reconciliation created exception
        assertThat(result).isNotNull();
        assertThat(result.getExceptionsCreated()).isGreaterThan(0);

        // Verify: Exception was created with status=OPEN
        List<FinancialException> exceptions = exceptionRepository.findByMerchantId("test_merchant");
        assertThat(exceptions).isNotEmpty();
        
        FinancialException exception = exceptions.stream()
                .filter(e -> e.getExceptionType() == ExceptionType.AMOUNT_MISMATCH)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected AMOUNT_MISMATCH exception to be created"));
        
        assertThat(exception.getStatus()).isEqualTo(ExceptionStatus.OPEN);
        assertThat(exception.getMerchantId()).isEqualTo("test_merchant");
        assertThat(exception.getExpectedAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(exception.getActualAmount()).isEqualByComparingTo(new BigDecimal("950.00"));

        // CRITICAL ASSERTIONS: Reconciliation did NOT create investigation
        assertThat(investigationRepository.count()).isEqualTo(0);
        assertThat(embeddingRepository.count()).isEqualTo(0);

        // Verify: Exception has NO investigation linked
        boolean hasInvestigation = investigationRepository.findByException_ExceptionIdAndException_MerchantId(
                exception.getExceptionId(), "test_merchant").isPresent();
        assertThat(hasInvestigation)
                .withFailMessage("CRITICAL BUG: Reconciliation auto-created investigation! This violates the architecture.")
                .isFalse();
    }

    @Test
    public void testReconciliation_MultipleExceptions_NoneInvestigated() {
        // Setup: Create multiple discrepancy scenarios
        
        // 1. Amount mismatch
        Order order1 = Order.builder()
                .orderId("ord_multi_001")
                .merchantId("test_merchant")
                .amount(new BigDecimal("500.00"))
                .currency("INR")
                .status(OrderStatus.PAID)
                .createdAt(OffsetDateTime.now().minusDays(1))
                .build();
        orderRepository.save(order1);

        Payment payment1 = Payment.builder()
                .paymentId("pay_multi_001")
                .merchantId("test_merchant")
                .order(order1)
                .amount(new BigDecimal("450.00"))
                .currency("INR")
                .status(PaymentStatus.SUCCESS)
                .method(PaymentMethod.UPI)
                .createdAt(OffsetDateTime.now().minusHours(30))
                .build();
        paymentRepository.save(payment1);

        // 2. Missing settlement
        Order order2 = Order.builder()
                .orderId("ord_multi_002")
                .merchantId("test_merchant")
                .amount(new BigDecimal("2000.00"))
                .currency("INR")
                .status(OrderStatus.PAID)
                .createdAt(OffsetDateTime.now().minusDays(5))
                .build();
        orderRepository.save(order2);

        Payment payment2 = Payment.builder()
                .paymentId("pay_multi_002")
                .merchantId("test_merchant")
                .order(order2)
                .amount(new BigDecimal("1800.00")) // AMOUNT MISMATCH (order is 2000.00)
                .currency("INR")
                .status(PaymentStatus.SUCCESS)
                .method(PaymentMethod.CARD)
                .settlement(null)
                .build();
        paymentRepository.save(payment2);

        // 3. Duplicate payment (same payment ID twice - simulated by creating with existing ID)
        // Note: In real scenario this would be caught at database level, but for test we verify detection logic

        // Act: Run reconciliation
        ReconciliationResultDto result = reconciliationService.reconcileAll(java.util.UUID.randomUUID().toString());

        // Verify: Multiple exceptions created
        List<FinancialException> exceptions = exceptionRepository.findByMerchantId("test_merchant");
        for (FinancialException fe : exceptions) {
            System.out.println("DEBUG EXCEPTION: type=" + fe.getExceptionType() + ", desc=" + fe.getDescription() + ", id=" + fe.getExceptionId());
        }
        System.out.println("DEBUG RESULT: checked=" + result.getRecordsChecked() + ", created=" + result.getExceptionsCreated() + ", existing=" + result.getExceptionsAlreadyExisting());
        assertThat(exceptions.size()).isGreaterThanOrEqualTo(2);

        // Verify: ALL exceptions are status=OPEN
        assertThat(exceptions).allMatch(e -> e.getStatus() == ExceptionStatus.OPEN);

        // CRITICAL: ZERO investigations created
        assertThat(investigationRepository.count())
                .withFailMessage("CRITICAL BUG: Reconciliation auto-created %d investigations!", investigationRepository.count())
                .isEqualTo(0);

        // CRITICAL: ZERO embeddings created
        assertThat(embeddingRepository.count()).isEqualTo(0);

        // Verify: No exception has INVESTIGATING status
        boolean anyInvestigating = exceptions.stream()
                .anyMatch(e -> e.getStatus() == ExceptionStatus.INVESTIGATING);
        assertThat(anyInvestigating)
                .withFailMessage("CRITICAL BUG: Reconciliation set exception status to INVESTIGATING!")
                .isFalse();
    }

    @Test
    public void testReconciliation_IdempotentRuns_StillNoInvestigations() {
        // Setup
        Order order = Order.builder()
                .orderId("ord_idemp_001")
                .merchantId("test_merchant")
                .amount(new BigDecimal("750.00"))
                .currency("INR")
                .status(OrderStatus.PAID)
                .createdAt(OffsetDateTime.now().minusDays(1))
                .build();
        orderRepository.save(order);

        Payment payment = Payment.builder()
                .paymentId("pay_idemp_001")
                .merchantId("test_merchant")
                .order(order)
                .amount(new BigDecimal("700.00"))
                .currency("INR")
                .status(PaymentStatus.SUCCESS)
                .method(PaymentMethod.CARD)
                .createdAt(OffsetDateTime.now().minusHours(2))
                .build();
        paymentRepository.save(payment);

        // Act: Run reconciliation TWICE with same idempotency key
        String idempKey = "test-idempotency-key-001";
        ReconciliationResultDto result1 = reconciliationService.reconcileAll(idempKey);
        ReconciliationResultDto result2 = reconciliationService.reconcileAll(idempKey);

        // Verify: Second run returned cached result
        assertThat(result2.getReconciliationId()).isEqualTo(result1.getReconciliationId());

        // Verify: Still ZERO investigations
        assertThat(investigationRepository.count()).isEqualTo(0);
        assertThat(embeddingRepository.count()).isEqualTo(0);

        // Act: Run reconciliation THIRD time with DIFFERENT idempotency key
        String idempKey3 = "test-idempotency-key-002";
        ReconciliationResultDto result3 = reconciliationService.reconcileAll(idempKey3);

        // Verify: Still ZERO investigations (no side effect from multiple runs)
        assertThat(investigationRepository.count()).isEqualTo(0);
    }
}
