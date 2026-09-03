package com.ledgerlens;

import com.ledgerlens.dto.ErrorResponseDto;
import com.ledgerlens.dto.OrderRequestDto;

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
class GlobalExceptionHandlerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testResourceNotFoundReturnsStandardizedJson() {
        TestRestTemplate operatorTemplate = restTemplate.withBasicAuth("operator", "operator123");

        ResponseEntity<ErrorResponseDto> response = operatorTemplate.getForEntity("/api/exceptions/non_existent_id", ErrorResponseDto.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponseDto body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertEquals("RESOURCE_NOT_FOUND", body.getError());
        assertEquals("/api/exceptions/non_existent_id", body.getPath());
        assertNotNull(body.getTimestamp());
        assertNotNull(body.getMessage());
    }

    @Test
    void testValidationErrorReturnsStandardizedJsonWithDetails() {
        TestRestTemplate adminTemplate = restTemplate.withBasicAuth("admin", "admin123");

        OrderRequestDto invalidDto = new OrderRequestDto();
        invalidDto.setOrderId(""); // Blank order ID

        ResponseEntity<ErrorResponseDto> response = adminTemplate.postForEntity("/api/orders", invalidDto, ErrorResponseDto.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponseDto body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("VALIDATION_FAILED", body.getError());
        assertEquals("/api/orders", body.getPath());
        assertNotNull(body.getDetails());
        assertFalse(body.getDetails().isEmpty());
    }
}
