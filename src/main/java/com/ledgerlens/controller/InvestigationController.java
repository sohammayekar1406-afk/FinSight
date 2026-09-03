package com.ledgerlens.controller;

import com.ledgerlens.dto.InvestigationResponseDto;
import com.ledgerlens.dto.RunInvestigationsResultDto;
import com.ledgerlens.service.InvestigationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investigations")
public class InvestigationController {

    private final InvestigationService investigationService;

    public InvestigationController(InvestigationService investigationService) {
        this.investigationService = investigationService;
    }

    @PostMapping("/{exceptionId}")
    public ResponseEntity<InvestigationResponseDto> investigateException(@PathVariable String exceptionId) {
        InvestigationResponseDto response = investigationService.investigateException(exceptionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{exceptionId}")
    public ResponseEntity<InvestigationResponseDto> getInvestigation(@PathVariable String exceptionId) {
        InvestigationResponseDto response = investigationService.getInvestigation(exceptionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/run")
    public ResponseEntity<RunInvestigationsResultDto> investigateAllOpenExceptions() {
        RunInvestigationsResultDto result = investigationService.investigateAllOpenExceptions();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{exceptionId}/resolve")
    public ResponseEntity<InvestigationResponseDto> resolveException(@PathVariable String exceptionId) {
        InvestigationResponseDto response = investigationService.resolveExceptionManually(exceptionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{exceptionId}/approve")
    public ResponseEntity<InvestigationResponseDto> approveException(@PathVariable String exceptionId) {
        InvestigationResponseDto response = investigationService.approveException(exceptionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{exceptionId}/reject")
    public ResponseEntity<InvestigationResponseDto> rejectException(@PathVariable String exceptionId) {
        InvestigationResponseDto response = investigationService.rejectException(exceptionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{exceptionId}/escalate")
    public ResponseEntity<InvestigationResponseDto> escalateException(@PathVariable String exceptionId) {
        InvestigationResponseDto response = investigationService.escalateException(exceptionId);
        return ResponseEntity.ok(response);
    }
}
