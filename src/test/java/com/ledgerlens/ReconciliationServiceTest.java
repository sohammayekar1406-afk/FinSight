package com.ledgerlens;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgerlens.dto.ReconciliationResultDto;
import com.ledgerlens.entity.*;
import com.ledgerlens.entity.enums.*;
import com.ledgerlens.repository.*;
import com.ledgerlens.service.ExceptionDetectionService;
import com.ledgerlens.service.MerchantContext;
import com.ledgerlens.service.ReconciliationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private RefundRepository refundRepository;
    @Mock private FeeRepository feeRepository;
    @Mock private AdjustmentRepository adjustmentRepository;
    @Mock private SettlementRepository settlementRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private ExceptionDetectionService exceptionDetectionService;
    @Mock private ReconciliationExecutionLockRepository reconciliationExecutionLockRepository;
    @Mock private ReconciliationRunRepository reconciliationRunRepository;
    @Mock private ObjectMapper objectMapper;
    @Mock private MerchantContext merchantContext;
    @Mock private MerchantSettingsRepository merchantSettingsRepository;

    @InjectMocks
    private ReconciliationService reconciliationService;

    private Order sampleOrder;
    private Payment samplePayment;
    private Settlement sampleSettlement;

    @BeforeEach
    void setUp() {
        when(merchantContext.merchantId()).thenReturn("merch_t");
        when(reconciliationExecutionLockRepository.findByIdForUpdate(anyString()))
                .thenReturn(java.util.Optional.of(new ReconciliationExecutionLock("MERCHANT:merch_t")));
        when(reconciliationRunRepository.findByIdempotencyKey(anyString())).thenReturn(java.util.Optional.empty());

        sampleOrder = Order.builder()
                .id(UUID.randomUUID())
                .orderId("ord_t1")
                .merchantId("merch_t")
                .amount(new BigDecimal("1000.00"))
                .currency("INR")
                .status(OrderStatus.PAID)
                .build();

        sampleSettlement = Settlement.builder()
                .id(UUID.randomUUID())
                .settlementId("set_t1")
                .merchantId("merch_t")
                .grossAmount(new BigDecimal("1000.00"))
                .totalRefundAmount(BigDecimal.ZERO)
                .totalFeeAmount(new BigDecimal("20.00"))
                .totalTaxAmount(new BigDecimal("3.60"))
                .totalAdjustmentAmount(BigDecimal.ZERO)
                .netAmount(new BigDecimal("976.40"))
                .actualSettledAmount(new BigDecimal("976.40"))
                .status(SettlementStatus.SETTLED)
                .build();

        samplePayment = Payment.builder()
                .id(UUID.randomUUID())
                .paymentId("pay_t1")
                .order(sampleOrder)
                .merchantId("merch_t")
                .method(PaymentMethod.CARD)
                .amount(new BigDecimal("1000.00"))
                .currency("INR")
                .status(PaymentStatus.SUCCESS)
                .settlement(sampleSettlement)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void testReconcileAllNormalTransaction() {
        when(orderRepository.findByMerchantId("merch_t")).thenReturn(List.of(sampleOrder));
        when(paymentRepository.findByMerchantId("merch_t")).thenReturn(List.of(samplePayment));
        when(paymentRepository.findByOrder_OrderId("ord_t1")).thenReturn(List.of(samplePayment));
        when(refundRepository.findByPayment_PaymentId("pay_t1")).thenReturn(Collections.emptyList());
        when(feeRepository.findByMerchantId("merch_t")).thenReturn(Collections.emptyList());
        when(settlementRepository.findByMerchantId("merch_t")).thenReturn(List.of(sampleSettlement));
        when(adjustmentRepository.findByMerchantId("merch_t")).thenReturn(Collections.emptyList());

        ReconciliationResultDto result = reconciliationService.reconcileAll("idempotency-key-1");

        assertNotNull(result);
        assertEquals(0, result.getExceptionsCreated());
        assertEquals(0, result.getFailedChecks());
        assertTrue(result.getSuccessfulChecks() >= 2);
    }

    @Test
    void testPaymentAmountMismatch() {
        samplePayment.setAmount(new BigDecimal("900.00")); // Mismatch vs order 1000.00

        when(orderRepository.findByMerchantId("merch_t")).thenReturn(List.of(sampleOrder));
        when(paymentRepository.findByMerchantId("merch_t")).thenReturn(List.of(samplePayment));
        when(paymentRepository.findByOrder_OrderId("ord_t1")).thenReturn(List.of(samplePayment));
        when(refundRepository.findByPayment_PaymentId("pay_t1")).thenReturn(Collections.emptyList());
        when(feeRepository.findByMerchantId("merch_t")).thenReturn(Collections.emptyList());
        when(settlementRepository.findByMerchantId("merch_t")).thenReturn(List.of(sampleSettlement));
        when(adjustmentRepository.findByMerchantId("merch_t")).thenReturn(Collections.emptyList());

        when(exceptionDetectionService.detectAndCreateException(any(), eq(ExceptionType.AMOUNT_MISMATCH), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        ReconciliationResultDto result = reconciliationService.reconcileAll("idempotency-key-2");

        assertEquals(1, result.getExceptionsCreated());
        assertEquals(new BigDecimal("100.00"), result.getTotalDiscrepancyAmount());
    }

    @Test
    void testSettlementMismatchUpdatesStatus() {
        sampleSettlement.setActualSettledAmount(new BigDecimal("476.40")); // Expected 976.40

        when(orderRepository.findByMerchantId("merch_t")).thenReturn(Collections.emptyList());
        when(paymentRepository.findByMerchantId("merch_t")).thenReturn(Collections.emptyList());
        when(feeRepository.findByMerchantId("merch_t")).thenReturn(Collections.emptyList());
        when(settlementRepository.findByMerchantId("merch_t")).thenReturn(List.of(sampleSettlement));
        when(adjustmentRepository.findByMerchantId("merch_t")).thenReturn(Collections.emptyList());

        when(exceptionDetectionService.detectAndCreateException(any(), eq(ExceptionType.AMOUNT_MISMATCH), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        ReconciliationResultDto result = reconciliationService.reconcileAll("idempotency-key-3");

        assertEquals(1, result.getExceptionsCreated());
        assertEquals(new BigDecimal("500.00"), result.getTotalDiscrepancyAmount());
        verify(settlementRepository).save(argThat(s -> s.getStatus() == SettlementStatus.DISCREPANT));
    }

    @Test
    void malformedPaymentIsRecordedAsDataIncompleteInsteadOfAbortingTheRun() {
        samplePayment.setAmount(null);
        when(orderRepository.findByMerchantId("merch_t")).thenReturn(Collections.emptyList());
        when(paymentRepository.findByMerchantId("merch_t")).thenReturn(List.of(samplePayment));
        when(feeRepository.findByMerchantId("merch_t")).thenReturn(Collections.emptyList());
        when(settlementRepository.findByMerchantId("merch_t")).thenReturn(Collections.emptyList());
        when(adjustmentRepository.findByMerchantId("merch_t")).thenReturn(Collections.emptyList());
        when(exceptionDetectionService.detectAndCreateException(any(), eq(ExceptionType.DATA_INCOMPLETE), any(), any(), any(), any(), isNull(), isNull(), any(), any()))
                .thenReturn(true);

        ReconciliationResultDto result = reconciliationService.reconcileAll("idempotency-key-4");

        assertEquals(1, result.getExceptionsCreated());
        assertEquals(1, result.getFailedChecks());
        assertEquals(ExceptionType.DATA_INCOMPLETE, result.getItems().getFirst().getExceptionType());
    }
}
