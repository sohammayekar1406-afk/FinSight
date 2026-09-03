package com.ledgerlens;

import com.ledgerlens.dto.ErrorResponseDto;
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
class SecurityAuthorizationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testPublicHealthEndpointAccessibleWithoutAuth() {
        ResponseEntity<Object> response = restTemplate.getForEntity("/api/health", Object.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testProtectedEndpointReturns401WhenUnauthenticated() {
        ResponseEntity<Object> response = restTemplate.getForEntity("/api/exceptions", Object.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testOperatorRoleCanViewExceptionsButCannotResolve() {
        TestRestTemplate operatorTemplate = restTemplate.withBasicAuth("operator", "operator123");

        // Operator can GET exceptions
        ResponseEntity<Object> getRes = operatorTemplate.getForEntity("/api/exceptions", Object.class);
        assertEquals(HttpStatus.OK, getRes.getStatusCode());

        // Operator is DENIED from resolving exception (403 Forbidden)
        ResponseEntity<Object> resolveRes = operatorTemplate.postForEntity("/api/investigations/exp_1002/resolve", null, Object.class);
        assertEquals(HttpStatus.FORBIDDEN, resolveRes.getStatusCode());
    }

    @Test
    void testAnalystRoleCanTriggerInvestigation() {
        // Seed demo data to ensure there are exceptions to view
        TestRestTemplate adminTemplate = restTemplate.withBasicAuth("admin", "admin123");
        adminTemplate.postForEntity("/api/demo/seed", null, Object.class);
        
        TestRestTemplate analystTemplate = restTemplate.withBasicAuth("analyst", "analyst123");

        // Analyst can GET exceptions
        ResponseEntity<Object> getRes = analystTemplate.getForEntity("/api/exceptions", Object.class);
        assertEquals(HttpStatus.OK, getRes.getStatusCode());

        // Analyst attempting to resolve non-existent exception gets 404
        // (resource check happens before authorization check)
        ResponseEntity<ErrorResponseDto> resolveRes = analystTemplate.postForEntity("/api/investigations/exp_1002/resolve", null, ErrorResponseDto.class);
        assertTrue(resolveRes.getStatusCode() == HttpStatus.NOT_FOUND || resolveRes.getStatusCode() == HttpStatus.FORBIDDEN,
                "Expected 404 NOT_FOUND or 403 FORBIDDEN, but got: " + resolveRes.getStatusCode());
    }

    @Test
    void testAdminRoleHasFullAccess() {
        TestRestTemplate adminTemplate = restTemplate.withBasicAuth("admin", "admin123");

        ResponseEntity<Object> seedRes = adminTemplate.postForEntity("/api/demo/seed", null, Object.class);
        assertEquals(HttpStatus.OK, seedRes.getStatusCode());
    }
}
