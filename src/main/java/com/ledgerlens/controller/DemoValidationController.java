package com.ledgerlens.controller;

import com.ledgerlens.dto.DemoValidationReportDto;
import com.ledgerlens.service.DemoValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the demo validation endpoint.
 * POST /api/demo/validate runs the full end-to-end workflow:
 * seeds data, reconciles, investigates, verifies audit trail.
 * Requires ADMIN role.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoValidationController {

    private final DemoValidationService demoValidationService;

    public DemoValidationController(DemoValidationService demoValidationService) {
        this.demoValidationService = demoValidationService;
    }

    @PostMapping("/validate")
    public ResponseEntity<DemoValidationReportDto> runValidation() {
        DemoValidationReportDto report = demoValidationService.runValidation();
        return ResponseEntity.ok(report);
    }
}