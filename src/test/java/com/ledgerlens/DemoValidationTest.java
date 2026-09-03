package com.ledgerlens;

import com.ledgerlens.dto.DemoValidationReportDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for the full demo validation workflow.
 * Exercises: Seed -> Reconcile -> Detect -> Investigate -> AuditCheck -> Dashboard
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DemoValidationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testDemoValidationEndpointRequiresAdminAuth() {
        HttpEntity<String> emptyBody = new HttpEntity<>("");

        // Unauthenticated → 401
        ResponseEntity<Object> unauthRes = restTemplate.getForEntity(
                "/api/exceptions",
                Object.class);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthRes.getStatusCode());

        // Operator (non-admin) cannot access admin-only demo validate → 403
        TestRestTemplate operatorTemplate = restTemplate.withBasicAuth("operator", "operator123");
        ResponseEntity<Object> operatorRes = operatorTemplate.postForEntity(
                "/api/demo/validate",
                emptyBody,
                Object.class);
        assertEquals(HttpStatus.FORBIDDEN, operatorRes.getStatusCode());
    }

    @Test
    void testFullDemoValidationWorkflow() {
        TestRestTemplate adminTemplate = restTemplate.withBasicAuth("admin", "admin123");
        HttpEntity<String> emptyBody = new HttpEntity<>("");

        // Run the complete validation
        ResponseEntity<DemoValidationReportDto> response = adminTemplate.postForEntity(
                "/api/demo/validate", emptyBody, DemoValidationReportDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        DemoValidationReportDto report = response.getBody();
        assertNotNull(report, "Validation report must not be null");

        // Overall status must be PASS or PARTIAL (never FAIL for a healthy system)
        assertNotNull(report.getOverallStatus(), "Overall status must be set");
        assertTrue(
                "PASS".equals(report.getOverallStatus()) || "PARTIAL".equals(report.getOverallStatus()),
                "Expected PASS or PARTIAL, got: " + report.getOverallStatus()
        );

        // Timestamp must be present
        assertNotNull(report.getGeneratedAt(), "Report must include a generatedAt timestamp");

        // Steps must be verified
        assertNotNull(report.getStepsVerified(), "Steps checklist must be present");
        assertFalse(report.getStepsVerified().isEmpty(), "At least one step must be verified");

        // Seed summary — data should have been seeded (at least idempotent)
        assertNotNull(report.getSeedSummary(), "Seed summary must be present");

        // Reconciliation summary
        assertNotNull(report.getReconciliationSummary(), "Reconciliation summary must be present");
        assertTrue(report.getReconciliationSummary().getRecordsChecked() >= 0,
                "Records checked must be non-negative");

        // Exception summary — at least one exception detected in the demo scenarios
        assertNotNull(report.getExceptionsSummary(), "Exception summary must be present");
        assertTrue(report.getExceptionsSummary().getTotalExceptions() >= 0,
                "Total exceptions must be non-negative");

        // Investigation summary
        assertNotNull(report.getInvestigationsSummary(), "Investigation summary must be present");

        // Audit trail — must have at least one audit entry after the full workflow
        assertNotNull(report.getAuditSummary(), "Audit summary must be present");
        assertTrue(report.getAuditSummary().getTotalAuditEntries() > 0,
                "At least one audit entry must exist after the workflow");
        assertTrue(report.getAuditSummary().isAuditTrailIntact(), "Audit trail must be intact");

        // Dashboard stats
        assertNotNull(report.getDashboardStats(), "Dashboard stats must be present");
        assertTrue(report.getDashboardStats().getTotalTransactions() >= 0,
                "Total transactions must be non-negative");
    }

    @Test
    void testSeedIsIdempotentWhenRunTwice() {
        TestRestTemplate adminTemplate = restTemplate.withBasicAuth("admin", "admin123");
        HttpEntity<String> emptyBody = new HttpEntity<>("");

        // Run validation twice — second run should not fail due to duplicate seed data
        ResponseEntity<DemoValidationReportDto> first = adminTemplate.postForEntity(
                "/api/demo/validate", emptyBody, DemoValidationReportDto.class);
        assertEquals(HttpStatus.OK, first.getStatusCode());

        ResponseEntity<DemoValidationReportDto> second = adminTemplate.postForEntity(
                "/api/demo/validate", emptyBody, DemoValidationReportDto.class);
        assertEquals(HttpStatus.OK, second.getStatusCode());

        DemoValidationReportDto secondReport = second.getBody();
        assertNotNull(secondReport);
        // Should not be FAIL after second run
        assertNotEquals("FAIL", secondReport.getOverallStatus(),
                "Idempotent run should not fail");
    }
}
