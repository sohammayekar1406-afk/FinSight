package com.ledgerlens.service;

import com.ledgerlens.dto.ReconciliationItemResultDto;
import com.ledgerlens.dto.ReconciliationResultDto;
import com.ledgerlens.entity.*;
import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.entity.enums.OrderStatus;
import com.ledgerlens.entity.enums.PaymentStatus;
import com.ledgerlens.entity.enums.SettlementStatus;
import com.ledgerlens.exception.ResourceNotFoundException;
import com.ledgerlens.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgerlens.entity.ReconciliationExecutionLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReconciliationService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final FeeRepository feeRepository;
    private final AdjustmentRepository adjustmentRepository;
    private final SettlementRepository settlementRepository;
    private final AuditLogRepository auditLogRepository;
    private final ExceptionDetectionService exceptionDetectionService;
    private final ReconciliationExecutionLockRepository reconciliationExecutionLockRepository;
    private final ReconciliationRunRepository reconciliationRunRepository;
    private final ObjectMapper objectMapper;
    private final MerchantContext merchantContext;
    private final MerchantSettingsRepository merchantSettingsRepository;
    private final ReconciliationLockService reconciliationLockService;

    @Value("${finsight.reconciliation.settlement-delay-hours:24}")
    private int settlementDelayHours;

    public ReconciliationService(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            RefundRepository refundRepository,
            FeeRepository feeRepository,
            AdjustmentRepository adjustmentRepository,
            SettlementRepository settlementRepository,
            AuditLogRepository auditLogRepository,
            ExceptionDetectionService exceptionDetectionService,
            ReconciliationExecutionLockRepository reconciliationExecutionLockRepository,
            ReconciliationRunRepository reconciliationRunRepository,
            ObjectMapper objectMapper,
            MerchantContext merchantContext,
            MerchantSettingsRepository merchantSettingsRepository,
            ReconciliationLockService reconciliationLockService) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.feeRepository = feeRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.settlementRepository = settlementRepository;
        this.auditLogRepository = auditLogRepository;
        this.exceptionDetectionService = exceptionDetectionService;
        this.reconciliationExecutionLockRepository = reconciliationExecutionLockRepository;
        this.reconciliationRunRepository = reconciliationRunRepository;
        this.objectMapper = objectMapper;
        this.merchantContext = merchantContext;
        this.merchantSettingsRepository = merchantSettingsRepository;
        this.reconciliationLockService = reconciliationLockService;
    }

    @Transactional
    public ReconciliationResultDto reconcileAll() {
        return runReconciliation(null);
    }

    /**
     * Runs one global reconciliation operation. Supplying the same key returns the persisted
     * original response, while the row-level lock serializes all global runs across nodes.
     */
    @Transactional
    public ReconciliationResultDto reconcileAll(String idempotencyKey) {
        return reconcileAllForMerchant(idempotencyKey, merchantContext.merchantId());
    }

    @Transactional
    public ReconciliationResultDto reconcileAllForMerchant(String idempotencyKey, String merchantId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return runReconciliation(merchantId);
        }
        if (idempotencyKey.length() > 128) {
            throw new IllegalArgumentException("Idempotency-Key must be at most 128 characters");
        }

        // Ensure lock row exists before acquiring pessimistic lock
        String lockId = "MERCHANT:" + merchantId;
        reconciliationLockService.ensureLockExists(lockId);

        // Acquire pessimistic lock with self-healing fallback
        ReconciliationExecutionLock lock = reconciliationExecutionLockRepository.findByIdForUpdate(lockId)
                .orElseGet(() -> {
                    reconciliationLockService.ensureLockExists(lockId);
                    return reconciliationExecutionLockRepository.findByIdForUpdate(lockId)
                            .orElseGet(() -> new ReconciliationExecutionLock(lockId));
                });

        String scopedKey = merchantId + ":" + idempotencyKey;
        Optional<ReconciliationRun> previous = reconciliationRunRepository.findByIdempotencyKey(scopedKey);
        if (previous.isPresent()) {
            return deserializeResult(previous.get().getResultPayload());
        }

        ReconciliationResultDto result = runReconciliation(merchantId);
        reconciliationRunRepository.save(new ReconciliationRun(scopedKey, serializeResult(result)));
        return result;
    }

    private ReconciliationResultDto runReconciliation(String merchantId) {
        int merchantSettlementDelayHours = merchantId == null ? settlementDelayHours : merchantSettingsRepository
                .findByMerchant_MerchantId(merchantId).map(MerchantSettings::getSettlementDelayHours).orElse(settlementDelayHours);
        BigDecimal merchantFeeRate = merchantId == null ? null : merchantSettingsRepository
                .findByMerchant_MerchantId(merchantId).map(MerchantSettings::getFeeRate).orElse(null);
        BigDecimal merchantFeeTolerance = merchantId == null ? BigDecimal.ZERO : merchantSettingsRepository
                .findByMerchant_MerchantId(merchantId).map(MerchantSettings::getFeeTolerance).orElse(BigDecimal.ZERO);
        OffsetDateTime startedAt = OffsetDateTime.now();
        String runId = "rec_" + UUID.randomUUID().toString().substring(0, 8);
        List<ReconciliationItemResultDto> itemResults = new ArrayList<>();

        int recordsChecked = 0;
        int exceptionsCreated = 0;
        int exceptionsAlreadyExisting = 0;
        int successfulChecks = 0;
        int failedChecks = 0;
        BigDecimal totalDiscrepancyAmount = BigDecimal.ZERO;

        // 1. Reconcile Orders (Rule B: Missing Payment)
        List<Order> orders = merchantId == null ? orderRepository.findAll() : orderRepository.findByMerchantId(merchantId);
        for (Order order : orders) {
            recordsChecked++;
            if (order == null || order.getOrderId() == null || order.getAmount() == null || order.getStatus() == null) {
                failedChecks++;
                boolean created = exceptionDetectionService.detectAndCreateException(
                        order != null ? order.getMerchantId() : null, ExceptionType.DATA_INCOMPLETE, order, null, null, null,
                        null, null, BigDecimal.ZERO, "Order record is missing an identifier, amount, or status");
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                itemResults.add(incompleteItem("ORDER", order != null ? order.getOrderId() : null));
                continue;
            }
            List<Payment> orderPayments = paymentRepository.findByOrder_OrderId(order.getOrderId());
            boolean hasSuccessPayment = orderPayments.stream().anyMatch(p -> p.getStatus() == PaymentStatus.SUCCESS || p.getStatus() == PaymentStatus.PARTIALLY_REFUNDED || p.getStatus() == PaymentStatus.REFUNDED);

            if ((order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.PARTIALLY_PAID) && !hasSuccessPayment) {
                failedChecks++;
                boolean created = exceptionDetectionService.detectAndCreateException(
                        order.getMerchantId(), ExceptionType.MISSING_PAYMENT, order, null, null, null,
                        order.getAmount(), BigDecimal.ZERO, order.getAmount(),
                        "Order " + order.getOrderId() + " marked as " + order.getStatus() + " but has no successful payment"
                );
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                itemResults.add(ReconciliationItemResultDto.builder()
                        .entityType("ORDER").entityId(order.getOrderId()).status("DISCREPANCY")
                        .expectedAmount(order.getAmount()).actualAmount(BigDecimal.ZERO).discrepancyAmount(order.getAmount())
                        .exceptionType(ExceptionType.MISSING_PAYMENT)
                        .message("Missing payment for order " + order.getOrderId()).build());
            } else {
                successfulChecks++;
                itemResults.add(ReconciliationItemResultDto.builder()
                        .entityType("ORDER").entityId(order.getOrderId()).status("PASSED").build());
            }
        }

        // 2. Reconcile Payments (Rule A: Payment/Order Amount, Rule C: Missing Settlement, Rule E: Refund Validation, Rule H: Unknown Transaction)
        List<Payment> payments = merchantId == null ? paymentRepository.findAll() : paymentRepository.findByMerchantId(merchantId);
        Map<String, Long> paymentIdCounts = payments.stream().collect(Collectors.groupingBy(Payment::getPaymentId, Collectors.counting()));

        for (Payment payment : payments) {
            recordsChecked++;
            if (payment == null || payment.getPaymentId() == null || payment.getAmount() == null || payment.getStatus() == null) {
                failedChecks++;
                boolean created = exceptionDetectionService.detectAndCreateException(
                        payment != null ? payment.getMerchantId() : null, ExceptionType.DATA_INCOMPLETE, null, payment, null, null,
                        null, null, BigDecimal.ZERO, "Payment record is missing an identifier, amount, or status");
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                itemResults.add(incompleteItem("PAYMENT", payment != null ? payment.getPaymentId() : null));
                continue;
            }

            // Rule G: Duplicate Payment check
            if (paymentIdCounts.getOrDefault(payment.getPaymentId(), 0L) > 1) {
                failedChecks++;
                boolean created = exceptionDetectionService.detectAndCreateException(
                        payment.getMerchantId(), ExceptionType.DUPLICATE_TRANSACTION, payment.getOrder(), payment, null, null,
                        payment.getAmount(), payment.getAmount(), BigDecimal.ZERO,
                        "Duplicate payment record detected for paymentId " + payment.getPaymentId()
                );
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
            }

            // Rule H: Unknown / Unlinked Order
            if (payment.getOrder() == null) {
                failedChecks++;
                boolean created = exceptionDetectionService.detectAndCreateException(
                        payment.getMerchantId(), ExceptionType.UNKNOWN_TRANSACTION, null, payment, null, null,
                        BigDecimal.ZERO, payment.getAmount(), payment.getAmount(),
                        "Payment " + payment.getPaymentId() + " cannot be linked to an existing order"
                );
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                totalDiscrepancyAmount = totalDiscrepancyAmount.add(payment.getAmount());
                continue;
            }

            // Rule A: Amount Mismatch vs Order
            Order order = payment.getOrder();
            if (order.getAmount() == null || order.getCurrency() == null || payment.getCurrency() == null) {
                failedChecks++;
                boolean created = exceptionDetectionService.detectAndCreateException(
                        payment.getMerchantId(), ExceptionType.DATA_INCOMPLETE, order, payment, null, null,
                        null, null, BigDecimal.ZERO, "Payment or linked order is missing an amount or currency");
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                itemResults.add(incompleteItem("PAYMENT", payment.getPaymentId()));
                continue;
            }
            if (payment.getStatus() == PaymentStatus.SUCCESS && payment.getAmount().compareTo(order.getAmount()) != 0) {
                failedChecks++;
                BigDecimal diff = payment.getAmount().subtract(order.getAmount()).abs();
                boolean created = exceptionDetectionService.detectAndCreateException(
                        payment.getMerchantId(), ExceptionType.AMOUNT_MISMATCH, order, payment, null, null,
                        order.getAmount(), payment.getAmount(), diff,
                        "Payment amount " + payment.getAmount() + " does not match order amount " + order.getAmount()
                );
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                totalDiscrepancyAmount = totalDiscrepancyAmount.add(diff);
                itemResults.add(ReconciliationItemResultDto.builder()
                        .entityType("PAYMENT").entityId(payment.getPaymentId()).status("DISCREPANCY")
                        .expectedAmount(order.getAmount()).actualAmount(payment.getAmount()).discrepancyAmount(diff)
                        .exceptionType(ExceptionType.AMOUNT_MISMATCH)
                        .message("Payment amount mismatch against order").build());
            }

            // Rule C: Missing Settlement
            if (payment.getStatus() == PaymentStatus.SUCCESS && payment.getSettlement() == null) {
                OffsetDateTime threshold = OffsetDateTime.now().minusHours(merchantSettlementDelayHours);
                if (payment.getCreatedAt() != null && payment.getCreatedAt().isBefore(threshold)) {
                    failedChecks++;
                    boolean created = exceptionDetectionService.detectAndCreateException(
                            payment.getMerchantId(), ExceptionType.MISSING_SETTLEMENT, order, payment, null, null,
                            payment.getAmount(), BigDecimal.ZERO, payment.getAmount(),
                            "Payment " + payment.getPaymentId() + " remains unsettled beyond " + merchantSettlementDelayHours + " hours"
                    );
                    if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                    totalDiscrepancyAmount = totalDiscrepancyAmount.add(payment.getAmount());
                    itemResults.add(ReconciliationItemResultDto.builder()
                            .entityType("PAYMENT").entityId(payment.getPaymentId()).status("DISCREPANCY")
                            .expectedAmount(payment.getAmount()).actualAmount(BigDecimal.ZERO).discrepancyAmount(payment.getAmount())
                            .exceptionType(ExceptionType.MISSING_SETTLEMENT)
                            .message("Payment unsettled beyond threshold").build());
                }
            }

            // Rule E: Refund Validation
            List<Refund> refunds = refundRepository.findByPayment_PaymentId(payment.getPaymentId());
            if (refunds.stream().anyMatch(refund -> refund == null || refund.getAmount() == null)) {
                failedChecks++;
                boolean created = exceptionDetectionService.detectAndCreateException(
                        payment.getMerchantId(), ExceptionType.DATA_INCOMPLETE, order, payment, null, null,
                        null, null, BigDecimal.ZERO, "A refund linked to this payment is missing its amount");
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                itemResults.add(incompleteItem("PAYMENT", payment.getPaymentId()));
                continue;
            }
            BigDecimal totalRefunds = refunds.stream().map(Refund::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalRefunds.compareTo(payment.getAmount()) > 0) {
                failedChecks++;
                BigDecimal diff = totalRefunds.subtract(payment.getAmount());
                boolean created = exceptionDetectionService.detectAndCreateException(
                        payment.getMerchantId(), ExceptionType.DISCREPANT_REFUND, order, payment, null, null,
                        payment.getAmount(), totalRefunds, diff,
                        "Total refunds (" + totalRefunds + ") exceed payment amount (" + payment.getAmount() + ")"
                );
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                totalDiscrepancyAmount = totalDiscrepancyAmount.add(diff);
                itemResults.add(ReconciliationItemResultDto.builder()
                        .entityType("PAYMENT").entityId(payment.getPaymentId()).status("DISCREPANCY")
                        .expectedAmount(payment.getAmount()).actualAmount(totalRefunds).discrepancyAmount(diff)
                        .exceptionType(ExceptionType.DISCREPANT_REFUND)
                        .message("Total refunds exceed payment amount").build());
            }

            // Rule H: Currency Mismatch
            if (payment.getOrder() != null && !payment.getCurrency().equals(payment.getOrder().getCurrency())) {
                failedChecks++;
                boolean created = exceptionDetectionService.detectAndCreateException(
                        payment.getMerchantId(), ExceptionType.CURRENCY_MISMATCH, order, payment, null, null,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        "Currency mismatch detected: Order has " + payment.getOrder().getCurrency() + " but Payment has " + payment.getCurrency()
                );
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                itemResults.add(ReconciliationItemResultDto.builder()
                        .entityType("PAYMENT").entityId(payment.getPaymentId()).status("DISCREPANCY")
                        .expectedAmount(BigDecimal.ZERO).actualAmount(BigDecimal.ZERO).discrepancyAmount(BigDecimal.ZERO)
                        .exceptionType(ExceptionType.CURRENCY_MISMATCH)
                        .message("Currency mismatch between Order and Payment").build());
            }

            successfulChecks++;
            itemResults.add(ReconciliationItemResultDto.builder()
                    .entityType("PAYMENT").entityId(payment.getPaymentId()).status("PASSED").build());
        }

        // 3. Reconcile Fees (Rule F: Fee Validation)
        List<Fee> fees = merchantId == null ? feeRepository.findAll() : feeRepository.findByMerchantId(merchantId);
        for (Fee fee : fees) {
            recordsChecked++;
            if (fee == null || fee.getFeeAmount() == null || fee.getTaxAmount() == null || fee.getTotalFee() == null) {
                failedChecks++;
                Payment p = fee != null ? fee.getPayment() : null;
                Refund r = fee != null ? fee.getRefund() : null;
                boolean created = exceptionDetectionService.detectAndCreateException(
                        fee != null ? fee.getMerchantId() : null, ExceptionType.DATA_INCOMPLETE, p != null ? p.getOrder() : null, p, r, null,
                        null, null, BigDecimal.ZERO, "Fee record is missing a fee, tax, or total amount");
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                itemResults.add(incompleteItem("FEE", fee != null && fee.getId() != null ? fee.getId().toString() : null));
                continue;
            }
            BigDecimal expectedTotal = merchantFeeRate != null && fee.getPayment() != null
                    ? fee.getPayment().getAmount().multiply(merchantFeeRate).add(fee.getTaxAmount())
                    : fee.getFeeAmount().add(fee.getTaxAmount());
            if (fee.getTotalFee().subtract(expectedTotal).abs().compareTo(merchantFeeTolerance) > 0) {
                failedChecks++;
                BigDecimal diff = fee.getTotalFee().subtract(expectedTotal).abs();
                Payment p = fee.getPayment();
                Refund r = fee.getRefund();
                boolean created = exceptionDetectionService.detectAndCreateException(
                        fee.getMerchantId(), ExceptionType.UNEXPECTED_FEE, p != null ? p.getOrder() : null, p, r, null,
                        expectedTotal, fee.getTotalFee(), diff,
                        "Fee totalFee (" + fee.getTotalFee() + ") does not match feeAmount + taxAmount (" + expectedTotal + ")"
                );
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                totalDiscrepancyAmount = totalDiscrepancyAmount.add(diff);
                itemResults.add(ReconciliationItemResultDto.builder()
                        .entityType("FEE").entityId(fee.getId().toString()).status("DISCREPANCY")
                        .expectedAmount(expectedTotal).actualAmount(fee.getTotalFee()).discrepancyAmount(diff)
                        .exceptionType(ExceptionType.UNEXPECTED_FEE)
                        .message("Unexpected fee calculation mismatch").build());
            } else {
                successfulChecks++;
            }
        }

        // 4. Reconcile Settlements (Rule D: Settlement Amount Mismatch)
        List<Settlement> settlements = merchantId == null ? settlementRepository.findAll() : settlementRepository.findByMerchantId(merchantId);
        for (Settlement settlement : settlements) {
            recordsChecked++;
            if (settlement == null || settlement.getSettlementId() == null || settlement.getGrossAmount() == null
                    || settlement.getTotalRefundAmount() == null || settlement.getTotalFeeAmount() == null
                    || settlement.getTotalTaxAmount() == null || settlement.getTotalAdjustmentAmount() == null) {
                failedChecks++;
                boolean created = exceptionDetectionService.detectAndCreateException(
                        settlement != null ? settlement.getMerchantId() : null, ExceptionType.DATA_INCOMPLETE, null, null, null, settlement,
                        null, null, BigDecimal.ZERO, "Settlement record is missing an identifier or a required net calculation component");
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                itemResults.add(incompleteItem("SETTLEMENT", settlement != null ? settlement.getSettlementId() : null));
                continue;
            }
            BigDecimal expectedNet = settlement.getGrossAmount()
                    .subtract(settlement.getTotalRefundAmount())
                    .subtract(settlement.getTotalFeeAmount())
                    .subtract(settlement.getTotalTaxAmount())
                    .add(settlement.getTotalAdjustmentAmount());

            BigDecimal actualSettled = settlement.getActualSettledAmount() != null ? settlement.getActualSettledAmount() : BigDecimal.ZERO;

            if (expectedNet.compareTo(actualSettled) != 0) {
                failedChecks++;
                BigDecimal diff = expectedNet.subtract(actualSettled).abs();
                boolean created = exceptionDetectionService.detectAndCreateException(
                        settlement.getMerchantId(), ExceptionType.AMOUNT_MISMATCH, null, null, null, settlement,
                        expectedNet, actualSettled, diff,
                        "Settlement expected net (" + expectedNet + ") differs from actual settled amount (" + actualSettled + ")"
                );
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                totalDiscrepancyAmount = totalDiscrepancyAmount.add(diff);

                settlement.setStatus(SettlementStatus.DISCREPANT);
                settlementRepository.save(settlement);

                itemResults.add(ReconciliationItemResultDto.builder()
                        .entityType("SETTLEMENT").entityId(settlement.getSettlementId()).status("DISCREPANCY")
                        .expectedAmount(expectedNet).actualAmount(actualSettled).discrepancyAmount(diff)
                        .exceptionType(ExceptionType.AMOUNT_MISMATCH)
                        .message("Settlement payout amount mismatch").build());
            } else {
                successfulChecks++;
                itemResults.add(ReconciliationItemResultDto.builder()
                        .entityType("SETTLEMENT").entityId(settlement.getSettlementId()).status("PASSED").build());
            }
        }

        // 5. Reconcile Adjustments (Rule G: Unmatched Adjustment)
        List<Adjustment> adjustments = merchantId == null ? adjustmentRepository.findAll() : adjustmentRepository.findByMerchantId(merchantId);
        for (Adjustment adjustment : adjustments) {
            recordsChecked++;
            if (adjustment == null || adjustment.getAdjustmentId() == null || adjustment.getAmount() == null) {
                failedChecks++;
                boolean created = exceptionDetectionService.detectAndCreateException(
                        adjustment != null ? adjustment.getMerchantId() : null, ExceptionType.DATA_INCOMPLETE, null,
                        adjustment != null ? adjustment.getPayment() : null, null,
                        adjustment != null ? adjustment.getSettlement() : null, null, null, BigDecimal.ZERO,
                        "Adjustment record is missing an identifier or amount");
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                itemResults.add(incompleteItem("ADJUSTMENT", adjustment != null ? adjustment.getAdjustmentId() : null));
                continue;
            }
            if (adjustment.getSettlement() == null && adjustment.getPayment() == null) {
                failedChecks++;
                boolean created = exceptionDetectionService.detectAndCreateException(
                        adjustment.getMerchantId(), ExceptionType.UNMATCHED_ADJUSTMENT, null, null, null, null,
                        BigDecimal.ZERO, adjustment.getAmount(), adjustment.getAmount().abs(),
                        "Isolated adjustment record " + adjustment.getAdjustmentId() + " lacks mapping to a payment or settlement lineage"
                );
                if (created) exceptionsCreated++; else exceptionsAlreadyExisting++;
                totalDiscrepancyAmount = totalDiscrepancyAmount.add(adjustment.getAmount().abs());

                itemResults.add(ReconciliationItemResultDto.builder()
                        .entityType("ADJUSTMENT").entityId(adjustment.getAdjustmentId()).status("DISCREPANCY")
                        .expectedAmount(BigDecimal.ZERO).actualAmount(adjustment.getAmount()).discrepancyAmount(adjustment.getAmount().abs())
                        .exceptionType(ExceptionType.UNMATCHED_ADJUSTMENT)
                        .message("Adjustment record lacks valid mapping reference").build());
            } else {
                successfulChecks++;
            }
        }

        OffsetDateTime completedAt = OffsetDateTime.now();

        ReconciliationResultDto result = ReconciliationResultDto.builder()
                .reconciliationId(runId)
                .recordsChecked(recordsChecked)
                .exceptionsCreated(exceptionsCreated)
                .exceptionsAlreadyExisting(exceptionsAlreadyExisting)
                .successfulChecks(successfulChecks)
                .failedChecks(failedChecks)
                .totalDiscrepancyAmount(totalDiscrepancyAmount)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .items(itemResults)
                .build();

        // Audit Logging
        AuditLog auditLog = AuditLog.builder()
                .entityType("RECONCILIATION")
                .entityId(UUID.randomUUID())
                .merchantId(merchantId)
                .action("RECONCILIATION_RUN")
                .performedBy("SYSTEM")
                .details(String.format("{\"runId\":\"%s\",\"recordsChecked\":%d,\"exceptionsCreated\":%d,\"exceptionsAlreadyExisting\":%d,\"totalDiscrepancy\":%s}",
                        runId, recordsChecked, exceptionsCreated, exceptionsAlreadyExisting, totalDiscrepancyAmount.toString()))
                .build();
        auditLogRepository.save(auditLog);

        return result;
    }

    @Transactional
    public ReconciliationItemResultDto reconcilePayment(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment " + paymentId + " was not found"));

        Order order = payment.getOrder();
        if (order != null && payment.getAmount().compareTo(order.getAmount()) != 0) {
            BigDecimal diff = payment.getAmount().subtract(order.getAmount()).abs();
            exceptionDetectionService.detectAndCreateException(
                    payment.getMerchantId(), ExceptionType.AMOUNT_MISMATCH, order, payment, null, null,
                    order.getAmount(), payment.getAmount(), diff,
                    "Payment amount mismatch against order"
            );
            return ReconciliationItemResultDto.builder()
                    .entityType("PAYMENT").entityId(paymentId).status("DISCREPANCY")
                    .expectedAmount(order.getAmount()).actualAmount(payment.getAmount()).discrepancyAmount(diff)
                    .exceptionType(ExceptionType.AMOUNT_MISMATCH).message("Payment amount mismatch").build();
        }

        return ReconciliationItemResultDto.builder()
                .entityType("PAYMENT").entityId(paymentId).status("PASSED").message("Payment reconciled successfully").build();
    }

    @Transactional
    public ReconciliationItemResultDto reconcileSettlement(String settlementId) {
        Settlement settlement = settlementRepository.findBySettlementId(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement " + settlementId + " was not found"));

        BigDecimal expectedNet = settlement.getGrossAmount()
                .subtract(settlement.getTotalRefundAmount())
                .subtract(settlement.getTotalFeeAmount())
                .subtract(settlement.getTotalTaxAmount())
                .add(settlement.getTotalAdjustmentAmount());

        BigDecimal actualSettled = settlement.getActualSettledAmount() != null ? settlement.getActualSettledAmount() : BigDecimal.ZERO;

        if (expectedNet.compareTo(actualSettled) != 0) {
            BigDecimal diff = expectedNet.subtract(actualSettled).abs();
            exceptionDetectionService.detectAndCreateException(
                    settlement.getMerchantId(), ExceptionType.AMOUNT_MISMATCH, null, null, null, settlement,
                    expectedNet, actualSettled, diff,
                    "Settlement net mismatch"
            );
            settlement.setStatus(SettlementStatus.DISCREPANT);
            settlementRepository.save(settlement);
            return ReconciliationItemResultDto.builder()
                    .entityType("SETTLEMENT").entityId(settlementId).status("DISCREPANCY")
                    .expectedAmount(expectedNet).actualAmount(actualSettled).discrepancyAmount(diff)
                    .exceptionType(ExceptionType.AMOUNT_MISMATCH).message("Settlement amount mismatch").build();
        }

        return ReconciliationItemResultDto.builder()
                .entityType("SETTLEMENT").entityId(settlementId).status("PASSED").message("Settlement reconciled successfully").build();
    }

    @Transactional
    public ReconciliationItemResultDto reconcileOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " was not found"));

        List<Payment> orderPayments = paymentRepository.findByOrder_OrderId(orderId);
        boolean hasSuccessPayment = orderPayments.stream().anyMatch(p -> p.getStatus() == PaymentStatus.SUCCESS || p.getStatus() == PaymentStatus.PARTIALLY_REFUNDED || p.getStatus() == PaymentStatus.REFUNDED);

        if ((order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.PARTIALLY_PAID) && !hasSuccessPayment) {
            exceptionDetectionService.detectAndCreateException(
                    order.getMerchantId(), ExceptionType.MISSING_PAYMENT, order, null, null, null,
                    order.getAmount(), BigDecimal.ZERO, order.getAmount(),
                    "Order missing payment"
            );
            return ReconciliationItemResultDto.builder()
                    .entityType("ORDER").entityId(orderId).status("DISCREPANCY")
                    .expectedAmount(order.getAmount()).actualAmount(BigDecimal.ZERO).discrepancyAmount(order.getAmount())
                    .exceptionType(ExceptionType.MISSING_PAYMENT).message("Missing payment for order").build();
        }

        return ReconciliationItemResultDto.builder()
                .entityType("ORDER").entityId(orderId).status("PASSED").message("Order reconciled successfully").build();
    }

    private String serializeResult(ReconciliationResultDto result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not persist the reconciliation response", ex);
        }
    }

    private ReconciliationResultDto deserializeResult(String resultPayload) {
        try {
            return objectMapper.readValue(resultPayload, ReconciliationResultDto.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Stored reconciliation response is invalid", ex);
        }
    }

    private ReconciliationItemResultDto incompleteItem(String entityType, String entityId) {
        return ReconciliationItemResultDto.builder()
                .entityType(entityType).entityId(entityId != null ? entityId : "UNKNOWN")
                .status("DISCREPANCY").exceptionType(ExceptionType.DATA_INCOMPLETE)
                .message("Required reconciliation data is incomplete").build();
    }
}
