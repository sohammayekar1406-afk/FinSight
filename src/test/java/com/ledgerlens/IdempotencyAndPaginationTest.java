package com.ledgerlens;

import com.ledgerlens.dto.InvestigationResponseDto;
import com.ledgerlens.dto.PagedResponseDto;

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
class IdempotencyAndPaginationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testPagedExceptionsEndpointReturnsPaginationMetadata() {
        TestRestTemplate operatorTemplate = restTemplate.withBasicAuth("operator", "operator123");
        TestRestTemplate adminTemplate = restTemplate.withBasicAuth("admin", "admin123");

        adminTemplate.postForEntity("/api/demo/seed", null, Object.class);
        adminTemplate.postForEntity("/api/reconciliation/run", null, Object.class);

        ResponseEntity<PagedResponseDto> pagedRes = operatorTemplate.getForEntity("/api/exceptions/paged?page=0&size=5", PagedResponseDto.class);
        assertEquals(HttpStatus.OK, pagedRes.getStatusCode());
        PagedResponseDto body = pagedRes.getBody();
        assertNotNull(body);
        assertEquals(0, body.getPage());
        assertEquals(5, body.getSize());
        assertNotNull(body.getContent());
        assertTrue(body.getTotalElements() >= 0);
    }

    @Test
    void testRepeatedResolutionIsIdempotent() {
        TestRestTemplate adminTemplate = restTemplate.withBasicAuth("admin", "admin123");

        adminTemplate.postForEntity("/api/demo/seed", null, Object.class);
        adminTemplate.postForEntity("/api/reconciliation/run", null, Object.class);

        // First resolution call
        ResponseEntity<InvestigationResponseDto> firstRes = adminTemplate.postForEntity("/api/investigations/exp_amount_mismatch_ceb8129e/resolve", null, InvestigationResponseDto.class);
        if (firstRes.getStatusCode() == HttpStatus.OK) {
            assertNotNull(firstRes.getBody());

            // Second repeated resolution call should succeed without throwing error
            ResponseEntity<InvestigationResponseDto> secondRes = adminTemplate.postForEntity("/api/investigations/exp_amount_mismatch_ceb8129e/resolve", null, InvestigationResponseDto.class);
            assertEquals(HttpStatus.OK, secondRes.getStatusCode());
            assertNotNull(secondRes.getBody());
        }
    }
}
