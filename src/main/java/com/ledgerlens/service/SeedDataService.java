package com.ledgerlens.service;

import com.ledgerlens.dto.SeedResponseDto;
import com.ledgerlens.entity.*;
import com.ledgerlens.entity.enums.*;
import com.ledgerlens.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeedDataService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final FeeRepository feeRepository;
    private final AdjustmentRepository adjustmentRepository;
    private final SettlementRepository settlementRepository;
    private final com.ledgerlens.repository.MerchantRepository merchantRepository;
    private final com.ledgerlens.repository.MerchantSettingsRepository merchantSettingsRepository;
    private final com.ledgerlens.repository.AppUserRepository appUserRepository;
    private final FinancialExceptionRepository exceptionRepository;
    private final InvestigationRepository investigationRepository;
    private final HistoricalInvestigationEmbeddingRepository embeddingRepository;
    private final ReconciliationExecutionLockRepository reconciliationExecutionLockRepository;
    private final AuditLogRepository auditLogRepository;
    private final ReconciliationRunRepository reconciliationRunRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    public SeedDataService(OrderRepository orderRepository, PaymentRepository paymentRepository, RefundRepository refundRepository, FeeRepository feeRepository, AdjustmentRepository adjustmentRepository, SettlementRepository settlementRepository, com.ledgerlens.repository.MerchantRepository merchantRepository, com.ledgerlens.repository.MerchantSettingsRepository merchantSettingsRepository, com.ledgerlens.repository.AppUserRepository appUserRepository, FinancialExceptionRepository exceptionRepository, InvestigationRepository investigationRepository, HistoricalInvestigationEmbeddingRepository embeddingRepository, ReconciliationExecutionLockRepository reconciliationExecutionLockRepository, AuditLogRepository auditLogRepository, ReconciliationRunRepository reconciliationRunRepository, org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.feeRepository = feeRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.settlementRepository = settlementRepository;
        this.merchantRepository = merchantRepository;
        this.merchantSettingsRepository = merchantSettingsRepository;
        this.appUserRepository = appUserRepository;
        this.exceptionRepository = exceptionRepository;
        this.investigationRepository = investigationRepository;
        this.embeddingRepository = embeddingRepository;
        this.reconciliationExecutionLockRepository = reconciliationExecutionLockRepository;
        this.auditLogRepository = auditLogRepository;
        this.reconciliationRunRepository = reconciliationRunRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void clearDemoData() {
        List<String> demoMerchants = List.of("merchant_a", "merchant_b");
        for (String mId : demoMerchants) {
            // 1. Delete Embeddings
            jdbcTemplate.update("DELETE FROM historical_investigation_embeddings WHERE merchant_id = ? OR investigation_id IN (SELECT i.id FROM investigations i JOIN exceptions e ON i.exception_id = e.id WHERE e.merchant_id = ?)", mId, mId);

            // 2. Delete Investigations
            jdbcTemplate.update("DELETE FROM investigations WHERE exception_id IN (SELECT id FROM exceptions WHERE merchant_id = ?)", mId);

            // 3. Delete Exceptions
            jdbcTemplate.update("DELETE FROM exceptions WHERE merchant_id = ?", mId);

            // 4. Delete Fees
            jdbcTemplate.update("DELETE FROM fees WHERE merchant_id = ?", mId);

            // 5. Delete Adjustments
            jdbcTemplate.update("DELETE FROM adjustments WHERE merchant_id = ?", mId);

            // 6. Delete Refunds
            jdbcTemplate.update("DELETE FROM refunds WHERE merchant_id = ?", mId);

            // 7. Delete Payments
            jdbcTemplate.update("DELETE FROM payments WHERE merchant_id = ?", mId);

            // 8. Delete Settlements
            jdbcTemplate.update("DELETE FROM settlements WHERE merchant_id = ?", mId);

            // 9. Delete Orders
            jdbcTemplate.update("DELETE FROM orders WHERE merchant_id = ?", mId);

            // 10. Delete Audit Logs
            jdbcTemplate.update("DELETE FROM audit_logs WHERE merchant_id = ?", mId);

            // 11. Delete Reconciliation Runs
        }
        // Ensure required singleton mutex rows exist in reconciliation_execution_locks
        try {
            jdbcTemplate.update("INSERT INTO reconciliation_execution_locks (id) VALUES (?) ON CONFLICT (id) DO NOTHING", ReconciliationExecutionLock.GLOBAL_LOCK_ID);
            jdbcTemplate.update("INSERT INTO reconciliation_execution_locks (id) VALUES (?) ON CONFLICT (id) DO NOTHING", "MERCHANT:merchant_a");
            jdbcTemplate.update("INSERT INTO reconciliation_execution_locks (id) VALUES (?) ON CONFLICT (id) DO NOTHING", "MERCHANT:merchant_b");
        } catch (Exception e) {
            try {
                jdbcTemplate.update("MERGE INTO reconciliation_execution_locks (id) KEY (id) VALUES (?)", ReconciliationExecutionLock.GLOBAL_LOCK_ID);
                jdbcTemplate.update("MERGE INTO reconciliation_execution_locks (id) KEY (id) VALUES (?)", "MERCHANT:merchant_a");
                jdbcTemplate.update("MERGE INTO reconciliation_execution_locks (id) KEY (id) VALUES (?)", "MERCHANT:merchant_b");
            } catch (Exception ignored) { }
        }
        if (entityManager != null) {
            entityManager.clear();
        }
    }

    @Transactional
    public SeedResponseDto seedDemoData() {
        // Reset all prior demo data to guarantee a pristine baseline
        clearDemoData();

        Merchant merchantA = merchantRepository.findByMerchantId("merchant_a").orElseGet(() -> merchantRepository.save(new Merchant("merchant_a", "Merchant A")));
        Merchant merchantB = merchantRepository.findByMerchantId("merchant_b").orElseGet(() -> merchantRepository.save(new Merchant("merchant_b", "Merchant B")));
        if (merchantSettingsRepository.findByMerchant_MerchantId("merchant_a").isEmpty()) merchantSettingsRepository.save(new MerchantSettings(merchantA, 24, new BigDecimal("0.0200"), new BigDecimal("0.01")));
        if (merchantSettingsRepository.findByMerchant_MerchantId("merchant_b").isEmpty()) merchantSettingsRepository.save(new MerchantSettings(merchantB, 1, new BigDecimal("0.0300"), new BigDecimal("0.01")));
        createUserIfAbsent("operator", merchantA, "OPERATOR");
        createUserIfAbsent("analyst", merchantA, "ANALYST");
        createUserIfAbsent("admin", merchantA, "ADMIN");
        createUserIfAbsent("merchant_b_operator", merchantB, "OPERATOR");
        createUserIfAbsent("merchant_b_analyst", merchantB, "ANALYST");
        createUserIfAbsent("merchant_b_admin", merchantB, "ADMIN");

        List<String> ordersCreated = new ArrayList<>();
        List<String> paymentsCreated = new ArrayList<>();
        List<String> refundsCreated = new ArrayList<>();
        List<String> feesCreated = new ArrayList<>();
        List<String> adjustmentsCreated = new ArrayList<>();
        List<String> settlementsCreated = new ArrayList<>();

        // A separate merchant data set is intentionally small but independently reconcilable.
        Order merchantBOrder = getOrCreateOrder("ord_b_1001", "merchant_b", "cust_b_1", new BigDecimal("100.00"), OrderStatus.PAID, ordersCreated);
        getOrCreatePayment("pay_b_1001", merchantBOrder, "merchant_b", PaymentMethod.UPI, new BigDecimal("100.00"), PaymentStatus.SUCCESS, null, OffsetDateTime.now().minusHours(2), paymentsCreated);

        // 1. Normal Transaction
        Order ord1 = getOrCreateOrder("ord_1001", "merchant_a", "cust_101", new BigDecimal("1000.00"), OrderStatus.PAID, ordersCreated);
        Settlement set1 = getOrCreateSettlement("set_1001", "merchant_a", new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("20.00"), new BigDecimal("3.60"), BigDecimal.ZERO, new BigDecimal("976.40"), new BigDecimal("976.40"), SettlementStatus.SETTLED, "UTR99881122", settlementsCreated);
        Payment pay1 = getOrCreatePayment("pay_1001", ord1, "merchant_a", PaymentMethod.CARD, new BigDecimal("1000.00"), PaymentStatus.SUCCESS, set1, OffsetDateTime.now(), paymentsCreated);
        createFeeIfAbsent(pay1, null, "merchant_a", new BigDecimal("20.00"), new BigDecimal("3.60"), new BigDecimal("23.60"), new BigDecimal("0.0200"), feesCreated);

        // 2. Exception Transaction (Mismatch)
        Order ord2 = getOrCreateOrder("ord_1002", "merchant_a", "cust_102", new BigDecimal("1000.00"), OrderStatus.PAID, ordersCreated);
        Settlement set2 = getOrCreateSettlement("set_1002", "merchant_a", new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("20.00"), new BigDecimal("3.60"), BigDecimal.ZERO, new BigDecimal("976.40"), new BigDecimal("476.40"), SettlementStatus.DISCREPANT, "UTR99881123", settlementsCreated);
        Payment pay2 = getOrCreatePayment("pay_1002", ord2, "merchant_a", PaymentMethod.UPI, new BigDecimal("1000.00"), PaymentStatus.SUCCESS, set2, OffsetDateTime.now(), paymentsCreated);
        createFeeIfAbsent(pay2, null, "merchant_a", new BigDecimal("20.00"), new BigDecimal("3.60"), new BigDecimal("23.60"), new BigDecimal("0.0200"), feesCreated);

        // 3. Partial Refund
        Order ord3 = getOrCreateOrder("ord_1003", "merchant_a", "cust_103", new BigDecimal("2000.00"), OrderStatus.PARTIALLY_PAID, ordersCreated);
        Payment pay4 = getOrCreatePayment("pay_1004", ord3, "merchant_a", PaymentMethod.CARD, new BigDecimal("2000.00"), PaymentStatus.PARTIALLY_REFUNDED, null, OffsetDateTime.now(), paymentsCreated);
        createRefundIfAbsent("rfnd_1001", pay4, "merchant_a", new BigDecimal("500.00"), RefundStatus.PROCESSED, "Customer requested partial return", refundsCreated);

        // 4. Chargeback
        createAdjustmentIfAbsent("adj_1001", "merchant_a", set1, pay1, new BigDecimal("-150.00"), AdjustmentType.CHARGEBACK, "Dispute chargeback", adjustmentsCreated);

        // 5. Unsettled Payment (>24h delay for Rule B / MISSING_SETTLEMENT)
        Order ord4 = getOrCreateOrder("ord_1004", "merchant_a", "cust_104", new BigDecimal("1500.00"), OrderStatus.PAID, ordersCreated);
        getOrCreatePayment("pay_1003", ord4, "merchant_a", PaymentMethod.NETBANKING, new BigDecimal("1500.00"), PaymentStatus.SUCCESS, null, OffsetDateTime.now().minusDays(2), paymentsCreated);

        // 6. Discrepant Refund — settlement refund total exceeds gross amount
        Order ord5 = getOrCreateOrder("ord_1005", "merchant_a", "cust_105", new BigDecimal("500.00"), OrderStatus.CANCELLED, ordersCreated);
        Settlement set3 = getOrCreateSettlement("set_1003", "merchant_a", new BigDecimal("500.00"), new BigDecimal("600.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("-100.00"), new BigDecimal("-100.00"), SettlementStatus.DISCREPANT, "UTR99881124", settlementsCreated);
        getOrCreatePayment("pay_1005", ord5, "merchant_a", PaymentMethod.CARD, new BigDecimal("500.00"), PaymentStatus.REFUNDED, set3, OffsetDateTime.now(), paymentsCreated);

        // 7. Unexpected Fee — totalFee (75.00) intentionally != feeAmount (50.00) + taxAmount (9.00) = 59.00
        Order ord6 = getOrCreateOrder("ord_1006", "merchant_a", "cust_106", new BigDecimal("200.00"), OrderStatus.PAID, ordersCreated);
        Payment pay6 = getOrCreatePayment("pay_1006", ord6, "merchant_a", PaymentMethod.CARD, new BigDecimal("200.00"), PaymentStatus.SUCCESS, null, OffsetDateTime.now(), paymentsCreated);
        createFeeIfAbsent(pay6, null, "merchant_a", new BigDecimal("50.00"), new BigDecimal("9.00"), new BigDecimal("75.00"), new BigDecimal("0.2500"), feesCreated);

        // 8. Reversal adjustment on isolated settlement
        Settlement set4 = getOrCreateSettlement("set_1004", "merchant_a", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, SettlementStatus.SETTLED, "UTR99881125", settlementsCreated);
        createAdjustmentIfAbsent("adj_1002", "merchant_a", set4, null, new BigDecimal("-10.00"), AdjustmentType.REVERSAL, "Reversal adjustment on isolated settlement", adjustmentsCreated);

        // 9. Chargeback (Multiple)
        createAdjustmentIfAbsent("adj_1003", "merchant_a", set1, pay1, new BigDecimal("-50.00"), AdjustmentType.CHARGEBACK, "Additional reversal", adjustmentsCreated);

        // 10. Clean After Refund — full refund within bounds, passes reconciliation
        Order ord7 = getOrCreateOrder("ord_1007", "merchant_a", "cust_107", new BigDecimal("300.00"), OrderStatus.CANCELLED, ordersCreated);
        Payment pay7 = getOrCreatePayment("pay_1007", ord7, "merchant_a", PaymentMethod.UPI, new BigDecimal("300.00"), PaymentStatus.REFUNDED, null, OffsetDateTime.now(), paymentsCreated);
        createRefundIfAbsent("rfnd_1002", pay7, "merchant_a", new BigDecimal("300.00"), RefundStatus.PROCESSED, "Full return approved", refundsCreated);

        // ─── FIVE HERO EXCEPTIONS ───
        // HERO 1 — SETTLEMENT SHORTFALL
        Order ordHero1 = getOrCreateOrder("ord_hero_1", "merchant_a", "cust_hero_1", new BigDecimal("52000.00"), OrderStatus.PAID, ordersCreated);
        Settlement setHero1 = getOrCreateSettlement("set_hero_1", "merchant_a", new BigDecimal("52000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("-4850.00"), new BigDecimal("52000.00"), new BigDecimal("47150.00"), SettlementStatus.DISCREPANT, "UTR_HERO_1", settlementsCreated);
        Payment payHero1 = getOrCreatePayment("pay_hero_1", ordHero1, "merchant_a", PaymentMethod.CARD, new BigDecimal("52000.00"), PaymentStatus.SUCCESS, setHero1, OffsetDateTime.now(), paymentsCreated);
        createAdjustmentIfAbsent("adj_hero_1", "merchant_a", setHero1, payHero1, new BigDecimal("-4850.00"), AdjustmentType.CHARGEBACK, "Chargeback dispute shortfall", adjustmentsCreated);

        // HERO 2 — MISSING SETTLEMENT (>24h, no settlement linked)
        Order ordHero2 = getOrCreateOrder("ord_hero_2", "merchant_a", "cust_hero_2", new BigDecimal("10000.00"), OrderStatus.PAID, ordersCreated);
        getOrCreatePayment("pay_hero_2", ordHero2, "merchant_a", PaymentMethod.UPI, new BigDecimal("10000.00"), PaymentStatus.SUCCESS, null, OffsetDateTime.now().minusDays(2), paymentsCreated);

        // HERO 3 — DUPLICATE PAYMENT
        Order ordHero3 = getOrCreateOrder("ord_hero_3", "merchant_a", "cust_hero_3", new BigDecimal("12000.00"), OrderStatus.PAID, ordersCreated);
        getOrCreatePayment("pay_hero_3a", ordHero3, "merchant_a", PaymentMethod.CARD, new BigDecimal("12000.00"), PaymentStatus.SUCCESS, null, OffsetDateTime.now(), paymentsCreated);
        getOrCreatePayment("pay_hero_3b", ordHero3, "merchant_a", PaymentMethod.CARD, new BigDecimal("12000.00"), PaymentStatus.SUCCESS, null, OffsetDateTime.now(), paymentsCreated);

        // HERO 4 — FEE OVERCHARGE
        Order ordHero4 = getOrCreateOrder("ord_hero_4", "merchant_a", "cust_hero_4", new BigDecimal("1500.00"), OrderStatus.PAID, ordersCreated);
        Payment payHero4 = getOrCreatePayment("pay_hero_4", ordHero4, "merchant_a", PaymentMethod.CARD, new BigDecimal("1500.00"), PaymentStatus.SUCCESS, null, OffsetDateTime.now(), paymentsCreated);
        createFeeIfAbsent(payHero4, null, "merchant_a", new BigDecimal("423.73"), new BigDecimal("76.27"), new BigDecimal("500.00"), new BigDecimal("0.0200"), feesCreated);

        // HERO 5 — AMBIGUOUS CASE (CURRENCY MISMATCH, USD Order vs INR Payment)
        Order ordHero5 = getOrCreateOrder("ord_hero_5", "merchant_a", "cust_hero_5", new BigDecimal("8000.00"), OrderStatus.PAID, ordersCreated);
        ordHero5.setCurrency("USD");
        orderRepository.save(ordHero5);
        getOrCreatePayment("pay_hero_5", ordHero5, "merchant_a", PaymentMethod.CARD, new BigDecimal("8000.00"), PaymentStatus.SUCCESS, null, OffsetDateTime.now(), paymentsCreated);

        // ─── 70 CLEAN BULK DATA RECORDS FOR REALISTIC METRICS ───
        if (orderRepository.findByOrderId("ord_2001").isEmpty()) {
            List<Order> bulkOrders = new ArrayList<>(70);
            List<Settlement> bulkSettlements = new ArrayList<>(70);
            List<Payment> bulkPayments = new ArrayList<>(70);
            List<Fee> bulkFees = new ArrayList<>(70);

            for (int i = 2001; i <= 2070; i++) {
                String ordId = "ord_" + i;
                String payId = "pay_" + i;
                String setId = "set_" + i;
                String custId = "cust_" + (i - 1000);

                BigDecimal amount = new BigDecimal("1000.00").add(new BigDecimal(i % 10).multiply(new BigDecimal("100.00")));
                BigDecimal feeRate = new BigDecimal("0.0200");
                BigDecimal feeAmt = amount.multiply(feeRate).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal taxAmt = feeAmt.multiply(new BigDecimal("0.18")).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal totalFee = feeAmt.add(taxAmt);
                BigDecimal netAmt = amount.subtract(totalFee);

                Order bulkOrd = Order.builder()
                        .orderId(ordId)
                        .merchantId("merchant_a")
                        .customerId(custId)
                        .amount(amount)
                        .currency("INR")
                        .status(OrderStatus.PAID)
                        .build();
                bulkOrders.add(bulkOrd);
                ordersCreated.add(ordId);

                Settlement bulkSet = Settlement.builder()
                        .settlementId(setId)
                        .merchantId("merchant_a")
                        .grossAmount(amount)
                        .totalRefundAmount(BigDecimal.ZERO)
                        .totalFeeAmount(feeAmt)
                        .totalTaxAmount(taxAmt)
                        .totalAdjustmentAmount(BigDecimal.ZERO)
                        .netAmount(netAmt)
                        .actualSettledAmount(netAmt)
                        .status(SettlementStatus.SETTLED)
                        .utr("UTR9988" + i)
                        .settledAt(OffsetDateTime.now())
                        .build();
                bulkSettlements.add(bulkSet);
                settlementsCreated.add(setId);

                Payment bulkPay = Payment.builder()
                        .paymentId(payId)
                        .order(bulkOrd)
                        .merchantId("merchant_a")
                        .method(PaymentMethod.CARD)
                        .amount(amount)
                        .currency("INR")
                        .status(PaymentStatus.SUCCESS)
                        .settlement(bulkSet)
                        .createdAt(OffsetDateTime.now())
                        .build();
                bulkPayments.add(bulkPay);
                paymentsCreated.add(payId);

                Fee bulkFee = Fee.builder()
                        .payment(bulkPay)
                        .refund(null)
                        .merchantId("merchant_a")
                        .feeAmount(feeAmt)
                        .taxAmount(taxAmt)
                        .totalFee(totalFee)
                        .feeRate(feeRate)
                        .currency("INR")
                        .build();
                bulkFees.add(bulkFee);
                feesCreated.add("fee_" + payId);
            }

            orderRepository.saveAll(bulkOrders);
            settlementRepository.saveAll(bulkSettlements);
            paymentRepository.saveAll(bulkPayments);
            feeRepository.saveAll(bulkFees);
        }

        return SeedResponseDto.builder()
                .message("Demo financial data seeded successfully: " + getScenariosDescription())
                .ordersCreated(ordersCreated)
                .paymentsCreated(paymentsCreated)
                .refundsCreated(refundsCreated)
                .feesCreated(feesCreated)
                .adjustmentsCreated(adjustmentsCreated)
                .settlementsCreated(settlementsCreated)
                .build();
    }

    private String getScenariosDescription() {
        return "80 scenarios covered including 5 deterministic Hero exceptions (Settlement Shortfall, Missing Settlement, Duplicate Payment, Fee Overcharge, Currency Mismatch) and 70 clean transactions.";
    }

    private Order getOrCreateOrder(String orderId, String merchantId, String customerId, BigDecimal amount, OrderStatus status, List<String> tracker) {
        return orderRepository.findByOrderId(orderId).map(order -> {
            order.setMerchantId(merchantId);
            order.setCustomerId(customerId);
            order.setAmount(amount);
            order.setCurrency("INR");
            order.setStatus(status);
            return orderRepository.save(order);
        }).orElseGet(() -> {
            Order order = orderRepository.save(Order.builder()
                    .orderId(orderId)
                    .merchantId(merchantId)
                    .customerId(customerId)
                    .amount(amount)
                    .currency("INR")
                    .status(status)
                    .build());
            tracker.add(orderId);
            return order;
        });
    }

    private Settlement getOrCreateSettlement(String settlementId, String merchantId, BigDecimal gross, BigDecimal refunds, BigDecimal fee, BigDecimal tax, BigDecimal adj, BigDecimal net, BigDecimal actual, SettlementStatus status, String utr, List<String> tracker) {
        return settlementRepository.findBySettlementId(settlementId).map(s -> {
            s.setMerchantId(merchantId);
            s.setGrossAmount(gross);
            s.setTotalRefundAmount(refunds);
            s.setTotalFeeAmount(fee);
            s.setTotalTaxAmount(tax);
            s.setTotalAdjustmentAmount(adj);
            s.setNetAmount(net);
            s.setActualSettledAmount(actual);
            s.setStatus(status);
            s.setUtr(utr);
            s.setSettledAt(status == SettlementStatus.SETTLED ? OffsetDateTime.now() : null);
            return settlementRepository.save(s);
        }).orElseGet(() -> {
            Settlement settlement = settlementRepository.save(Settlement.builder()
                    .settlementId(settlementId)
                    .merchantId(merchantId)
                    .grossAmount(gross)
                    .totalRefundAmount(refunds)
                    .totalFeeAmount(fee)
                    .totalTaxAmount(tax)
                    .totalAdjustmentAmount(adj)
                    .netAmount(net)
                    .actualSettledAmount(actual)
                    .status(status)
                    .utr(utr)
                    .settledAt(status == SettlementStatus.SETTLED ? OffsetDateTime.now() : null)
                    .build());
            tracker.add(settlementId);
            return settlement;
        });
    }

    private Payment getOrCreatePayment(String paymentId, Order order, String merchantId, PaymentMethod method, BigDecimal amount, PaymentStatus status, Settlement settlement, OffsetDateTime createdAt, List<String> tracker) {
        return paymentRepository.findByPaymentId(paymentId).map(payment -> {
            payment.setOrder(order);
            payment.setMerchantId(merchantId);
            payment.setMethod(method);
            payment.setAmount(amount);
            payment.setCurrency("INR");
            payment.setStatus(status);
            payment.setSettlement(settlement);
            payment.setCreatedAt(createdAt);
            return paymentRepository.save(payment);
        }).orElseGet(() -> {
            Payment payment = paymentRepository.save(Payment.builder()
                    .paymentId(paymentId)
                    .order(order)
                    .merchantId(merchantId)
                    .method(method)
                    .amount(amount)
                    .currency("INR")
                    .status(status)
                    .settlement(settlement)
                    .createdAt(createdAt)
                    .build());
            tracker.add(paymentId);
            return payment;
        });
    }

    private void createRefundIfAbsent(String refundId, Payment payment, String merchantId, BigDecimal amount, RefundStatus status, String reason, List<String> tracker) {
        if (!refundRepository.existsByRefundId(refundId)) {
            refundRepository.save(Refund.builder()
                    .refundId(refundId)
                    .payment(payment)
                    .merchantId(merchantId)
                    .amount(amount)
                    .currency("INR")
                    .status(status)
                    .reason(reason)
                    .processedAt(OffsetDateTime.now())
                    .build());
            tracker.add(refundId);
        }
    }

    private void createFeeIfAbsent(Payment payment, Refund refund, String merchantId, BigDecimal feeAmount, BigDecimal taxAmount, BigDecimal totalFee, BigDecimal feeRate, List<String> tracker) {
        String feeIdentifier = (payment != null ? "pay:" + payment.getPaymentId() : "rfnd:" + refund.getRefundId());
        boolean exists = false;
        if (payment != null) {
            exists = !feeRepository.findByPayment_PaymentId(payment.getPaymentId()).isEmpty();
        } else if (refund != null) {
            exists = !feeRepository.findByRefund_RefundId(refund.getRefundId()).isEmpty();
        }
        if (!exists) {
            feeRepository.save(Fee.builder()
                    .payment(payment)
                    .refund(refund)
                    .merchantId(merchantId)
                    .feeAmount(feeAmount)
                    .taxAmount(taxAmount)
                    .totalFee(totalFee)
                    .feeRate(feeRate)
                    .currency("INR")
                    .build());
            tracker.add(feeIdentifier);
        }
    }

    private void createAdjustmentIfAbsent(String adjustmentId, String merchantId, Settlement settlement, Payment payment, BigDecimal amount, AdjustmentType type, String description, List<String> tracker) {
        if (!adjustmentRepository.existsByAdjustmentId(adjustmentId)) {
            adjustmentRepository.save(Adjustment.builder()
                    .adjustmentId(adjustmentId)
                    .merchantId(merchantId)
                    .settlement(settlement)
                    .payment(payment)
                    .amount(amount)
                    .type(type)
                    .description(description)
                    .build());
            tracker.add(adjustmentId);
        }
    }

    private void createUserIfAbsent(String username, Merchant merchant, String role) {
        if (appUserRepository.findByUsername(username).isEmpty()) {
            appUserRepository.save(new AppUser(username, merchant, role));
        }
    }
}
