package com.ledgerlens;

import com.ledgerlens.dto.FinancialExceptionResponseDto;
import com.ledgerlens.dto.InvestigationResponseDto;
import com.ledgerlens.entity.enums.ExceptionStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class InvestigationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCompleteDemoWorkflowSeedReconcileInvestigateResolve() {
        TestRestTemplate adminTemplate = restTemplate.withBasicAuth("admin", "admin123");

        // 1. Seed Demo Data
        ResponseEntity<Object> seedRes = adminTemplate.postForEntity("/api/demo/seed", null, Object.class);
        assertEquals(HttpStatus.OK, seedRes.getStatusCode());

        // 2. Run Reconciliation
        ResponseEntity<Object> reconRes = adminTemplate.postForEntity("/api/reconciliation/run", null, Object.class);
        assertEquals(HttpStatus.OK, reconRes.getStatusCode());

        // 3. Fetch exceptions and verify exceptions were detected
        ResponseEntity<FinancialExceptionResponseDto[]> exceptionsRes = adminTemplate.getForEntity("/api/exceptions", FinancialExceptionResponseDto[].class);
        assertEquals(HttpStatus.OK, exceptionsRes.getStatusCode());
        FinancialExceptionResponseDto[] exceptions = exceptionsRes.getBody();
        assertNotNull(exceptions);
        assertTrue(exceptions.length > 0, "Exceptions should be detected after reconciliation");

        String targetExceptionId = exceptions[0].getExceptionId();

        // 4. Run Investigation on single exception
        ResponseEntity<InvestigationResponseDto> invRes = adminTemplate.postForEntity("/api/investigations/" + targetExceptionId, null, InvestigationResponseDto.class);
        assertEquals(HttpStatus.OK, invRes.getStatusCode());
        InvestigationResponseDto inv = invRes.getBody();
        assertNotNull(inv);
        assertEquals(targetExceptionId, inv.getExceptionId());
        assertNotNull(inv.getSummary());
        assertNotNull(inv.getEvidence());
        assertNotNull(inv.getEvidence().getLineage());

        // 5. Mark Exception as Manually Resolved
        ResponseEntity<InvestigationResponseDto> resolveRes = adminTemplate.postForEntity("/api/investigations/" + targetExceptionId + "/resolve", null, InvestigationResponseDto.class);
        assertEquals(HttpStatus.OK, resolveRes.getStatusCode());
        InvestigationResponseDto resolvedInv = resolveRes.getBody();
        assertNotNull(resolvedInv);
        assertEquals(ExceptionStatus.RESOLVED_MANUAL, resolvedInv.getEvidence().getException().getStatus());

        // 6. Verify GET exception returns RESOLVED_MANUAL status
        ResponseEntity<FinancialExceptionResponseDto> getExRes = adminTemplate.getForEntity("/api/exceptions/" + targetExceptionId, FinancialExceptionResponseDto.class);
        assertEquals(HttpStatus.OK, getExRes.getStatusCode());
        assertEquals(ExceptionStatus.RESOLVED_MANUAL, getExRes.getBody().getStatus());
    }
}
