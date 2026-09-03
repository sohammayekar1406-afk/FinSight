package com.ledgerlens.controller;

import com.ledgerlens.dto.ReconciliationItemResultDto;
import com.ledgerlens.dto.ReconciliationResultDto;
import com.ledgerlens.service.ReconciliationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/run")
    public ResponseEntity<ReconciliationResultDto> runGlobalReconciliation(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        // A generated key preserves the previous API behaviour for callers that do not opt in.
        ReconciliationResultDto result = reconciliationService.reconcileAll(
                (idempotencyKey == null || idempotencyKey.isBlank()) ? java.util.UUID.randomUUID().toString() : idempotencyKey);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/payment/{paymentId}")
    public ResponseEntity<ReconciliationItemResultDto> reconcilePayment(@PathVariable String paymentId) {
        ReconciliationItemResultDto result = reconciliationService.reconcilePayment(paymentId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<ReconciliationItemResultDto> reconcileOrder(@PathVariable String orderId) {
        ReconciliationItemResultDto result = reconciliationService.reconcileOrder(orderId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/settlement/{settlementId}")
    public ResponseEntity<ReconciliationItemResultDto> reconcileSettlement(@PathVariable String settlementId) {
        ReconciliationItemResultDto result = reconciliationService.reconcileSettlement(settlementId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "OPERATIONAL",
                "engine", "Rule-Based Reconciliation Engine"
        ));
    }
}
