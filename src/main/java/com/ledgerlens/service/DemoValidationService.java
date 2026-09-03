package com.ledgerlens.service;

import com.ledgerlens.dto.*;
import com.ledgerlens.entity.Investigation;
import com.ledgerlens.entity.enums.ExceptionSeverity;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.repository.AuditLogRepository;
import com.ledgerlens.repository.FinancialExceptionRepository;
import com.ledgerlens.repository.InvestigationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Runs a full end-to-end demo validation:
 *   1. Seed demo data (idempotent)
 *   2. Run reconciliation
 *   3. Run investigations on all open exceptions
 *   4. Verify audit trail
 *   5. Collect dashboard stats
 *   6. Produce DemoValidationReportDto
 */
@Service
public class DemoValidationService {

    private final SeedDataService seedDataService;
    private final ReconciliationService reconciliationService;
    private final InvestigationService investigationService;
    private final DashboardService dashboardService;
    private final FinancialExceptionRepository exceptionRepository;
    private final InvestigationRepository investigationRepository;
    private final AuditLogRepository auditLogRepository;
    private final MerchantContext merchantContext;

    public DemoValidationService(
            SeedDataService seedDataService,
            ReconciliationService reconciliationService,
            InvestigationService investigationService,
            DashboardService dashboardService,
            FinancialExceptionRepository exceptionRepository,
            InvestigationRepository investigationRepository,
            AuditLogRepository auditLogRepository,
            MerchantContext merchantContext) {
        this.seedDataService = seedDataService;
        this.reconciliationService = reconciliationService;
        this.investigationService = investigationService;
        this.dashboardService = dashboardService;
        this.exceptionRepository = exceptionRepository;
        this.investigationRepository = investigationRepository;
        this.auditLogRepository = auditLogRepository;
        this.merchantContext = merchantContext;
    }

    @Transactional
    public DemoValidationReportDto runValidation() {
        List<DemoValidationReportDto.StepResult> steps = new ArrayList<>();
        boolean allPass = true;

        // -- Step 1: Seed Demo Data --------------------------------------------
        SeedResponseDto seedResponse;
        try {
            seedResponse = seedDataService.seedDemoData();
            int ordersSeeded = seedResponse.getOrdersCreated() != null ? seedResponse.getOrdersCreated().size() : 0;
            int paymentsSeeded = seedResponse.getPaymentsCreated() != null ? seedResponse.getPaymentsCreated().size() : 0;
            int refundsSeeded = seedResponse.getRefundsCreated() != null ? seedResponse.getRefundsCreated().size() : 0;
            int feesSeeded = seedResponse.getFeesCreated() != null ? seedResponse.getFeesCreated().size() : 0;
            int settlementsSeeded = seedResponse.getSettlementsCreated() != null ? seedResponse.getSettlementsCreated().size() : 0;
            int adjustmentsSeeded = seedResponse.getAdjustmentsCreated() != null ? seedResponse.getAdjustmentsCreated().size() : 0;

            steps.add(new DemoValidationReportDto.StepResult(1, "Seed Demo Data", "PASS",
                    String.format("Seeded: %d orders, %d payments, %d refunds, %d fees, %d settlements, %d adjustments",
                            ordersSeeded, paymentsSeeded, refundsSeeded, feesSeeded, settlementsSeeded, adjustmentsSeeded)));
        } catch (Exception e) {
            allPass = false;
            seedResponse = null;
            steps.add(new DemoValidationReportDto.StepResult(1, "Seed Demo Data", "FAIL", "Error: " + e.getMessage()));
        }

        // -- Step 2: Reconciliation --------------------------------------------
        ReconciliationResultDto reconResult;
        try {
            reconResult = reconciliationService.reconcileAll(java.util.UUID.randomUUID().toString());
            steps.add(new DemoValidationReportDto.StepResult(2, "Run Reconciliation", "PASS",
                    String.format("Checked %d records, created %d exceptions, total discrepancy: INR %s",
                            reconResult.getRecordsChecked(),
                            reconResult.getExceptionsCreated(),
                            reconResult.getTotalDiscrepancyAmount())));
        } catch (Exception e) {
            allPass = false;
            reconResult = null;
            steps.add(new DemoValidationReportDto.StepResult(2, "Run Reconciliation", "FAIL", "Error: " + e.getMessage()));
        }

        // -- Step 3: Exception Detection Verification --------------------------
        List<FinancialExceptionResponseDto> allExceptions;
        try {
            String merchantId = merchantContext.merchantId();
            List<FinancialExceptionResponseDto> merchantExceptions = exceptionRepository.findByMerchantId(merchantId).stream()
                    .map(ex -> FinancialExceptionResponseDto.builder()
                            .exceptionId(ex.getExceptionId())
                            .exceptionType(ex.getExceptionType())
                            .severity(ex.getSeverity())
                            .status(ex.getStatus())
                            .discrepancyAmount(ex.getDiscrepancyAmount())
                            .description(ex.getDescription())
                            .merchantId(ex.getMerchantId())
                            .detectedAt(ex.getDetectedAt())
                            .build())
                    .collect(Collectors.toList());
            allExceptions = merchantExceptions;
            steps.add(new DemoValidationReportDto.StepResult(3, "Exception Detection", "PASS",
                    String.format("Detected %d financial exceptions for merchant %s", merchantExceptions.size(), merchantId)));
        } catch (Exception e) {
            allPass = false;
            allExceptions = List.of();
            steps.add(new DemoValidationReportDto.StepResult(3, "Exception Detection", "FAIL", "Error: " + e.getMessage()));
        }

        // -- Step 4: AI Investigation ------------------------------------------
        RunInvestigationsResultDto invResult;
        try {
            invResult = investigationService.investigateAllOpenExceptions();
            steps.add(new DemoValidationReportDto.StepResult(4, "AI Investigation", "PASS",
                    String.format("Investigated %d exceptions: %d new, %d already investigated, %d auto-resolved",
                            invResult.getExceptionsProcessed() + invResult.getAlreadyInvestigated(),
                            invResult.getInvestigationsCreated(),
                            invResult.getAlreadyInvestigated(),
                            invResult.getAutoResolved())));
        } catch (Exception e) {
            allPass = false;
            invResult = null;
            steps.add(new DemoValidationReportDto.StepResult(4, "AI Investigation", "FAIL", "Error: " + e.getMessage()));
        }

        // -- Step 5: Audit Trail -----------------------------------------------
        long auditCount = 0;
        try {
            auditCount = auditLogRepository.count();
            boolean auditOk = auditCount > 0;
            if (!auditOk) allPass = false;
            steps.add(new DemoValidationReportDto.StepResult(5, "Audit Trail", auditOk ? "PASS" : "FAIL",
                    String.format("Total audit entries: %d", auditCount)));
        } catch (Exception e) {
            allPass = false;
            steps.add(new DemoValidationReportDto.StepResult(5, "Audit Trail", "FAIL", "Error: " + e.getMessage()));
        }

        // -- Step 6: Dashboard Stats -------------------------------------------
        DashboardStatsDto dashboardStats;
        try {
            dashboardStats = dashboardService.getDashboardStats();
            steps.add(new DemoValidationReportDto.StepResult(6, "Dashboard Stats", "PASS",
                    String.format("Transactions: %d, Open Exceptions: %d, Unreconciled: INR %s",
                            dashboardStats.getTotalTransactions(),
                            dashboardStats.getOpenExceptionsCount(),
                            dashboardStats.getUnreconciledAmount())));
        } catch (Exception e) {
            allPass = false;
            dashboardStats = null;
            steps.add(new DemoValidationReportDto.StepResult(6, "Dashboard Stats", "FAIL", "Error: " + e.getMessage()));
        }

        // -- Build Exception Summary -------------------------------------------
        DemoValidationReportDto.ExceptionsSummary exSummary = new DemoValidationReportDto.ExceptionsSummary();
        if (!allExceptions.isEmpty()) {
            exSummary.setTotalExceptions(allExceptions.size());
            exSummary.setOpenExceptions((int) allExceptions.stream()
                    .filter(e -> e.getStatus() == ExceptionStatus.OPEN).count());
            exSummary.setInvestigatingExceptions((int) allExceptions.stream()
                    .filter(e -> e.getStatus() == ExceptionStatus.INVESTIGATING).count());
            exSummary.setResolvedExceptions((int) allExceptions.stream()
                    .filter(e -> e.getStatus() == ExceptionStatus.RESOLVED_AUTO || e.getStatus() == ExceptionStatus.RESOLVED_MANUAL).count());

            Map<String, Long> byType = allExceptions.stream()
                    .filter(e -> e.getExceptionType() != null)
                    .collect(Collectors.groupingBy(e -> e.getExceptionType().name(), Collectors.counting()));
            exSummary.setByType(byType);

            Map<String, Long> bySeverity = allExceptions.stream()
                    .filter(e -> e.getSeverity() != null)
                    .collect(Collectors.groupingBy(e -> e.getSeverity().name(), Collectors.counting()));
            exSummary.setBySeverity(bySeverity);

            List<DemoValidationReportDto.ExceptionBrief> briefs = allExceptions.stream()
                    .map(e -> new DemoValidationReportDto.ExceptionBrief(
                            e.getExceptionId(), e.getExceptionType(), e.getSeverity(),
                            e.getStatus(), e.getDiscrepancyAmount()))
                    .collect(Collectors.toList());
            exSummary.setExceptions(briefs);
        }

        // -- Build Investigation Summary ----------------------------------------
        DemoValidationReportDto.InvestigationsSummary invSummary;
        if (invResult != null) {
            String merchantId = merchantContext.merchantId();
            List<Investigation> merchantInv = investigationRepository.findByException_MerchantId(merchantId);
            long totalInv = merchantInv.size();
            long aiInv = merchantInv.stream()
                    .filter(i -> i.getAiModelVersion() != null && i.getAiModelVersion().startsWith("gemini"))
                    .count();
            long fallbackInv = totalInv - aiInv;
            invSummary = new DemoValidationReportDto.InvestigationsSummary(
                    (int) totalInv, (int) aiInv, (int) fallbackInv,
                    invResult.getAutoResolved(), invResult.getSentToHuman());
        } else {
            invSummary = new DemoValidationReportDto.InvestigationsSummary(0, 0, 0, 0, 0);
        }

        // -- Build Seed Summary ------------------------------------------------
        DemoValidationReportDto.SeedSummary seedSummary;
        if (seedResponse != null) {
            seedSummary = new DemoValidationReportDto.SeedSummary(
                    seedResponse.getOrdersCreated() != null ? seedResponse.getOrdersCreated().size() : 0,
                    seedResponse.getPaymentsCreated() != null ? seedResponse.getPaymentsCreated().size() : 0,
                    seedResponse.getRefundsCreated() != null ? seedResponse.getRefundsCreated().size() : 0,
                    seedResponse.getFeesCreated() != null ? seedResponse.getFeesCreated().size() : 0,
                    seedResponse.getSettlementsCreated() != null ? seedResponse.getSettlementsCreated().size() : 0,
                    seedResponse.getAdjustmentsCreated() != null ? seedResponse.getAdjustmentsCreated().size() : 0);
        } else {
            seedSummary = new DemoValidationReportDto.SeedSummary(0, 0, 0, 0, 0, 0);
        }

        // -- Build Reconciliation Summary --------------------------------------
        DemoValidationReportDto.ReconciliationSummary reconSummary;
        if (reconResult != null) {
            reconSummary = new DemoValidationReportDto.ReconciliationSummary(
                    reconResult.getRecordsChecked(),
                    reconResult.getExceptionsCreated(),
                    reconResult.getFailedChecks(),
                    reconResult.getSuccessfulChecks(),
                    reconResult.getTotalDiscrepancyAmount());
        } else {
            reconSummary = new DemoValidationReportDto.ReconciliationSummary(0, 0, 0, 0, java.math.BigDecimal.ZERO);
        }

        // -- Determine Overall Status ------------------------------------------
        boolean anyFail = steps.stream().anyMatch(s -> "FAIL".equals(s.getStatus()));
        String overallStatus = anyFail ? (allPass ? "PARTIAL" : "FAIL") : "PASS";
        // Refine: if audit entries > 0 and reconciliation ran and exceptions detected => PASS
        if (!anyFail) overallStatus = "PASS";
        else if (reconResult != null && exSummary.getTotalExceptions() > 0) overallStatus = "PARTIAL";
        else overallStatus = "FAIL";

        return DemoValidationReportDto.builder()
                .overallStatus(overallStatus)
                .generatedAt(OffsetDateTime.now())
                .seedSummary(seedSummary)
                .reconciliationSummary(reconSummary)
                .exceptionsSummary(exSummary)
                .investigationsSummary(invSummary)
                .auditSummary(new DemoValidationReportDto.AuditSummary(auditCount, auditCount > 0))
                .dashboardStats(dashboardStats)
                .stepsVerified(steps)
                .build();
    }
}
