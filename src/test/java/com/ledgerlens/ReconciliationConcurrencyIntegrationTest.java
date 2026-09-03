package com.ledgerlens;

import com.ledgerlens.dto.ReconciliationResultDto;
import com.ledgerlens.repository.ReconciliationRunRepository;
import com.ledgerlens.service.ReconciliationService;
import com.ledgerlens.service.SeedDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ReconciliationConcurrencyIntegrationTest {

    @Autowired private ReconciliationRunRepository reconciliationRunRepository;
    @Autowired private ReconciliationService reconciliationService;
    @Autowired private SeedDataService seedDataService;
    @Autowired private com.ledgerlens.repository.ReconciliationExecutionLockRepository reconciliationExecutionLockRepository;

    @Test
    void repeatedAndConcurrentRequestsWithTheSameKeyReturnTheSinglePersistedRun() throws Exception {
        seedDataService.seedDemoData();

        // Pre-initialize the lock to avoid race condition during concurrent test execution
        String merchantId = "merchant_a";
        reconciliationExecutionLockRepository.findById("MERCHANT:" + merchantId)
                .orElseGet(() -> reconciliationExecutionLockRepository.save(
                        new com.ledgerlens.entity.ReconciliationExecutionLock("MERCHANT:" + merchantId)));

        String key = "test-" + UUID.randomUUID();
        long runsBefore = reconciliationRunRepository.count();
        CompletableFuture<ReconciliationResultDto> first = CompletableFuture.supplyAsync(
                () -> reconciliationService.reconcileAll(key));
        CompletableFuture<ReconciliationResultDto> second = CompletableFuture.supplyAsync(
                () -> reconciliationService.reconcileAll(key));

        ReconciliationResultDto firstResponse = first.get(30, TimeUnit.SECONDS);
        ReconciliationResultDto secondResponse = second.get(30, TimeUnit.SECONDS);

        assertNotNull(firstResponse);
        assertNotNull(secondResponse);
        assertEquals(firstResponse.getReconciliationId(), secondResponse.getReconciliationId());
        assertEquals(runsBefore + 1, reconciliationRunRepository.count());
    }
}
