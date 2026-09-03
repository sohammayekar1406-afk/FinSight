package com.ledgerlens.controller;

import com.ledgerlens.dto.AdjustmentRequestDto;
import com.ledgerlens.dto.AdjustmentResponseDto;
import com.ledgerlens.service.AdjustmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adjustments")
public class AdjustmentController {

    private final AdjustmentService adjustmentService;

    public AdjustmentController(AdjustmentService adjustmentService) {
        this.adjustmentService = adjustmentService;
    }

    @PostMapping
    public ResponseEntity<AdjustmentResponseDto> createAdjustment(@Valid @RequestBody AdjustmentRequestDto dto) {
        AdjustmentResponseDto response = adjustmentService.createAdjustment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{adjustmentId}")
    public ResponseEntity<AdjustmentResponseDto> getAdjustment(@PathVariable String adjustmentId) {
        AdjustmentResponseDto response = adjustmentService.getAdjustmentByAdjustmentId(adjustmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AdjustmentResponseDto>> getAllAdjustments() {
        List<AdjustmentResponseDto> response = adjustmentService.getAllAdjustments();
        return ResponseEntity.ok(response);
    }
}
